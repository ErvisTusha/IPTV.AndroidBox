/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cy8018.iptv.player;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.leanback.media.PlaybackGlue;

import com.cy8018.iptv.database.AppDatabase;
import com.cy8018.iptv.database.ScheduleData;
import com.cy8018.iptv.database.StationData;
import com.cy8018.iptv.model.ScheduleDisplayInfo;
import com.cy8018.iptv.model.Station;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Handles video playback with media controls.
 */
public class PlaybackVideoFragment extends MyVideoSupportFragment {

    private VideoMediaPlayerGlue<ExoPlayerAdapter> mMediaPlayerGlue;
    final MyVideoSupportFragmentGlueHost mHost = new MyVideoSupportFragmentGlueHost(this);
    private Station currentStation = null;
    private ArrayList<Station> mStationList;
    private List<ScheduleData> mScheduleDataList;

    private int currentSourceIndex = 0;

    private String targetChannelId = "";

    // message to show the control overlay
    public static final int MSG_SHOW_CONTROL = 0;

    // message to hide the control overlay
    public static final int MSG_HIDE_CONTROL = 1;

    public static final int MSG_SWITCH_CHANNEL = 2;

    public static final float CONTROL_OVERLAY_FADE_TIME = 5.5f;

    public static final float SWITCH_CHANNEL_DELAY_TIME = 1.0f;

    public static long lastActiveTimeStamp = 0;

    public static boolean isCheckerRunning = false;

    public final MsgHandler mHandler = new MsgHandler(this);

    private static final String TAG = "PlaybackVideoFragment";

