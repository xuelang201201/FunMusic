package com.charles.funmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.charles.funmusic.constant.Actions;
import com.charles.funmusic.service.PlayService;

/**
 * 来电/耳机拔出时暂停播放
 */
public class NoisyAudioStreamReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PlayService.startCommand(context, Actions.ACTION_MEDIA_PLAY_PAUSE);
    }
}
