package com.debloat.castlauncher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public class CastActivity extends Activity {
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        // Start the Cast receiver service
        try {
            Intent svc = new Intent();
            svc.setComponent(new ComponentName(
                "com.google.android.apps.mediashell",
                "com.google.android.apps.mediashell.MediaShellCastReceiverService"));
            startService(svc);
        } catch (Exception e) {}
        // Open Cast settings
        try {
            Intent settings = new Intent("com.google.android.settings.CAST_RECEIVER_SETTINGS");
            settings.setPackage("com.google.android.apps.mediashell");
            startActivity(settings);
        } catch (Exception e) {
            try {
                Intent fallback = new Intent(android.provider.Settings.ACTION_CAST_SETTINGS);
                startActivity(fallback);
            } catch (Exception e2) {}
        }
        finish();
    }
}
