package com.tangorra.matias.savi.BroadCastReciber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class EstadoDispositivo extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if(action != null) {
            if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
                // CODE
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                // CODE
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                // CODE
            } else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
                // CODE
            } else if (action.equals(Intent.ACTION_DATE_CHANGED)) {
                // CODE
            } else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                // CODE
            } else if (action.equals(Intent.ACTION_SHUTDOWN)) {
                // CODE
            }
        }
    }
}
