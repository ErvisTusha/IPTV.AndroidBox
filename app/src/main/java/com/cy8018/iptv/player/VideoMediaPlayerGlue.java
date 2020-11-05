/*
 * Copyright (C) 2016 The Android Open Source Project
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
 *
 */

package com.cy8018.iptv.player;

import android.app.Activity;
import android.net.TrafficStats;
import android.os.Handler;

import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.media.PlayerAdapter;
import androidx.leanback.widget.PlaybackRowPresenter;
import androidx.leanback.widget.Presenter;

import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.cy8018.iptv.R;
import com.cy8018.iptv.model.ScheduleDisplayInfo;
import com.cy8018.iptv.model.Station;

import org.jetbrains.annotations.NotNull;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlayerGlue for video playback
 * @param <T>
 */
public class VideoMediaPlayerGlue<T extends PlayerAdapter> extends PlaybackTransportControlGlue<T> {

    private Activity mContext;

    private Station currentStation;

    private String currentTime = "";

    private ScheduleDisplayInfo scheduleDisplayInfo;

    private String currentChannelId = "";

    private String targetChannelId = "";

    private long lastTotalRxBytes = 0;

    private long lastTimeStamp = 0;

    public List<Station> stationList;

    public static final int MSG_UPDATE_INFO = 0;

    public static String gNetworkSpeed = "";

    private static final String TAG = "VideoMediaPlayerGlue";

    public void setStationList(List<Station> list)
    {
        stationList = list;
    }

    public ScheduleDisplayInfo getScheduleDisplayInfo() {
        return scheduleDisplayInfo;
    }

    public void setCurrentChannelId(String currentChannelId)
    {
        this.currentChannelId = currentChannelId;
    }

    public void setTargetChannelId(String channelId)
    {
        this.targetChannelId = channelId;
    }

    public Station getCurrentStation() {
        return currentStation;
    }

    public Station getNextStation() {
        int index = currentStation.index + 1;
        if (index > stationList.size() - 1)
        {
            index = 0;
        }
        return stationList.get(index);
    }

    public Station getNextNextStation() {
        int index = currentStation.index + 2;
        if (index > stationList.size() - 1)
        {
            index = index - stationList.size() ;
        }
        return stationList.get(index);
    }

    public Station getPrevStation() {
        int index = currentStation.index - 1;
        if (index < 0)
        {
            index = stationList.size() - 1;
        }
        return stationList.get(index);
    }

    public Station getPrevPrevStation() {
        int index = currentStation.index - 2;
        if (index < 0)
        {
            index = stationList.size() + index;
        }
        return stationList.get(index);
    }

    public String getTargetChannelId() {
        return targetChannelId;
    }

    public void setScheduleInfo(ScheduleDisplayInfo scheduleDisplayInfo) {
        this.scheduleDisplayInfo = scheduleDisplayInfo;
    }

    public void setCurrentStation(Station currentStation) {
        this.currentStation = currentStation;
        this.currentChannelId = String.valueOf(currentStation.index + 1);
    }

    public VideoMediaPlayerGlue(Activity context, T impl) {
        super(context, impl);
        mContext = context;
    }

    @Override
    protected PlaybackRowPresenter onCreateRowPresenter() {
        PlayControlPresenter presenter = new PlayControlPresenter();
        presenter.setDescriptionPresenter(new MyDescriptionPresenter());
        return presenter;
    }

    private long getNetSpeed() {

        long nowTotalRxBytes = TrafficStats.getUidRxBytes(mContext.getApplicationContext().getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : TrafficStats.getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long calculationTime = (nowTimeStamp - lastTimeStamp);
        if (calculationTime == 0) {
            return calculationTime;
        }

        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / calculationTime);
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return speed;
    }

    public String getNetSpeedText(long speed) {
        String text = "";
        if (speed >= 0 && speed < 1024) {
            text = speed + " B/s";
        } else if (speed >= 1024 && speed < (1024 * 1024)) {
            text = speed / 1024 + " KB/s";
        } else if (speed >= (1024 * 1024) && speed < (1024 * 1024 * 1024)) {
            text = speed / (1024 * 1024) + " MB/s";
        }
        return text;
    }

    public String getCurrentTimeString()
    {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String dateNowStr = sdf.format(date);
        return  dateNowStr;
    }

    public void getNetSpeedInfo() {
        gNetworkSpeed = getNetSpeedText(getNetSpeed());
        Log.d(TAG, gNetworkSpeed);
    }

    public static boolean isContainChinese(String str) {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    private class MyDescriptionPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tv_info, parent, false);

            View infoBarView = view.findViewById(R.id.tv_info_bar);
            View channelIdView = view.findViewById(R.id.channel_id_bg);
            View channelListBar = view.findViewById(R.id.channel_list_bar);
            View currentChannelBar = view.findViewById(R.id.current_channel_bar);

