package com.debloat.castlauncher;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Force-start the Cast receiver service after boot
            try {
                Intent svc = new Intent();
                svc.setComponent(new ComponentName(
                    "com.google.android.apps.mediashell",
                    "com.google.android.apps.mediashell.MediaShellCastReceiverService"));
                context.startService(svc);
            } catch (Exception e) {}
        }
    }
}
