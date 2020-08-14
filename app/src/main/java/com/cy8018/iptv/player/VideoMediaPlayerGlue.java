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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlayerGlue for video playback
 * @param <T>
 */
public class VideoMediaPlayerGlue<T extends PlayerAdapter> extends PlaybackTransportControlGlue<T> {

    private Station currentStation;

    private String currentTime = "";

    private String currentProgramName = "";
    private String currentProgramTime = "";

    private ScheduleDisplayInfo scheduleDisplayInfo;

    private String currentChannelId = "";

    private String targetChannelId = "";

    private long lastTotalRxBytes = 0;

    private long lastTimeStamp = 0;

    public static final int MSG_UPDATE_INFO = 0;

    public static String gNetworkSpeed = "";

    private static final String TAG = "VideoMediaPlayerGlue";

    Activity mContext;

    public void setCurrentTime (String time) { this.currentTime = time; }

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
            View channelIdBg = view.findViewById(R.id.channel_id_bg);

            //logo.setBackgroundColor(Color.DKGRAY);
            infoBarView.getBackground().setAlpha(0);
            channelIdBg.getBackground().setAlpha(100);

//            TextView channelNameTextView = view.findViewById(R.id.channel_name);
//            if (channelNameTextView.getText().length()>4)
//            {
//                channelNameTextView.setTextSize(60);
//            }

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {

            VideoMediaPlayerGlue glue = (VideoMediaPlayerGlue) item;
            String channelNameString = glue.getTitle().toString();
            if (channelNameString != ((ViewHolder)viewHolder).channelName.getText())
            {
                ((ViewHolder)viewHolder).channelName.setText(channelNameString);
            }

//            if ((channelNameString.length() > 4 && isContainChinese(channelNameString))
//                || channelNameString.length() > 10
//            )
//            {
//                ((ViewHolder)viewHolder).channelName.setTextSize(60);
//            }
//            else
//            {
//                ((ViewHolder)viewHolder).channelName.setTextSize(78);
//            }

            ((ViewHolder)viewHolder).sourceInfo.setText(glue.getSubtitle());
            ((ViewHolder)viewHolder).currentTime.setText(currentTime);
            ((ViewHolder)viewHolder).channelId.setText(currentChannelId);
            ((ViewHolder)viewHolder).targetChannelId.setText(targetChannelId);

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
            LinearLayout scheduleInfoBar;
            ImageView logo;

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

                logo = itemView.findViewById(R.id.channel_logo);

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