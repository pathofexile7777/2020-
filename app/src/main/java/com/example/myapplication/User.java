package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User implements Parcelable {
    private List<Glucose> DATA;
    private List<Meal> MEAL;
    private List<Activity> ACTIVITY;
    private int age;

    public User(){
        DATA= new ArrayList<Glucose>();
        MEAL=new ArrayList<Meal>();
        ACTIVITY=new ArrayList<Activity>();
        age = -1;
    }

    protected User(Parcel in) {
        in.readTypedList(DATA,Glucose.CREATOR);
        in.readTypedList(MEAL,Meal.CREATOR);
        in.readTypedList(ACTIVITY,Activity.CREATOR);
        age = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in){
            return new User(in);
        }

        @Override
        public  User[] newArray(int size){
            return  new User[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(DATA);
        out.writeTypedList(MEAL);
        out.writeTypedList(ACTIVITY);
        out.writeInt(age);
    }

    public List<Glucose> getDATA(){
        return DATA;
    }

    public void addDATA(Glucose g){
        DATA.add(g);
    }

    public Glucose lastbg(){
        int last_index = DATA.size();
        return DATA.get(last_index-1);
    }

    public List<Meal> getMEAL(){
        return MEAL;
    }

    public void addMEAL(Meal m){
        MEAL.add(m);
    }

    public List<Activity> getACTIVITY(){
        return ACTIVITY;
    }

    public void addACTIVITY(Activity a){
        ACTIVITY.add(a);
    }

    public int getAge(){
        return age;
    }
    public void setAge(int num){
        age = num;
    }

}
