package com.ecttion.pilotoboradevan;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class BootCompletedIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "Liga Service***");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, MapsActivity.class);
            pushIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pushIntent);
        }
    }
}