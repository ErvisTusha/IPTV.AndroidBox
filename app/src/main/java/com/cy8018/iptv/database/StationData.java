package com.cy8018.iptv.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stations")
public class StationData {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "channel_code")
    public String channelCode;

    @ColumnInfo(name = "station_name")
    public String stationName;

    @ColumnInfo(name = "last_source")
    public int lastSource;

    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;
}
