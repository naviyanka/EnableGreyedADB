package com.debloat.adbaccess;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.BLACK);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        status = new TextView(this);
        status.setTextColor(Color.GREEN);
        status.setTextSize(13);
        layout.addView(status);

        addButton(layout, "1. WRITE_SETTINGS Permission", new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    i.setData(Uri.parse("package:com.debloat.adbaccess"));
                    startActivity(i);
                } catch (Exception e) { appendStatus("ERR: " + e.getMessage()); }
            }
        });

        addButton(layout, "2. Overlay Permission", new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    i.setData(Uri.parse("package:com.debloat.adbaccess"));
                    startActivity(i);
                } catch (Exception e) { appendStatus("ERR: " + e.getMessage()); }
            }
        });

        addButton(layout, "3. App Info (all permissions)", new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.setData(Uri.parse("package:com.debloat.adbaccess"));
                    startActivity(i);
                } catch (Exception e) { appendStatus("ERR: " + e.getMessage()); }
            }
        });

        addButton(layout, "4. Developer Options", new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                } catch (Exception e) { appendStatus("ERR: " + e.getMessage()); }
            }
        });

        addButton(layout, "5. Accessibility Settings", new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                } catch (Exception e) { appendStatus("ERR: " + e.getMessage()); }
            }
        });

        addButton(layout, "6. TRY ADB ENABLE (all methods)", new View.OnClickListener() {
            @Override public void onClick(View v) {
                tryAllAdbMethods();
            }
        });

        addButton(layout, "7. Security Settings", new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                } catch (Exception e) { appendStatus("ERR: " + e.getMessage()); }
            }
        });

        addButton(layout, "REFRESH", new View.OnClickListener() {
            @Override public void onClick(View v) { updateStatus(); }
        });

        sv.addView(layout);
        setContentView(sv);
        updateStatus();
    }

    private void addButton(LinearLayout layout, String text, View.OnClickListener listener) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setOnClickListener(listener);
        btn.setFocusable(true);
        btn.setFocusableInTouchMode(true);
        layout.addView(btn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("ADB Enable v11\n\n");

        boolean canWrite = Settings.System.canWrite(this);
        sb.append("WRITE_SETTINGS: ").append(canWrite ? "GRANTED" : "DENIED").append("\n");
        sb.append("OVERLAY: ").append(Settings.canDrawOverlays(this) ? "GRANTED" : "DENIED").append("\n");

        try {
            sb.append("adb_enabled: ").append(
                Settings.Global.getString(getContentResolver(), "adb_enabled")).append("\n");
        } catch (Exception e) {}
        try {
            sb.append("dev_settings: ").append(
                Settings.Global.getString(getContentResolver(), "development_settings_enabled")).append("\n");
        } catch (Exception e) {}

        // Dump tch_info
        sb.append("\ntch_info:\n");
        try {
            Cursor c = getContentResolver().query(
                Uri.parse("content://com.technicolor.contentprovider/info"),
                null, null, null, null);
            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < c.getColumnCount(); i++)
                        try { sb.append(c.getColumnName(i)).append("=")
                                .append(c.getString(i)).append(" "); }
                        catch (Exception ex) {}
                    sb.append("\n");
                }
                c.close();
            }
        } catch (Exception e) {
            sb.append("ERR\n");
        }

        sb.append("\nPort 5555: ");
        try {
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress("127.0.0.1", 5555), 1500);
            s.close();
            sb.append("OPEN!!!\n");
        } catch (Exception e) {
            sb.append("closed\n");
        }

        status.setText(sb.toString());
    }

    private void tryAllAdbMethods() {
        StringBuilder sb = new StringBuilder();
        ContentResolver cr = getContentResolver();

        // Method 1: Direct Settings.Global
        try {
            Settings.Global.putInt(cr, "adb_enabled", 1);
            sb.append("M1 Global.putInt: OK!\n");
        } catch (Exception e) {
            sb.append("M1 Global.putInt: ").append(shorten(e.getMessage(), 50)).append("\n");
        }

        // Method 2: Content provider insert
        try {
            ContentValues cv = new ContentValues();
            cv.put("data", "adb");
            cv.put("value", "1");
            Uri r = cr.insert(Uri.parse("content://com.technicolor.contentprovider"), cv);
            sb.append("M2 insert base: ").append(r).append("\n");
        } catch (Exception e) {
            sb.append("M2 insert: ").append(shorten(e.getMessage(), 50)).append("\n");
        }

        // Method 3: Settings content provider
        try {
            ContentValues cv = new ContentValues();
            cv.put("name", "adb_enabled");
            cv.put("value", "1");
            Uri r = cr.insert(Uri.parse("content://settings/global"), cv);
            sb.append("M3 settings/global: ").append(r).append("\n");
        } catch (Exception e) {
            sb.append("M3 settings: ").append(shorten(e.getMessage(), 50)).append("\n");
        }

        // Method 4: Shell
        try {
            Process p = Runtime.getRuntime().exec(
                new String[]{"sh", "-c", "settings put global adb_enabled 1"});
            sb.append("M4 shell: exit=").append(p.waitFor()).append("\n");
        } catch (Exception e) {
            sb.append("M4 shell: ERR\n");
        }

        // Method 5: Trigger EnableService
        try {
            Intent i = new Intent();
            i.setClassName("com.technicolor.enableUpdates",
                "com.technicolor.enableUpdates.EnableService");
            startService(i);
            sb.append("M5 EnableService: started\n");
        } catch (Exception e) {
            sb.append("M5 EnableService: ERR\n");
        }

        appendStatus("\n" + sb.toString());
        updateStatus();
    }

    private void appendStatus(String text) {
        status.append(text + "\n");
    }

    private String shorten(String s, int max) {
        if (s == null) return "null";
        return s.length() > max ? s.substring(0, max) : s;
    }
}
