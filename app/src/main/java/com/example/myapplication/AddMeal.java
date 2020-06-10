package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddMeal extends AppCompatActivity {

    private ActionBar actionBar;
    private Button check, cancel, date_pick, time_pick;
    private EditText kcal, memo;
    private TextView tv_date, tv_time;
    private RadioGroup radioGroup;
    private RadioButton rbtn1, rbtn2, rbtn3, rbtn4;
    private int btn_on=0;
    final int DIALOG_DATE = 1;
    final int DIALOG_TIME = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPref sp = new SharedPref(this);

        if (sp.loadNightModeState())
        {
            setTheme(R.style.DarkTheme);
        }
        else
        {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        findViewById();

        date_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_DATE);
            }
        });

        time_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_TIME);
            }
        });

        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);
        rbtn1.setOnClickListener(radioButtonClickListener);
        rbtn2.setOnClickListener(radioButtonClickListener);
        rbtn3.setOnClickListener(radioButtonClickListener);
        rbtn4.setOnClickListener(radioButtonClickListener);

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str1 = tv_date.getText().toString();
                String str2 = tv_time.getText().toString();

                if (str1 == null || str2==null){
                    Toast.makeText(getApplicationContext(), "날짜와 시간을 선택해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(btn_on==0){
                    Toast.makeText(getApplicationContext(),"식사 종류를 선택해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                Meal meal = new Meal();

                String from = str1+" "+str2+":00";
                SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = fm.parse(from, new ParsePosition(0));
                meal.time = date.getTime();

                meal.type = btn_on;
                if(kcal.getText().toString()!=null)
                    meal.kcal= Integer.parseInt(kcal.getText().toString());
                if (memo.getText().toString()!=null)
                    meal.memo=memo.getText().toString();

                Toast.makeText(getApplicationContext(),"입력이 완료 되었습니다", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("MEAL", meal);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(getApplicationContext(),"입력이 취소되었습니다",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void findViewById(){
        actionBar =  this.getSupportActionBar();
        actionBar.setTitle("식사 정보 추가");

        check=findViewById(R.id.add_meal_check);
        cancel=findViewById(R.id.add_meal_cancel);
        memo=findViewById(R.id.add_meal_memo);
        kcal=findViewById(R.id.kcal);
        radioGroup=findViewById(R.id.radioGroup);
        date_pick=findViewById(R.id.date_picker);
        time_pick=findViewById(R.id.time_picker);
        tv_date=findViewById(R.id.date_view);
        tv_time=findViewById(R.id.time_view);
        rbtn1=findViewById(R.id.breakfast);
        rbtn2=findViewById(R.id.lunch);
        rbtn3=findViewById(R.id.dinner);
        rbtn4=findViewById(R.id.eating);
    }

    RadioButton.OnClickListener radioButtonClickListener = new RadioButton.OnClickListener(){
        @Override public void onClick(View view) {
        }
    };

    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            switch (i) {
                case R.id.breakfast:
                    btn_on =1;
                    break;
                case R.id.lunch:
                    btn_on =2;
                    break;
                case R.id.dinner:
                    btn_on =3;
                    break;
                case R.id.eating:
                    btn_on=4;
                    break;
                default:
                    btn_on=0;
            }
        }
    };
    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DATE:
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month++;
                        if(month>=10)
                            tv_date.setText(year+"-"+month+"-"+dayOfMonth);
                        else
                            tv_date.setText(year+"-"+0+month+"-"+dayOfMonth);
                    }
                },2020,0,1);
                return datePickerDialog;
            case DIALOG_TIME :
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if(hourOfDay<10) {
                            if(minute<10)
                               tv_time.setText("0" + hourOfDay + ":" + 0 + minute);
                            else
                                tv_time.setText("0" + hourOfDay + ":" + minute);
                        }else {
                            if(minute<10)
                                tv_time.setText(hourOfDay + ":" + 0 + minute);
                            else
                                tv_time.setText(hourOfDay + ":" + minute);
                        }
                    }
                },0,0,false);
                return  timePickerDialog;
        }
        return super.onCreateDialog(id);
    }
}
