package com.cy8018.iptv.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity(tableName = "schedules")
public class ScheduleData {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "channel_id")
    public String channelId;

    @ColumnInfo(name = "program_name")
    public String programName;

    @ColumnInfo(name = "start_time")
    public Date startTime;

    @ColumnInfo(name = "end_time")
    public Date endTime;


}

