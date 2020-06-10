package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.Forecast;
import com.example.myapplication.Glucose;
import com.example.myapplication.User;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;

public class Graph {
    private final static String TAG="Estimator";
    public User user;

    public boolean doMmol = false;
    public double lowMark;
    public double highMark;
    public double goalMark;

    public static final double MMOLL_TO_MGDL = 18.0182;
    public static final double MGDL_TO_MMOLL = 1 / MMOLL_TO_MGDL;
    public static final int FUZZER = (1000 * 30 * 5); // 2.5 minutes
    private static final double vehicle_mode_adjust_mgdl = 18;

    public static double low_occurs_at = -1;
    public static double previous_low_occurs_at = -1;
    private static double low_occurs_at_processed_till_timestamp = -1;

    private boolean prediction_enabled = false;

    private final List<PointValue> polyBgValues = new ArrayList<PointValue>();
    private final List<PointValue> lowValues = new ArrayList<>();
    private final List<PointValue> inRangeValues = new ArrayList<>();
    private final List<PointValue> highValues = new ArrayList<>();
    private final List<PointValue> mealValues = new ArrayList<PointValue>();
    private final List<PointValue> activityValues = new ArrayList<PointValue>();

    private int predictivehours = 0;
    private final static boolean d = false; // debug flag, could be read from preferences
    final boolean show_moment_working_line =false;

    public double defaultMinY = unitized(40);
    public double defaultMaxY = unitized(250);

    public double now;
    public double end_time;
    public double start_time;
    public double predictive_end_time;

    private final static double timeshift = 500_000;
    final long PERIOD = 300_000; // 5 minutes

    private static boolean low_notifying = false;
    public static final int lowPredictAlertNotificationId = 013;

    final int pointSize=4;

    public int data_size;

    public Graph(boolean show_prediction, int num){
        prediction_enabled = show_prediction;
        data_size = num;

        lowMark = 70;
        highMark = 170;
        goalMark = -1;
    }

    public void setting_time(){
        int ago  = (user.getDATA().size()-data_size);
        now =  new Date().getTime() - (PERIOD*ago);

        end_time = (now + (60000 * 10)) / FUZZER;
        start_time = end_time - ((60000 * 60 * 24)) / FUZZER;
    }


