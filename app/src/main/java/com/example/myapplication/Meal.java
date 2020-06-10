package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class Meal implements Parcelable {
    public long time;
    public int type;
    public float kcal;
    public String memo;

    Meal(){
        time = -1;
        type = -1;
        kcal = -1;
        memo = null;
    }

    Meal(long Time, int Type, float Kcal, String Memo){
        time=Time;
        type=Type;
        kcal=Kcal;
        memo=Memo;
    }

    protected Meal(Parcel in) {
        kcal = in.readFloat();
        time =in.readLong();
        type = in.readInt();
        memo = in.readString();
    }


    public static final Creator<Meal> CREATOR = new Creator<Meal>() {
        @Override
        public Meal createFromParcel(Parcel in){
            return new Meal(in);
        }

        @Override
        public  Meal[] newArray(int size){
            return  new Meal[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(kcal);
        out.writeLong(time);
        out.writeInt(type);
        out.writeString(memo);
    }
}
