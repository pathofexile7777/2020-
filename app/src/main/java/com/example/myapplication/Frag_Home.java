package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static android.app.Activity.RESULT_OK;

public class Frag_Home extends Fragment {
    private User User;
    private ViewGroup viewGroup;
    private Graph graph;

    public ImageView trending_arrow;
    public LineChartView lineChart;
    public LineChartData data;
    public TextView user_num,lowPredictText, BG_Text, DeltaText;
    public ActionBar actionBar;

    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;

    public static final int FUZZER = (1000 * 30 * 5); // 2.5 minute
    private int predictivehours = 0;

    public double end_time = (new Date().getTime() + (60000 * 10)) / FUZZER;
    public double start_time = end_time - ((60000 * 60 * 24)) / FUZZER;

    public String type;
    private int order=0;
    private int dots = 10;
    private int high=-1, low=-1, goal=-1;
    private boolean unit;

    public static final int MEAL = 100;
    public static final int ACTIVITY = 101;
    public final int HIGH =1;
    public final int LOW = -1;
    public final int NORMAL = 0;

    public int status = -99;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        viewGroup = (ViewGroup)inflater.inflate(R.layout.frag1,container,false);

        fab_open = AnimationUtils.loadAnimation((MainActivity)getActivity(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation((MainActivity)getActivity(), R.anim.fab_close);

        findViewById();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
            }
        });

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                Toast.makeText((MainActivity)getActivity(), "식사 정보 추가", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),AddMeal.class);
                startActivityForResult(intent, MEAL);
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                Toast.makeText((MainActivity)getActivity(), "활동 정보 추가", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),AddActivity.class);
                startActivityForResult(intent, ACTIVITY);
            }
        });

//
//        bt_next_user.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    check_order(true);
//                    update();
//            }
//        });
//
//        bt_prev_user.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                check_order(false);
//                update();
//            }
//        });

        if(getArguments()!=null){
            User = getArguments().getParcelable("userlist");

            high = getArguments().getInt("highBg",-1);
            low = getArguments().getInt("lowBg",-1);
            goal = getArguments().getInt("goal",-1);
            unit = getArguments().getBoolean("unit");
        }

