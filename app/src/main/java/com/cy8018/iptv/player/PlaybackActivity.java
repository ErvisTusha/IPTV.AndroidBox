/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;


/**
 * Loads {@link PlaybackVideoFragment}.
 */
public class PlaybackActivity extends FragmentActivity {

    private static final String TAG = "PlaybackActivity";

    private PlaybackVideoFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragment = new PlaybackVideoFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, mFragment)
                    .commit();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            Toast toast= Toast.makeText(getApplicationContext(), "KeyCode:  "+ event.getKeyCode(), Toast.LENGTH_LONG);
//            toast.show();
//        }

        if (!((event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && !mFragment.isControlsOverlayVisible())
                || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
        )) {

//            Context context = getApplicationContext();
//            CharSequence text = String.valueOf(event.getKeyCode());
//            int duration = Toast.LENGTH_SHORT;
//            Toast toast = Toast.makeText(context, text, duration);
//            toast.show();

            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
            {
                mFragment.ConfirmTargetChannelId();
            }
            else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_0)
            {
                mFragment.SetTargetChannelId(0);
            }
            else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_1)
            {
                mFragment.SetTargetChannelId(1);
            }
            else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_2)
            {
                mFragment.SetTargetChannelId(2);
            }
            else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_3)
            {
                mFragment.SetTargetChannelId(3);
            }
            else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_4)
            {
                mFragment.SetTargetChannelId(4);
            }
            else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_5)
            {
                mFragment.SetTargetChannelId(5);
            }
            else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_6)
            {
                mFragment.SetTargetChannelId(6);
            }
            else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_7)
            {
                mFragment.SetTargetChannelId(7);
            }
            else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_8)
            {
                mFragment.SetTargetChannelId(8);
            }
            else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_9)
            {
                mFragment.SetTargetChannelId(9);
            }
            mFragment.ShowControl();
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN
                &&  (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_CHANNEL_UP)) {
            mFragment.SwitchChannel(false);
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_CHANNEL_DOWN)) {
            mFragment.SwitchChannel(true);
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            mFragment.SwitchSource(false);
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            mFragment.SwitchSource(true);
            return true;
        }

        return super.dispatchKeyEvent(event);
    }
}