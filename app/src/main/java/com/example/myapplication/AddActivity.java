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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private Button check, cancel, date_pick, time_pick;
    private EditText activity, memo;
    private TextView tv_date, tv_time;
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
        setContentView(R.layout.activity_add_activity);

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


        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str1 = tv_date.getText().toString();
                String str2 = tv_time.getText().toString();

                if (str1 == null || str2==null){
                    Toast.makeText(getApplicationContext(), "날짜와 시간을 선택해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(activity.getText()==null){
                    Toast.makeText(getApplicationContext(),"활동을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                Activity active = new Activity();

                String from = str1+" "+str2+":00";
                SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = fm.parse(from, new ParsePosition(0));
                active.time = date.getTime();

                if(activity.getText().toString()!=null)
                    active.act= activity.getText().toString();
                if (memo.getText().toString()!=null)
                    active.memo=memo.getText().toString();

                Toast.makeText(getApplicationContext(),"입력이 완료 되었습니다", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("ACTIVE", active);
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


    private void findViewById(){
        actionBar =  this.getSupportActionBar();
        actionBar.setTitle("활동 정보 추가");

        check=findViewById(R.id.add_activity_check);
        cancel=findViewById(R.id.add_activity_cancel);
        memo=findViewById(R.id.text_memo);
        activity=findViewById(R.id.text_activity);
        date_pick=findViewById(R.id.bt_date);
        time_pick=findViewById(R.id.bt_time);
        tv_date=findViewById(R.id.text_date);
        tv_time=findViewById(R.id.text_time);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(getApplicationContext(),"입력이 취소되었습니다",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }


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