//        dots = User.getDATA().size();

        build_graph();
        graph.estimator(false);
        initLineChart();//initialization

        if(unit == false)
            type = " mg/dl";
        else
            type =" mmol/l";

        Low_alarm();
        current_bg();

        return viewGroup;
    }

    @Override
    public  void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).showOptionMenu(true);
    }


    private void findViewById(){
        lineChart = viewGroup.findViewById(R.id.main_chart);
//        bt_next_bg = (Button) findViewById(R.id.next_bg);
//        bt_prev_bg = (Button) findViewById(R.id.prev_bg);
//        bt_prev_user = (Button) findViewById(R.id.prev_user);
//        bt_next_user = (Button) findViewById(R.id.next_user);
//        user_num = viewGroup.findViewById(R.id.user_num);
        lowPredictText = viewGroup.findViewById(R.id.low_predict);
        trending_arrow = viewGroup.findViewById(R.id.arrow);
        DeltaText=viewGroup.findViewById(R.id.delta);
        DeltaText.setText(null);
        BG_Text = viewGroup.findViewById(R.id.glucose);
        BG_Text.setText(null);
        actionBar =  ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle("메인");

        fab = viewGroup.findViewById(R.id.fab);
        fab1 = viewGroup.findViewById(R.id.fab1);
        fab2 = viewGroup.findViewById(R.id.fab2);
    }

    public void update(){
        graph.estimator(false);
        initLineChart();//initialization
        Low_alarm();
        current_bg();
    }

    public void build_graph(){
//        int num_order = order+1;
//        user_num.setText("User["+num_order+"]");

        graph = new Graph(true,dots);
        graph.user=User;

        if(high != -1)
            graph.highMark = high;
        if(low !=-1)
           graph.lowMark = low;
        if(goal != -1)
            graph.goalMark = goal;
        graph.doMmol = unit;
        graph.setting_time();
    }

    private void initLineChart() {
        graph.setting_time();

        data = new LineChartData(graph.defaultLines(false));

        Axis axisX = new Axis(); //X axis
        axisX.setHasTiltedLabels(true);  // X axis font is oblique display or straight, true is oblique display
        axisX.setTextColor(Color.GRAY);  // Set the font color
        axisX.setName("(time)"); //Table name
        axisX.setTextSize(10);// Set the font size
        axisX.setMaxLabelChars(10); //Up to a few X-axis coordinates, meaning your scaling allows the number of data on the X-axis to be 7<=x<=mAxisXValues.length

        axisX.setHasLines(true); //x axis split line
        axisX.setAutoGenerated(false);
        List<AxisValue> xAxisValues = new ArrayList<AxisValue>();

        final java.text.DateFormat timeFormat = new SimpleDateFormat(DateFormat.is24HourFormat(getContext()) ? "HH" : "h a");
        timeFormat.setTimeZone(TimeZone.getDefault());

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis((long)(start_time * FUZZER));
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis()<(start_time * FUZZER)){
            calendar.add(Calendar.HOUR, 1);
        }
        while (calendar.getTimeInMillis()< ( (end_time * FUZZER) + (predictivehours * 60 * 60 * 1000))) {
            xAxisValues.add(new AxisValue((calendar.getTimeInMillis() / FUZZER), (timeFormat.format(calendar.getTimeInMillis())).toCharArray()));
            calendar.add(Calendar.HOUR, 1);
        }
        axisX.setValues(xAxisValues);

        data.setAxisXBottom(axisX); //x axis at the bottom

        Axis axisY = new Axis();  //Y axis
        axisY.setAutoGenerated(false);
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int j = 1; j <= 12; j += 1) {
            if (!unit) {
                axisValues.add(new AxisValue(j * 50));
            } else {
                axisValues.add(new AxisValue(j * 2));
            }
        }
        axisY.setValues(axisValues);
        axisY.setName("("+type+")");//y axis label
        axisY.setTextSize(10);// Set the font size
        axisY.setInside(true);
        data.setAxisYLeft(axisY);  //Y axis is set to the left

        // Set the behavior properties, support for zoom, slide and pan
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.getChartData().setAxisXTop(null);
        lineChart.setMaxZoom(20f);//Maximum method scale
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setViewportCalculationEnabled(true);
        lineChart.setOnValueTouchListener(new ValueTouchListener());

        lineChart.setVisibility(View.VISIBLE);

        Viewport holdViewport = new Viewport();
        holdViewport.set(0, 0, 0, 0);

        Viewport moveViewPort = new Viewport(lineChart.getMaximumViewport());
        float tempwidth = (float) moveViewPort.width() / 4;
        holdViewport.left = moveViewPort.right - tempwidth;
        holdViewport.right = moveViewPort.right + (moveViewPort.width() / 24);
        holdViewport.top = moveViewPort.top;
        holdViewport.bottom = moveViewPort.bottom;
        lineChart.setCurrentViewport(holdViewport);
    }

//    public void check_order(boolean plus){
//        if(plus){
//            if(order == 32){
//                order = 0;
//            }
//            else{
//                order++;
//            }
//        }
//        else {
//            if(order == 0){
//                order = 32;
//            }
//            else{
//                order--;
//            }
//        }
//        dots =3;
//    }

    public void add_dots(){
//        User u =UserList.get(order);

        if(dots < User.getDATA().size()) {
            dots++;
        }else {
            Toast.makeText(getActivity(), "Maximum", Toast.LENGTH_SHORT).show();
            return;
        }
        graph.data_size = dots;
    }

    public void sub_dots(){
        if(dots > 1) {
            dots--;
        }else {
            Toast.makeText(getActivity(), "Minimum", Toast.LENGTH_SHORT).show();
            return;
        }
        graph.data_size = dots;
    }