    public void estimator(boolean simple){

        lowValues.clear();

        long highest_bgreading_timestamp = -1; // most recent bgreading timestamp we have
        //////////////

        double trend_start_working = now - (1000 * 60 * 30); // ??20 minutes
        final double trendstart = trend_start_working;

        Forecast.TrendLine[] polys = new Forecast.TrendLine[5];
        polys[0] = new Forecast.PolyTrendLine(1);
        polys[1] = new Forecast.LogTrendLine();
        polys[2] = new Forecast.ExpTrendLine();
        polys[3] = new Forecast.PowerTrendLine();
        Forecast.TrendLine poly = null;

        final List<Double> polyxList = new ArrayList<>();
        final List<Double> polyyList = new ArrayList<>();

        final double momentum_illustration_start = now - (1000 * 60 * 60 * 2); // 8 hours
        double last_calibration = 0;

        final boolean predict_lows =true;

        int num=0;

        for(final Meal meal : user.getMEAL()){ //모든 기록을 다 그래프에 표시하지x
            mealValues.add(new PointValue((float)meal.time/FUZZER, (float) unitized(50)));
        }

        for (final Activity activity: user.getACTIVITY()){
            activityValues.add(new PointValue((float)activity.time/FUZZER, (float) unitized(20)));
        }

        for (final Glucose bgReading : user.getDATA()) {
            if(num==data_size)
                break;
            num++;

            if (bgReading.glucose >= 400) {
                highValues.add(new PointValue((float) (bgReading.timestamp / FUZZER), (float) unitized(400)));
            } else if (unitized(bgReading.glucose) >= highMark) {
                highValues.add(new PointValue((float) (bgReading.timestamp / FUZZER), (float) unitized(bgReading.glucose)));
            } else if (unitized(bgReading.glucose) >= lowMark) {
                inRangeValues.add(new PointValue((float) (bgReading.timestamp / FUZZER), (float) unitized(bgReading.glucose)));
            } else if (bgReading.glucose >= 40) {
                lowValues.add(new PointValue((float) (bgReading.timestamp / FUZZER), (float) unitized(bgReading.glucose)));
            } else if (bgReading.glucose > 13) {
                lowValues.add(new PointValue((float) (bgReading.timestamp / FUZZER), (float) unitized(40)));
            }

            // momentum trend
            if (!simple && (bgReading.timestamp > trendstart) && (bgReading.timestamp > last_calibration)) {
//                if (has_filtered && (bgReading.filtered_calculated_value > 0) && (bgReading.filtered_calculated_value != bgReading.glucose)) {
//                    polyxList.add((double) bgReading.timestamp - timeshift);
//                    polyyList.add(unitized(bgReading.filtered_calculated_value));
//                }
                if (bgReading.glucose > 0) {
                    polyxList.add((double) bgReading.timestamp);
                    polyyList.add(unitized(bgReading.glucose));
                }
                if (d)
                    Log.d(TAG, "poly Added: " + qs(polyxList.get(polyxList.size() - 1)) + " / " + qs(polyyList.get(polyyList.size() - 1), 2));
            }
        }

        if (!simple) {
            // momentum
            try {
                if (d) Log.d("Graph", "moment Poly list size: " + polyxList.size());
                if (polyxList.size() > 1) {
                    final double[] polyys = Forecast.PolyTrendLine.toPrimitiveFromList(polyyList);
                    final double[] polyxs = Forecast.PolyTrendLine.toPrimitiveFromList(polyxList);

                    // set and evaluate poly curve models and select first best
                    double min_errors = 9999999;
                    for (Forecast.TrendLine this_poly : polys) {
                        if (this_poly != null) {
                            if (poly == null) {
                                poly = this_poly;
                            }
                            this_poly.setValues(polyys, polyxs);
                            if (this_poly.errorVarience() < min_errors) {
                                min_errors = this_poly.errorVarience();
                                poly = this_poly;
                                //if (d) Log.d(TAG, "set forecast best model to: " + poly.getClass().getSimpleName() + " with varience of: " + JoH.qs(poly.errorVarience(),14));
                            }

                        }
                    }
                    if (d)
                        Log.i("Graph", "set forecast best model to: " + poly.getClass().getSimpleName() + " with varience of: " + qs(poly.errorVarience(), 4));
                } else {
                    if (d) Log.i("Graph", "Not enough data for forecast model");
                }

            } catch (Exception e) {
                Log.e("Graph", " Error with poly trend: " + e.toString());
            }

            try {
                // show trend for whole bg reading area
                if ((show_moment_working_line) && (poly != null)) {

                    for (Glucose bgReading : user.getDATA()) {
                        // only show working curve for last x hours to a
                        if (bgReading.timestamp > momentum_illustration_start) {
                            double polyPredicty = poly.predict(bgReading.timestamp);
                            //if (d) Log.d(TAG, "Poly predict: "+JoH.qs(polyPredict)+" @ "+JoH.qs(iob.timestamp));
                            if ((polyPredicty < highMark) && (polyPredicty > 0)) {
                                PointValue zv = new PointValue((float) (bgReading.timestamp / FUZZER), (float) polyPredicty);
                                polyBgValues.add(zv);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Graph", "Error creating back trend: " + e.toString());
            }


            low_occurs_at = -1;
            try {
                if ((predict_lows) && (prediction_enabled) && (poly != null)) {
                    final double offset = unitized(vehicle_mode_adjust_mgdl);
                    final double plow_now =now;
                    double plow_timestamp = plow_now + (1000 * 60 * 99); // max look-ahead
                    double polyPredicty = poly.predict(plow_timestamp);
                    Log.d("Low Estimator", "Low predictor at max lookahead is: " + qs(polyPredicty));
                    low_occurs_at_processed_till_timestamp = highest_bgreading_timestamp; // store that we have processed up to this timestamp
                    if (polyPredicty <= (lowMark + offset)) {
                        low_occurs_at = plow_timestamp;
                        final double lowMarkIndicator = (lowMark - (lowMark / 4));
                        //if (d) Log.d(TAG, "Poly predict: "+JoH.qs(polyPredict)+" @ "+JoH.qsz(iob.timestamp));
                        while (plow_timestamp > plow_now) {
                            plow_timestamp = plow_timestamp - FUZZER;
                            polyPredicty = poly.predict(plow_timestamp);
                            if (polyPredicty > (lowMark + offset)) {
                                PointValue zv = new PointValue((float) (plow_timestamp / FUZZER), (float) polyPredicty);
                                polyBgValues.add(zv);
                            } else {
                                low_occurs_at = plow_timestamp;
                                if (polyPredicty > lowMarkIndicator) {
                                    polyBgValues.add(new PointValue((float) (plow_timestamp / FUZZER), (float) polyPredicty));
                                }
                            }
                        }
                        Log.i("Low Estimator", "LOW PREDICTED AT: " + dateTimeText((long) low_occurs_at));
                        predictivehours = Math.max(predictivehours, (int) ((low_occurs_at - plow_now) / (60 * 60 * 1000)) + 1);
                    }
                }

            } catch (NullPointerException e) {
                Log.d(TAG,"Error with low prediction trend: "+e.toString());
            }
        }
    }

    public synchronized List<Line> defaultLines(boolean simple) {
        List<Line> lines = new ArrayList<Line>();
        try {
            estimator(simple);

            predictive_end_time = simple ? end_time : ((end_time * FUZZER) + (60000 * 10) + (1000 * 60 * 60 * predictivehours)) / FUZZER; // used first in ideal/highline

            lines.add(mealLine());
            lines.add(actvityLine());

            lines.add(treatmentValuesLine());
            lines.add(highLine());
            lines.add(predictiveHighLine());
            lines.add(lowLine());
            lines.add(predictiveLowLine());

            if(goalMark!=-1) {
                lines.add(goalLine());
                lines.add(predictiveGoalLine());
            }

            lines.add(inRangeValuesLine());
            lines.add(lowValuesLine());
            lines.add(highValuesLine());

            lines.add(minShowLine());
            lines.add(maxShowLine());
        }catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error in bgbuilder defaultlines: " + e.toString());
        }
        return lines;
    }

    public Line treatmentValuesLine(){
        Line line = new Line();
        try {
            line = new Line(polyBgValues);
            line.setColor(Color.RED);
            line.setHasLines(false);
            line.setCubic(false);
            line.setStrokeWidth(1);
            line.setFilled(false);
            line.setPointRadius(1);
            line.setHasPoints(true);
            line.setHasLabels(false);
        }catch (Exception e) {
            if (d) Log.i(TAG, "Exception making treatment lines: " + e.toString());
        }
        return line;
    }

    public Line lowLine() {
        List<PointValue> lowLineValues = new ArrayList<PointValue>();
        lowLineValues.add(new PointValue((float) start_time, (float) unitized(lowMark)));
        lowLineValues.add(new PointValue((float) end_time, (float) unitized(lowMark)));
        Line lowLine = new Line(lowLineValues);
        lowLine.setHasPoints(false);
        lowLine.setAreaTransparency(50);
        lowLine.setColor(Color.RED);
        lowLine.setStrokeWidth(1);
        lowLine.setFilled(true);
        return lowLine;
    }

    public Line predictiveLowLine() {
        List<PointValue> lowLineValues = new ArrayList<PointValue>();
        lowLineValues.add(new PointValue((float) end_time, (float)unitized(lowMark)));
        lowLineValues.add(new PointValue((float) predictive_end_time, (float) unitized(lowMark)));
        Line lowLine = new Line(lowLineValues);
        lowLine.setHasPoints(false);
        lowLine.setAreaTransparency(40);
        lowLine.setColor(ChartUtils.darkenColor(ChartUtils.darkenColor(Color.RED)));
        lowLine.setStrokeWidth(1);
        lowLine.setFilled(true);
        return lowLine;
    }

    public Line highLine() {
        List<PointValue> highLineValues = new ArrayList<PointValue>();
        highLineValues.add(new PointValue((float) start_time, (float) unitized(highMark)));
        highLineValues.add(new PointValue((float) end_time, (float) unitized(highMark)));
        Line highLine = new Line(highLineValues);
        highLine.setHasPoints(false);
        highLine.setStrokeWidth(1);
        highLine.setColor(Color.YELLOW);
        return highLine;
    }

    public Line predictiveHighLine() {
        List<PointValue> predictiveHighLineValues = new ArrayList<PointValue>();
        predictiveHighLineValues.add(new PointValue((float) end_time, (float)unitized(highMark)));
        predictiveHighLineValues.add(new PointValue((float) predictive_end_time, (float)unitized(highMark)));
        Line highLine = new Line(predictiveHighLineValues);
        highLine.setHasPoints(false);
        highLine.setStrokeWidth(1);
        highLine.setColor(ChartUtils.darkenColor(ChartUtils.darkenColor(Color.YELLOW)));
        return highLine;
    }

    public Line goalLine() {
        List<PointValue> goalLineValues = new ArrayList<PointValue>();
        goalLineValues.add(new PointValue((float) start_time, (float)(unitized(goalMark))));
        goalLineValues.add(new PointValue((float) end_time, (float) goalMark));
        Line goalLine = new Line(goalLineValues);
        goalLine.setHasPoints(false);
        goalLine.setStrokeWidth(1);
        goalLine.setColor(Color.parseColor("#99ff7d"));
        return goalLine;
    }

    public Line predictiveGoalLine() {
        List<PointValue> predictiveGoalLineValues = new ArrayList<PointValue>();
        predictiveGoalLineValues.add(new PointValue((float) end_time, (float)unitized(goalMark)));
        predictiveGoalLineValues.add(new PointValue((float) predictive_end_time, (float)unitized(goalMark)));
        Line GoalLine = new Line(predictiveGoalLineValues);
        GoalLine.setHasPoints(false);
        GoalLine.setStrokeWidth(1);
        GoalLine.setColor(ChartUtils.darkenColor(ChartUtils.darkenColor(Color.parseColor("#99ff7d"))));
        return GoalLine;
    }
    public Line highValuesLine() {
        Line highValuesLine = new Line(highValues);
        highValuesLine.setColor(Color.parseColor("#E6AA33"));
        highValuesLine.setHasLines(false);
        highValuesLine.setPointRadius(pointSize);
        highValuesLine.setHasPoints(true);
        highValuesLine.setHasLabels(false);
        highValuesLine.setHasLabelsOnlyForSelected(false);
        return highValuesLine;
    }

    public Line lowValuesLine() {
        Line lowValuesLine = new Line(lowValues);
        lowValuesLine.setColor(Color.RED);
        lowValuesLine.setHasLines(false);
        lowValuesLine.setPointRadius(pointSize);
        lowValuesLine.setHasPoints(true);
        lowValuesLine.setHasLabels(false);
        lowValuesLine.setHasLabelsOnlyForSelected(false);
        return lowValuesLine;
    }

    public Line inRangeValuesLine() {
        Line inRangeValuesLine = new Line(inRangeValues);
        inRangeValuesLine.setColor(Color.parseColor("#00cc07"));
        inRangeValuesLine.setHasLines(false);
        inRangeValuesLine.setPointRadius(pointSize);
        inRangeValuesLine.setHasPoints(true);
        inRangeValuesLine.setHasLabels(false);
        inRangeValuesLine.setHasLabelsOnlyForSelected(false);
        return inRangeValuesLine;
    }


    public Line maxShowLine() {
        List<PointValue> maxShowValues = new ArrayList<PointValue>();
        maxShowValues.add(new PointValue((float) start_time, (float) defaultMaxY));
        maxShowValues.add(new PointValue((float) end_time, (float) defaultMaxY));
        Line maxShowLine = new Line(maxShowValues);
        maxShowLine.setHasLines(false);
        maxShowLine.setHasPoints(false);
        return maxShowLine;
    }

    public Line minShowLine() {
        List<PointValue> minShowValues = new ArrayList<PointValue>();
        minShowValues.add(new PointValue((float) start_time, (float) defaultMinY));
        minShowValues.add(new PointValue((float) end_time, (float) defaultMinY));
        Line minShowLine = new Line(minShowValues);
        minShowLine.setHasPoints(false);
        minShowLine.setHasLines(false);
        return minShowLine;
    }

    public Line actvityLine(){
        Line activityLine = new Line(activityValues);
        activityLine.setColor(Color.DKGRAY);
        activityLine.setHasLines(false);
        activityLine.setPointRadius(pointSize * 5 / 4);
        activityLine.setHasPoints(true);
        activityLine.setShape(ValueShape.SQUARE);
        activityLine.setHasLabels(false);
        return activityLine;
    }

    public Line mealLine(){
        Line mealLine = new Line(mealValues);
        mealLine.setColor(Color.MAGENTA);
        mealLine.setHasLines(false);
        mealLine.setPointRadius(pointSize * 5 / 4);
        mealLine.setHasPoints(true);
        mealLine.setShape(ValueShape.DIAMOND);
        mealLine.setHasLabels(false);
        return mealLine;
    }

    public static String dateTimeText(long timestamp) {
        return android.text.format.DateFormat.format("yyyy-MM-dd kk:mm:ss", timestamp).toString();
    }

    // qs = quick string conversion of double for printing
    public static String qs(double x) {
        return qs(x, 2);
    }

    // singletons to avoid repeated allocation
    private static DecimalFormatSymbols dfs;
    private static DecimalFormat df;
    public static String qs(double x, int digits) {

        if (digits == -1) {
            digits = 0;
            if (((int) x != x)) {
                digits++;
                if ((((int) x * 10) / 10 != x)) {
                    digits++;
                    if ((((int) x * 100) / 100 != x)) digits++;
                }
            }
        }

        if (dfs == null) {
            final DecimalFormatSymbols local_dfs = new DecimalFormatSymbols();
            local_dfs.setDecimalSeparator('.');
            dfs = local_dfs; // avoid race condition
        }

        final DecimalFormat this_df;
        // use singleton if on ui thread otherwise allocate new as DecimalFormat is not thread safe
        if (Thread.currentThread().getId() == 1) {
            if (df == null) {
                final DecimalFormat local_df = new DecimalFormat("#", dfs);
                local_df.setMinimumIntegerDigits(1);
                df = local_df; // avoid race condition
            }
            this_df = df;
        } else {
            this_df = new DecimalFormat("#", dfs);
        }

        this_df.setMaximumFractionDigits(digits);
        return this_df.format(x);
    }

    public double unitized(double value) {
        if (!doMmol) {
            return value;
        } else {
            return mmolConvert(value);
        }
    }

    public static double mmolConvert(double mgdl) {
        return mgdl * MGDL_TO_MMOLL;
    }

}
