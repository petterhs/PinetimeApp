package com.example.infinitimeapp.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.telecom.TelecomManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.example.infinitimeapp.WatchActivity;
import com.example.infinitimeapp.bluetooth.BluetoothService;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.infinitimeapp.common.Constants.*;

public class AlertNotificationService extends BaseService {
    private Context mContext;

    private static final String NEW_ALERT = "NEW_ALERT";
    private static final String EVENT = "EVENT";

    public static final byte PADDING = 0x00;

    public static final byte ALERT_SIMPLE_ALERT = 0x00;
    public static final byte ALERT_EMAIL = 0x01;
    public static final byte ALERT_NEWS = 0x02;
    public static final byte ALERT_INCOMING_CALL = 0x03;
    public static final byte ALERT_MISSED_CALL = 0x04;
    public static final byte ALERT_SMS = 0x05;
    public static final byte ALERT_VOICE_MAIL = 0x06;
    public static final byte ALERT_SCHEDULE = 0x07;
    public static final byte ALERT_HIGH_PRIORITY_ALERT = 0x08;
    public static final byte ALERT_INSTANT_MESSAGE = 0x09;
    //public static final byte ALERT_ALL = 0xFF;


    private static final byte EVENT_HANG_UP_CALL = 0x00;
    private static final byte EVENT_ANSWER_CALL = 0x01;
    private static final byte EVENT_MUTE_CALL = 0x02;

    private static AlertNotificationService sInstance;

    private AlertNotificationService() {
        super(Stream.of(new String[][]{
                {NEW_ALERT, "00002a46-0000-1000-8000-00805f9b34fb"},
                {EVENT, "00020001-78fc-48fe-8e23-433b3a1942d0"}
        }).collect(Collectors.toMap(p -> p[0], p -> p[1])));
    }

    public static AlertNotificationService getInstance() {
        if (sInstance == null) sInstance = new AlertNotificationService();
        return sInstance;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public UUID getEventUUID() {
        return getCharacteristicUUID(EVENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onDataRecieved(UUID characteristicUUID, byte[] message) {
        switch (getCharacteristicName(characteristicUUID)) {
            case NEW_ALERT:
                break;
            case EVENT:
                eventHandler(message);
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void eventHandler(byte[] message) {

        switch (message[0]) {
            case EVENT_ANSWER_CALL:
                answerPhoneCall();
                break;
            case EVENT_HANG_UP_CALL:
                hangUpPhoneCall();
                break;
            case EVENT_MUTE_CALL:
                mutePhoneCall();
                break;
        }
    }

    private void answerPhoneCall() {
        TelecomManager telecomManager = WatchActivity.sTelecomManager;
        if (telecomManager != null && mContext != null) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            telecomManager.acceptRingingCall();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void hangUpPhoneCall() {
        TelecomManager telecomManager = WatchActivity.sTelecomManager;
        if (telecomManager != null && mContext != null) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            telecomManager.endCall();
        }
    }

    private void mutePhoneCall() {
        TelecomManager telecomManager = WatchActivity.sTelecomManager;
        AudioManager audioManager = WatchActivity.sAudioManager;
        if (telecomManager != null && mContext != null) {
            if (ActivityCompat.checkSelfPermission(mContext , Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "MISSING PERMISSION TO SILENCE PHONE");
                return;
            }
            Log.d(TAG, "SILENCING PHONE");
            audioManager.setRingerMode(audioManager.RINGER_MODE_SILENT);
        }
    }

    public void sendMessage(BluetoothService bluetoothService, String message) {
        sendMessage(bluetoothService, message, ALERT_SIMPLE_ALERT);
    }

    public void subscribeOnEvents(BluetoothService bluetoothService) {
        bluetoothService.listenOnCharacteristic(getCharacteristicUUID(EVENT));
    }

    public void sendMessage(BluetoothService bluetoothService, String message, byte category) {
        ByteBuffer bb = ByteBuffer.allocate(message.length() + 4);
        bb.put(category);
        bb.put(PADDING);
        bb.put(PADDING);
        bb.put(message.getBytes());

        Log.d(TAG, message);
        bluetoothService.write(getCharacteristicUUID(NEW_ALERT), bb.array());
    }
}
