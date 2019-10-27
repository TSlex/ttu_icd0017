package com.tslex.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PhoneEventReceiver extends BroadcastReceiver {

    private static String TAG = PhoneEventReceiver.class.getSimpleName();
    private static TelephonyManager telephonyManager;
    private static PhoneStateListener listener;

    public PhoneEventReceiver() {
        super();
        Log.d(TAG, "Created");
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "Reseived: " + intent.getAction());

        if (listener == null) {
            listener = new PhoneStateListener() {

                @Override
                public void onCallStateChanged(int state, String phoneNumber) {

                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:
                            Log.d(TAG, "IDLE");
                            Log.d(TAG, "RINGING");
                            LocalBroadcastManager
                                    .getInstance(context.getApplicationContext())
                                    .sendBroadcast(new Intent(IntentActions.INTENT_PLAYER_UNMUTE.getAction()));
                            break;

                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            Log.d(TAG, "OFFHOOK");
                            break;

                        case TelephonyManager.CALL_STATE_RINGING:
                            Log.d(TAG, "RINGING");
                            LocalBroadcastManager
                                    .getInstance(context.getApplicationContext())
                                    .sendBroadcast(new Intent(IntentActions.INTENT_PLAYER_MUTE.getAction()));
                            break;
                    }

                    super.onCallStateChanged(state, phoneNumber);
                }
            };

            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
}