    private void setTargetChannelId (String channelId)
    {
        targetChannelId = channelId;
        mMediaPlayerGlue.setTargetChannelId(targetChannelId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setBackgroundType(BG_LIGHT);

        setBackgroundType(BG_NONE);
        ExoPlayerAdapter playerAdapter = new ExoPlayerAdapter(getActivity());
        mMediaPlayerGlue = new VideoMediaPlayerGlue(getActivity(), playerAdapter);
        mMediaPlayerGlue.setHost(mHost);

        mMediaPlayerGlue.setSeekEnabled(false);
        mMediaPlayerGlue.setControlsOverlayAutoHideEnabled(true);

        mStationList = getActivity().getIntent().getParcelableArrayListExtra("stationList");
        currentStation = getActivity().getIntent().getParcelableExtra("currentStation");

        int sourceIndex = 0;
        StationData stationData = AppDatabase.getInstance(getActivity()).stationDao().findByName(currentStation.name);

        //mScheduleDataList = AppDatabase.getInstance(getActivity()).scheduleDao().getAll();
        mScheduleDataList = AppDatabase.getInstance(getActivity()).scheduleDao().getAllByChannelId(currentStation.id);

        if (stationData!= null && stationData.lastSource > 0) {
            sourceIndex = stationData.lastSource;
        }

        mMediaPlayerGlue.setTitle(currentStation.name);
        mMediaPlayerGlue.setSubtitle(sourceIndex + 1 +"/" + currentStation.url.size());
        mMediaPlayerGlue.getPlayerAdapter().setDataSource(Uri.parse(currentStation.url.get(sourceIndex)));

        mMediaPlayerGlue.setCurrentStation(currentStation);

        mMediaPlayerGlue.setScheduleInfo(getScheduleDisplayInfo());

        mMediaPlayerGlue.playWhenPrepared();

        SetLastActiveTime();
        isCheckerRunning = true;
        new Thread(controlOverlayCheckRunnable).start();
    }

    @Override
    public void onStop() {
        isCheckerRunning = false;
        super.onStop();
    }

    private ScheduleData getNextProgram(ScheduleData currentProgram)
    {
        Date nextTime = new Date();
        nextTime.setTime(currentProgram.endTime.getTime() + 60000);
        for (ScheduleData scheduleData : mScheduleDataList)
        {
            if (isTimeInBetween(nextTime, scheduleData.startTime, scheduleData.endTime))
            {
                return scheduleData;
            }
        }
        return null;
    }

    private ScheduleDisplayInfo getScheduleDisplayInfo()
    {
        ScheduleDisplayInfo scheduleDisplayInfo = new ScheduleDisplayInfo();

        Date nowTime = new Date();
        int index = 0;
        for (ScheduleData scheduleData : mScheduleDataList)
        {
            index++;
            if (isTimeInBetween(nowTime, scheduleData.startTime, scheduleData.endTime))
            {
                Format formatter = new SimpleDateFormat("HH:mm");

                scheduleDisplayInfo.currentProgramTime = formatter.format(scheduleData.startTime);
                scheduleDisplayInfo.currentProgramName = scheduleData.programName;

                ScheduleData nextScheduleData = getNextProgram(scheduleData);

                if (nextScheduleData == null)
                {
                    scheduleDisplayInfo.nextProgramName = "";
                    scheduleDisplayInfo.nextProgramTime = "";
                }
                else
                {
                    scheduleDisplayInfo.nextProgramTime = formatter.format(nextScheduleData.startTime);
                    scheduleDisplayInfo.nextProgramName = nextScheduleData.programName;
                }
                break;
            }
        }
        return scheduleDisplayInfo;
    }

    private boolean isTimeInBetween(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    static void playWhenReady(VideoMediaPlayerGlue glue) {
        if (glue.isPrepared()) {
            glue.play();
        } else {
            glue.addPlayerCallback(new VideoMediaPlayerGlue.PlayerCallback() {
                @Override
                public void onPreparedStateChanged(PlaybackGlue glue) {
                    if (glue.isPrepared()) {
                        glue.removePlayerCallback(this);
                        glue.play();
                    }
                }
            });
        }
    }

    public void SwitchChannel(boolean isForward) {
        int index = 0;
        if (isForward)
        {
            index = currentStation.index + 1;
            if (index >= mStationList.size()) {
                index = 0;
            }
        }
        else {
            index = currentStation.index - 1;
            if (index < 0) {
                index = mStationList.size() - 1;
            }
        }

        currentStation = mStationList.get(index);

        mMediaPlayerGlue.setTitle(currentStation.name);

        int sourceIndex = 0;
        StationData stationData = AppDatabase.getInstance(getActivity()).stationDao().findByName(currentStation.name);

        if (stationData!= null && stationData.lastSource > 0) {
            sourceIndex = stationData.lastSource;
        }

        currentSourceIndex = sourceIndex;

        mMediaPlayerGlue.setSubtitle(currentSourceIndex + 1 +"/" + currentStation.url.size());
        mMediaPlayerGlue.getPlayerAdapter().setDataSource(Uri.parse(currentStation.url.get(currentSourceIndex)));

        mMediaPlayerGlue.setCurrentStation(currentStation);

        mScheduleDataList = AppDatabase.getInstance(getActivity()).scheduleDao().getAllByChannelId(currentStation.id);
        mMediaPlayerGlue.setScheduleInfo(getScheduleDisplayInfo());

        Log.d(TAG, "SwitchChanel: "+ currentStation.name);

        playWhenReady(mMediaPlayerGlue);
    }

    public void SwitchChannelById(String channelId) {
        int index = Integer.parseInt(channelId) - 1;

        if (index < 0 || index > mStationList.size())
        {
            Log.d(TAG, "SwitchChannelById index error, index: " + index);
            return;
        }

        currentStation = mStationList.get(index);
        mMediaPlayerGlue.setTitle(currentStation.name);
        mMediaPlayerGlue.setCurrentChannelId(channelId);

        int sourceIndex = 0;
        StationData stationData = AppDatabase.getInstance(getActivity()).stationDao().findByName(currentStation.name);

        if (stationData!= null && stationData.lastSource > 0) {
            sourceIndex = stationData.lastSource;
        }

        currentSourceIndex = sourceIndex;

        mMediaPlayerGlue.setSubtitle(currentSourceIndex + 1 +"/" + currentStation.url.size());
        mMediaPlayerGlue.getPlayerAdapter().setDataSource(Uri.parse(currentStation.url.get(currentSourceIndex)));

        mMediaPlayerGlue.setCurrentStation(currentStation);
        mScheduleDataList = AppDatabase.getInstance(getActivity()).scheduleDao().getAllByChannelId(currentStation.id);
        mMediaPlayerGlue.setScheduleInfo(getScheduleDisplayInfo());

        Log.d(TAG, "SwitchChanelById: "+ currentStation.name);

        playWhenReady(mMediaPlayerGlue);

        ShowControl();
    }

    public void SwitchSource(boolean isForward) {
        int index = 0;
        if (isForward)
        {
            index = currentSourceIndex + 1;
            if (index >= currentStation.url.size()) {
                index = 0;
            }
        }
        else {
            index = currentSourceIndex - 1;
            if (index < 0) {
                index = currentStation.url.size() - 1;
            }
        }

        currentSourceIndex = index;
        mMediaPlayerGlue.setTitle(currentStation.name);
        String sourceInfo = index + 1 + "/" + currentStation.url.size();
        mMediaPlayerGlue.setSubtitle(sourceInfo);
        mMediaPlayerGlue.getPlayerAdapter().setDataSource(Uri.parse(currentStation.url.get(index)));

        if (AppDatabase.getInstance(getActivity()).stationDao().findByName(currentStation.name) != null) {
            AppDatabase.getInstance(getActivity()).stationDao().setLastSource(currentStation.name, currentSourceIndex);
        }
        else {
            StationData station = new StationData();
            station.stationName = currentStation.name;
            station.lastSource = currentSourceIndex;
            station.isFavorite = false;
            AppDatabase.getInstance(getActivity()).stationDao().insert(station);
        }

        Log.d(TAG, "SwitchSource: "+ currentStation.name + " (" + sourceInfo + ")");

        playWhenReady(mMediaPlayerGlue);
    }

    public void ShowControl() {
        showControlsOverlay(true);
        SetLastActiveTime();
    }

    public void SetTargetChannelId(int id) {
        targetChannelId += Integer.toString(id);
        setTargetChannelId(targetChannelId);
        Log.d(TAG, "SetTargetChannelId: " + targetChannelId);
//        if (targetChannelId.length() > 1) {
//            ConfirmTargetChannelId();
//        }
    }

    public void ConfirmTargetChannelId() {
        if (targetChannelId != null && targetChannelId.length() > 0)
        {
            SwitchChannelById(targetChannelId);
            Log.d(TAG, "ConfirmTargetChannelId: " + targetChannelId);
            setTargetChannelId("");
        }
    }

    public void SetLastActiveTime() {
        lastActiveTimeStamp = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayerGlue != null) {
            mMediaPlayerGlue.pause();
        }
    }

    public static class MsgHandler extends Handler {
        WeakReference<PlaybackVideoFragment> mPlaybackVideoFragment;

        MsgHandler(PlaybackVideoFragment playbackVideoFragment) {
            mPlaybackVideoFragment = new WeakReference<>(playbackVideoFragment);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            super.handleMessage(msg);

            PlaybackVideoFragment playbackVideoFragment = mPlaybackVideoFragment.get();

            if (msg.what == MSG_SHOW_CONTROL) {
                if (!playbackVideoFragment.isControlsOverlayVisible())
                {
                    playbackVideoFragment.showControlsOverlay(true);
                }
            }
            else if (msg.what == MSG_HIDE_CONTROL) {
                if (playbackVideoFragment.isControlsOverlayVisible() && playbackVideoFragment.mMediaPlayerGlue.isPlaying()) {
                    playbackVideoFragment.hideControlsOverlay(true);
                }
            }
            else if (msg.what == MSG_SWITCH_CHANNEL) {
                playbackVideoFragment.ConfirmTargetChannelId();
            }
        }
    }

    Runnable controlOverlayCheckRunnable = new Runnable() {
        @Override
        public void run() {
            while (isCheckerRunning) {
                try {
                    long nowTimeStamp = System.currentTimeMillis();
                    long timeDiff = (nowTimeStamp - lastActiveTimeStamp);
                    //Log.d(TAG, "time diff: " + timeDiff);
                    if (timeDiff > CONTROL_OVERLAY_FADE_TIME * 1000) {
                        mHandler.sendEmptyMessage(MSG_HIDE_CONTROL);
                    }
                    if (timeDiff > SWITCH_CHANNEL_DELAY_TIME * 1000) {
                        mHandler.sendEmptyMessage(MSG_SWITCH_CHANNEL);
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}