            //logo.setBackgroundColor(Color.DKGRAY);
            infoBarView.getBackground().setAlpha(0);
            currentChannelBar.getBackground().setAlpha(180);
            channelIdView.getBackground().setAlpha(100);
            channelListBar.getBackground().setAlpha(100);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {

            VideoMediaPlayerGlue glue = (VideoMediaPlayerGlue) item;

            ((ViewHolder)viewHolder).sourceInfo.setText(glue.getSubtitle());
            ((ViewHolder)viewHolder).currentTime.setText(currentTime);
            ((ViewHolder)viewHolder).channelId.setText(currentChannelId);
            ((ViewHolder)viewHolder).targetChannelId.setText(targetChannelId);

            String channelNameString = glue.getTitle().toString();
            if (channelNameString != ((ViewHolder)viewHolder).channelName.getText())
            {
                ((ViewHolder)viewHolder).channelName.setText(channelNameString);
                ((ViewHolder)viewHolder).currentChannelId.setText(String.valueOf(glue.getCurrentStation().index + 1));

                ((ViewHolder)viewHolder).currentChannelName.setText(channelNameString);
                ((ViewHolder)viewHolder).nextChannelId.setText(String.valueOf(glue.getNextStation().index + 1));
                ((ViewHolder)viewHolder).nextChannelName.setText(glue.getNextStation().name);
                ((ViewHolder)viewHolder).nextNextChannelId.setText(String.valueOf(glue.getNextNextStation().index + 1));
                ((ViewHolder)viewHolder).nextNextChannelName.setText(glue.getNextNextStation().name);
                ((ViewHolder)viewHolder).prevChannelId.setText(String.valueOf(glue.getPrevStation().index + 1));
                ((ViewHolder)viewHolder).prevChannelName.setText(glue.getPrevStation().name);
                ((ViewHolder)viewHolder).prevPrevChannelId.setText(String.valueOf(glue.getPrevPrevStation().index + 1));
                ((ViewHolder)viewHolder).prevPrevChannelName.setText(glue.getPrevPrevStation().name);
            }

            Glide.with(getContext())
                    .asBitmap()
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10)))
                    .load(glue.getPrevPrevStation().logo)
                    .into(((ViewHolder)viewHolder).prevPrevChannelLogo);

            Glide.with(getContext())
                    .asBitmap()
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10)))
                    .load(glue.getPrevStation().logo)
                    .into(((ViewHolder)viewHolder).prevChannelLogo);

            Glide.with(getContext())
                    .asBitmap()
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10)))
                    .load(glue.getCurrentStation().logo)
                    .into(((ViewHolder)viewHolder).currentChannelLogo);

            Glide.with(getContext())
                    .asBitmap()
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10)))
                    .load(glue.getNextStation().logo)
                    .into(((ViewHolder)viewHolder).nextChannelLogo);

            Glide.with(getContext())
                    .asBitmap()
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10)))
                    .load(glue.getNextNextStation().logo)
                    .into(((ViewHolder)viewHolder).nextNextChannelLogo);

            Glide.with(getContext())
                    .asBitmap()
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10)))
                    .load(glue.getCurrentStation().logo)
                    .into(((ViewHolder)viewHolder).logo);
        }

        @Override
        public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

        }

        class ViewHolder extends Presenter.ViewHolder {

            TextView channelId;
            TextView targetChannelId;
            TextView currentTime;
            TextView channelName;
            TextView sourceInfo;
            TextView networkSpeed;
            TextView currentProgramName;
            TextView currentProgramTime;
            TextView nextProgramName;
            TextView nextProgramTime;
            TextView thirdProgramName;
            TextView thirdProgramTime;

            TextView currentChannelId;
            TextView prevChannelId;
            TextView prevPrevChannelId;
            TextView nextChannelId;
            TextView nextNextChannelId;
            TextView currentChannelName;
            TextView prevChannelName;
            TextView prevPrevChannelName;
            TextView nextChannelName;
            TextView nextNextChannelName;

            LinearLayout scheduleInfoBar;
            ImageView logo;
            ImageView prevPrevChannelLogo;
            ImageView prevChannelLogo;
            ImageView currentChannelLogo;
            ImageView nextChannelLogo;
            ImageView nextNextChannelLogo;

            private ViewHolder (View itemView)
            {
                super(itemView);
                channelId = itemView.findViewById(R.id.channel_id);
                targetChannelId = itemView.findViewById(R.id.target_channel_id);
                currentTime = itemView.findViewById(R.id.current_time);
                channelName = itemView.findViewById(R.id.channel_name);
                sourceInfo = itemView.findViewById(R.id.source_info);
                networkSpeed = itemView.findViewById(R.id.network_speed);
                currentProgramName = itemView.findViewById(R.id.current_program_name);
                currentProgramTime = itemView.findViewById(R.id.current_program_time);
                nextProgramName = itemView.findViewById(R.id.next_program_name);
                nextProgramTime = itemView.findViewById(R.id.next_program_time);
                thirdProgramName = itemView.findViewById(R.id.third_program_name);
                thirdProgramTime = itemView.findViewById(R.id.third_program_time);
                scheduleInfoBar = itemView.findViewById(R.id.schedule_info);

                currentChannelId = itemView.findViewById(R.id.current_channel_id);
                currentChannelName = itemView.findViewById(R.id.current_channel_name);
                nextChannelId = itemView.findViewById(R.id.next_channel_id);
                nextChannelName = itemView.findViewById(R.id.next_channel_name);
                nextNextChannelId = itemView.findViewById(R.id.next_next_channel_id);
                nextNextChannelName = itemView.findViewById(R.id.next_next_channel_name);
                prevChannelId = itemView.findViewById(R.id.prev_channel_id);
                prevChannelName = itemView.findViewById(R.id.prev_channel_name);
                prevPrevChannelId = itemView.findViewById(R.id.prev_prev_channel_id);
                prevPrevChannelName = itemView.findViewById(R.id.prev_prev_channel_name);

                logo = itemView.findViewById(R.id.channel_logo);
                prevPrevChannelLogo = itemView.findViewById(R.id.prev_prev_channel_logo);
                prevChannelLogo = itemView.findViewById(R.id.prev_channel_logo);
                currentChannelLogo = itemView.findViewById(R.id.current_channel_logo);
                nextChannelLogo = itemView.findViewById(R.id.next_channel_logo);
                nextNextChannelLogo = itemView.findViewById(R.id.next_next_channel_logo);

                currentChannelName.setSelected(true);
                currentProgramName.setSelected(true);
                nextProgramName.setSelected(true);
                thirdProgramName.setSelected(true);
                channelName.setSelected(true);

                new Thread(updateInfoRunnable).start();
            }

            public void UpdateDisplayInfo() {
                networkSpeed.setText(gNetworkSpeed);
                currentTime.setText(getCurrentTimeString());
                targetChannelId.setText(getTargetChannelId());
                if (currentProgramName.getText() != getScheduleDisplayInfo().currentProgramName)
                {
                    currentProgramName.setText(getScheduleDisplayInfo().currentProgramName);
                }
                if (currentProgramTime.getText() != getScheduleDisplayInfo().currentProgramTime)
                {
                    currentProgramTime.setText(getScheduleDisplayInfo().currentProgramTime);
                }
                if (nextProgramName.getText() != getScheduleDisplayInfo().nextProgramName)
                {
                    nextProgramName.setText(getScheduleDisplayInfo().nextProgramName);
                }
                if (nextProgramTime.getText() != getScheduleDisplayInfo().nextProgramTime)
                {
                    nextProgramTime.setText(getScheduleDisplayInfo().nextProgramTime);
                }
                if (thirdProgramName.getText() != getScheduleDisplayInfo().thirdProgramName)
                {
                    thirdProgramName.setText(getScheduleDisplayInfo().thirdProgramName);
                }
                if (thirdProgramTime.getText() != getScheduleDisplayInfo().thirdProgramTime)
                {
                    thirdProgramTime.setText(getScheduleDisplayInfo().thirdProgramTime);
                }

                if (getScheduleDisplayInfo() == null
                        || getScheduleDisplayInfo().currentProgramName == null
                        || getScheduleDisplayInfo().currentProgramName.length() == 0)
                {
                    ViewGroup.LayoutParams layoutParams = scheduleInfoBar.getLayoutParams();
                    layoutParams.height = 0;
                    layoutParams.width = 0;
                    scheduleInfoBar.setLayoutParams(layoutParams);
                    scheduleInfoBar.setVisibility(View.INVISIBLE);
                }
                else
                {
                    ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);;
                    scheduleInfoBar.setLayoutParams(layoutParams);
                    scheduleInfoBar.setVisibility(View.VISIBLE);
                }

            }

            public final MsgHandler mHandler = new MsgHandler(this);

            public class MsgHandler extends Handler {
                WeakReference<ViewHolder> mViewHolder;

                MsgHandler(ViewHolder viewHolder) {
                    mViewHolder = new WeakReference<ViewHolder>(viewHolder);
                }

                @Override
                public void handleMessage(@NotNull Message msg) {
                    super.handleMessage(msg);

                    ViewHolder vh = mViewHolder.get();
                    if (msg.what == MSG_UPDATE_INFO) {
                        getNetSpeedInfo();
                        vh.UpdateDisplayInfo();
                    }
                }
            }

            Runnable updateInfoRunnable = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            mHandler.sendEmptyMessage(MSG_UPDATE_INFO);
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        }
    }
}