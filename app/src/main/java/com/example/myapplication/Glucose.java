package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class Glucose implements Parcelable {

    public float glucose;
    public long timestamp;
    public int state;

    Glucose(){
        glucose = -1;
        timestamp = -1;
        state = -1;
    }

    Glucose(float g, long time, int s){
        glucose =g;
        timestamp = time;
        state = s;
    }

    protected Glucose(Parcel in) {
        glucose = in.readFloat();
        timestamp =in.readLong();
        state = in.readInt();
    }

    public static final Creator<Glucose> CREATOR = new Creator<Glucose>() {
        @Override
        public Glucose createFromParcel(Parcel in){
            return new Glucose(in);
        }

        @Override
        public  Glucose[] newArray(int size){
            return  new Glucose[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(glucose);
        out.writeLong(timestamp);
        out.writeInt(state);
    }
}
