package com.github.androidserialport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

import com.github.androidutils.logger.Logger;

public class KeyEventAnalyzer extends BroadcastReceiver {
    Context context;
    int keyEventAction = 0;
    int keyEventCode = 0;

    private void sendKeyEvent() {
        Intent buttonPressedIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent event = new KeyEvent(keyEventAction, keyEventCode);
        buttonPressedIntent.putExtra(Intent.EXTRA_KEY_EVENT, event);
        context.sendOrderedBroadcast(buttonPressedIntent, null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String string = intent.getStringExtra(ReadingService.EXTRA_STRING);
        Logger.getDefaultLogger().d("Intent contains: " + string);

        if (string.contains("VOLUME")) {
            onVolumeKeyEvent(string);
        } else {
            onMediaKeyEvent(string);
        }
    }

    /**
     * @param string
     */
    private void onVolumeKeyEvent(String string) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (string.contains("ACTION_UP")) {
            if (string.contains("KEYCODE_VOLUME_DOWN")) {
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI);
            }
            if (string.contains("KEYCODE_VOLUME_UP")) {
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI);
            }
        }
    }

    /**
     * @param string
     */
    private void onMediaKeyEvent(String string) {
        if (string.contains("ACTION_DOWN")) {
            keyEventAction = KeyEvent.ACTION_DOWN;
        }
        if (string.contains("ACTION_UP")) {
            keyEventAction = KeyEvent.ACTION_UP;
        }

        if (string.contains("KEYCODE_MEDIA_PLAY_PAUSE")) {
            keyEventCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
        }
        if (string.contains("KEYCODE_MEDIA_NEXT")) {
            keyEventCode = KeyEvent.KEYCODE_MEDIA_NEXT;
        }
        if (string.contains("KEYCODE_MEDIA_PREVIOUS")) {
            keyEventCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
        }
        sendKeyEvent();
    }

}
