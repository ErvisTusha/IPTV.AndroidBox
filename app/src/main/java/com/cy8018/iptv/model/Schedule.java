package com.cy8018.iptv.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class Schedule implements Parcelable {

    public int index;

    @SerializedName("channel_id") public String channelId;
    @SerializedName("program_name") public String programName;
    @SerializedName("start_time") public String startTime;
    @SerializedName("end_time") public String endTime;

    protected Schedule(Parcel in) {
        channelId = in.readString();
        programName = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        index = in.readInt();
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(channelId);
        parcel.writeString(programName);
        parcel.writeString(startTime);
        parcel.writeString(endTime);
        parcel.writeInt(index);
    }
}
