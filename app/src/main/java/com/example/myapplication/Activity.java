package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class Activity implements Parcelable {
    public long time;
    public String act;
    public int play_min;
    public String memo;

    Activity(){
        time = -1;
        act = null;
        play_min=-1;
        memo = null;
    }

    protected Activity(Parcel in){
        time=in.readLong();
        act=in.readString();
        play_min = in.readInt();
        memo=in.readString();
    }

    public static final Creator<Activity> CREATOR = new Creator<Activity>() {
        @Override
        public Activity createFromParcel(Parcel in){
            return new Activity(in);
        }

        @Override
        public  Activity[] newArray(int size){
            return  new Activity[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(time);
        out.writeString(act);
        out.writeInt(play_min);
        out.writeString(memo);
    }
}