//    private void generateLineData(int color, float range) {
//        // Cancel last animation if not finished.
//        lineChart.cancelDataAnimation();
//
//        // Modify data targets
//        Line line = data.getLines().get(0);// For this example there is always only one line.
//        line.setColor(color);
//        for (PointValue value : line.getValues()) {
//            // Change target only for Y value.
//            value.setTarget(value.getX(), (float) Math.random() * range);
//        }
//
//        // Start new data animation with 300ms duration;
//        lineChart.startDataAnimation(300);
//    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {
        @Override
        public void onValueDeselected() {
//            generateLineData(ChartUtils.COLOR_GREEN, 0);

        }

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
//            generateLineData(ChartUtils.COLOR_BLUE, 150);
            String bg = String.format("%.2f",value.getY());
            Toast.makeText(getActivity(),bg+type,Toast.LENGTH_SHORT).show();
        }
    }

    public long get_time(final long now, int ago, int size){
        final long PERIOD = 300_000; // 5 minutes
        ago = (size-ago);

        long time = now - (PERIOD*ago);

        return time;
    }

    public void Low_alarm() {
        lowPredictText.setText("");
        lowPredictText.setVisibility(View.INVISIBLE);
        if (graph.low_occurs_at > 0) {

            double low_predicted_alarm_minutes;
            low_predicted_alarm_minutes = 50d;

            // TODO use tsl()

            final double now = get_time(new Date().getTime(),dots,User.getDATA().size());
            final double predicted_low_in_mins = (graph.low_occurs_at - now) / 60000;

            if (predicted_low_in_mins > 1) {
                lowPredictText.append("저혈당 예상 시간: " + (int) predicted_low_in_mins + "minutes");
                if (predicted_low_in_mins < low_predicted_alarm_minutes) {
                    lowPredictText.setTextColor(Color.RED); // low front getting too close!
                } else {
                    final double previous_predicted_low_in_mins = (graph.previous_low_occurs_at - now) / 60000;
                    if ((graph.previous_low_occurs_at > 0) && ((previous_predicted_low_in_mins + 5) < predicted_low_in_mins)) {
                        lowPredictText.setTextColor(Color.GREEN); // low front is getting further away
                    } else {
                        lowPredictText.setTextColor(Color.parseColor("#E6AA33")); // low front is getting nearer!
                    }
                }
                lowPredictText.setVisibility(View.VISIBLE);
            }
            graph.previous_low_occurs_at = graph.low_occurs_at;
        }
    }

    public void current_bg(){

        Glucose G = User.getDATA().get(dots);
        float delta = G.glucose - User.getDATA().get(dots-1).glucose;

        BG_Text.setText(Float.toString(G.glucose));
        DeltaText.setText("Delta: "+ Float.toString(delta)+type);

        if(delta>=5)
            trending_arrow.setImageResource(R.drawable.ic_trending_up_black_24dp);
        else if(delta<=-5)
            trending_arrow.setImageResource(R.drawable.ic_trending_down_black_24dp);
        else
            trending_arrow.setImageResource(R.drawable.ic_trending_flat_black_24dp);

        if(G.glucose <= graph.lowMark) {
            trending_arrow.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            BG_Text.setTextColor(Color.RED);
            DeltaText.setTextColor(Color.RED);
            status = -1;
        }else if (G.glucose >= graph.highMark) {
            trending_arrow.setColorFilter(Color.parseColor("#E6AA33"), PorterDuff.Mode.SRC_IN);
            BG_Text.setTextColor(Color.parseColor("#E6AA33"));
            DeltaText.setTextColor(Color.parseColor("#E6AA33"));
            status = 1;
        }else {
            trending_arrow.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            BG_Text.setTextColor(Color.GRAY);
            DeltaText.setTextColor(Color.GRAY);
            status =0;
        }
    }

    public void anim() {

        if (isFabOpen) {
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
        } else {
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEAL) {
            if(resultCode == RESULT_OK){
                Meal meal = data.getParcelableExtra("MEAL");
                User.addMEAL(meal);
            }
        }
        else if(requestCode == ACTIVITY){
            if(resultCode == RESULT_OK){
                Activity active = data.getParcelableExtra("ACTIVE");
                User.addACTIVITY(active);
            }
        }
        update();
    }


}
