package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<User> UserList = new ArrayList<User>();

    private ActionBar actionBar;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    public Frag_Home frag1;
    public Frag_Chart frag2;
    public Frag_None frag3;
    public Frag_Setting frag4;
    public Bundle bundle;
    private Menu menu;
    public SharedPref sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = new SharedPref(this);

        if (sp.loadNightModeState())
        {
            setTheme(R.style.DarkTheme);
        }
        else
        {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = this.getSupportActionBar();

        readDataFromCsv();

        bundle = new Bundle();
        bundle.putParcelable("userlist",UserList.get(20));

        build_fragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_bar_plus:
                update(1);
                break;
            case R.id.action_bar_minus:
                update(2);
//            case R.id.action_bar_nextuser:
//                update(3);
        }
        return super.onOptionsItemSelected(item);
    }

    private void build_fragment(){
        bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        setFrag(0);
                        break;
                    case R.id.action_chart:
                        setFrag(1);
                        break;
//                    case R.id.action_none:
//                        setFrag(2);
//                        break;
                    case R.id.action_setting:
                        setFrag(3);
                        break;
                }
                return true;
            }
        });

        frag1=new Frag_Home();
        frag2=new Frag_Chart();
//        frag3=new Frag_None();
        frag4=new Frag_Setting();
        setFrag(0);
    }


    private void setFrag(int n)
    {
        fm = getSupportFragmentManager();
        ft= fm.beginTransaction();

        switch (n)
        {
            case 0:
                ft.replace(R.id.Main_Frame,frag1);
                frag1.setArguments(bundle);
                ft.commit();
                break;

            case 1:
                ft.replace(R.id.Main_Frame,frag2);
                frag2.setArguments(bundle);
                ft.commit();
                break;

//            case 2:
//                ft.replace(R.id.Main_Frame,frag3);
//                ft.commit();
//                break;
            case 3:
                ft.replace(R.id.Main_Frame,frag4);
                ft.commit();
                break;
        }
    }

    public void readDataFromCsv(){

        boolean first = true;

        try {
            InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.cgmseries));
            BufferedReader br = new BufferedReader(is);
            CSVReader reader = new CSVReader(br);
            String [] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                if(first){
                    first = false;
                    continue;
                }

                User u = new User();

                long now = new Date().getTime();
                int size = nextLine.length;

                for (int i = 0; i < size; i++) {
                    if(nextLine[i].length()==0)
                        break;

                    Glucose g = new Glucose();
                    g.glucose = Integer.valueOf(nextLine[i]);

                    g.timestamp =  get_time(now, i, size);
                    u.addDATA(g);
                }
                UserList.add(u);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long get_time(final long now, int ago, int size){
        final long PERIOD = 300_000; // 5 minutes
        ago = (size-ago);

        long time = now - (PERIOD*ago);

        return time;
    }


    public void show() {
        if(frag1.status==-1 || frag1.status==1)
            return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.drawable.blood);
        if(frag1.status ==1) {
            builder.setContentTitle("고혈당 알림");
            builder.setContentText("현재 혈당: "+frag1.BG_Text.getText()+frag1.type+" 혈당이 높습니다.");
        }

        if(frag1.status == -1){
            builder.setContentTitle("저혈당 알림");
            builder.setContentText("현재 혈당: "+frag1.BG_Text.getText()+frag1.type+" 혈당이 낮습니다.");
        }

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentIntent(pendingIntent);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.blood);
        builder.setLargeIcon(largeIcon);
        builder.setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_LOW));
            manager.getNotificationChannel("default").setVibrationPattern(new long[]{ 0 });
            manager.getNotificationChannel("default").enableVibration(true);
        }
        manager.notify(1,builder.build());
    }


    public void hide()
    {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            manager.getNotificationChannel("default");
        }
        manager.cancel(1);
    }

    public  void showOptionMenu(boolean isShow){
        if(menu == null)
            return;

        MenuItem item1 = menu.findItem(R.id.action_bar_plus);
        MenuItem item2 = menu.findItem(R.id.action_bar_minus);

        item1.setVisible(isShow);
        item2.setVisible(isShow);
    }

    private void update(int n){
        switch (n){
            case 1:
                frag1.add_dots();
                break;
            case 2:
                frag1.sub_dots();
                break;
//            case 3:
//                frag1.check_order();
//                break;
        }
        frag1.build_graph();
        frag1.update();

        if(frag4.high_alarm_switch==null || frag4.low_alarm_switch==null)
            return;

        if(frag4.high_alarm_switch.isChecked() || frag4.low_alarm_switch.isChecked())
            show();
    }
}
