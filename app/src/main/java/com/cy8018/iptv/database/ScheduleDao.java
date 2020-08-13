package com.cy8018.iptv.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleDao {
    @Query("SELECT * FROM schedules")
    List<ScheduleData> getAll();

    @Query("SELECT * FROM schedules WHERE start_time >= datetime('now','start of day','+0 day') and start_time < datetime('now','start of day','+1 day')")
    List<ScheduleData> getAllToday();

    @Query("SELECT * FROM schedules WHERE channel_id = :channelId")
    List<ScheduleData> getAllByChannelId(String channelId);

    @Query("SELECT * FROM schedules WHERE start_time >=datetime('now','start of day','+1 day') and start_time < datetime('now','start of day','+2 day')")
    List<ScheduleData> getAllTomorrow();

    @Query("SELECT * FROM schedules WHERE channel_id = :channelId and start_time >=datetime('now','start of day','+0 day') and start_time < datetime('now','start of day','+1 day')")
    List<ScheduleData> getAllByChannelIdToday(String channelId);

    @Query("DELETE FROM schedules WHERE start_time < datetime('now','start of day','+0 day')")
    void removeOldData();

    @Query("DELETE FROM schedules")
    void removeAll();

    @Insert
    void insert(ScheduleData schedule);

    @Insert
    void insertAll(ScheduleData... schedules);

    @Delete
    void delete(ScheduleData schedule);
}
