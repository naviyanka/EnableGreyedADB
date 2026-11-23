package com.debloat.adbaccess;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class AdbService extends AccessibilityService {

    private TextView overlayTv;
    private ScrollView overlaySv;
    private Handler handler;
    private boolean adbFlipped = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        handler = new Handler(Looper.getMainLooper());
        try {
            createOverlay();
        } catch (Exception e) {
            return;
        }

        log("===== v11 ALL-IN-ONE =====\n");
        log("-- BEFORE --");
        dumpTchInfo();
        checkAdb();

        // Run all phases with error isolation
        try { phase1ContentProvider(); } catch (Exception e) { log("P1 CRASH:" + e); }

        handler.postDelayed(new Runnable() { public void run() {
            try { phase2TriggerServices(); } catch (Exception e) { log("P2 CRASH:" + e); }
        }}, 1500);

        handler.postDelayed(new Runnable() { public void run() {
            try { phase3Shell(); } catch (Exception e) { log("P3 CRASH:" + e); }
        }}, 3000);

        handler.postDelayed(new Runnable() { public void run() {
            if (!adbFlipped) {
                try { phase4UiAutomation(); } catch (Exception e) { log("P4 CRASH:" + e); }
            }
        }}, 5000);

        handler.postDelayed(new Runnable() { public void run() {
            log("\n== FINAL ==");
            dumpTchInfo();
            checkAdb();
            log("===== END =====");
        }}, 15000);
    }

    private void phase1ContentProvider() {
        log("\n== P1: CONTENT PROVIDER ==");
        ContentResolver cr = getContentResolver();
        String base = "content://com.technicolor.contentprovider";
        String info = base + "/info";

        // A1: INSERT OR REPLACE on BASE URI with correct cols (data,value)
        // v8 proved base URI maps to: INSERT OR REPLACE INTO tch_info(cols) VALUES
        try {
            ContentValues cv = new ContentValues();
            cv.put("data", "adb");
            cv.put("value", "1");
            Uri r = cr.insert(Uri.parse(base), cv);
            log("A1 ins base(data,val)=" + r);
        } catch (Exception e) { log("A1:" + cut(e)); }

        // A2: Same on /info URI
        try {
            ContentValues cv = new ContentValues();
            cv.put("data", "adb");
            cv.put("value", "1");
            Uri r = cr.insert(Uri.parse(info), cv);
            log("A2 ins /info(data,val)=" + r);
        } catch (Exception e) { log("A2:" + cut(e)); }

        // A3: INSERT with all 3 cols (ID,data,value)
        try {
            ContentValues cv = new ContentValues();
            cv.put("ID", 1);
            cv.put("data", "adb");
            cv.put("value", "1");
            Uri r = cr.insert(Uri.parse(base), cv);
            log("A3 ins(ID,data,val)=" + r);
        } catch (Exception e) { log("A3:" + cut(e)); }

        // A4: UPDATE attempts
        try {
            ContentValues cv = new ContentValues();
            cv.put("value", "1");
            int r = cr.update(Uri.parse(info), cv, "data=?", new String[]{"adb"});
            log("A4a upd /info data=adb rows=" + r);
        } catch (Exception e) { log("A4a:" + cut(e)); }

        try {
            ContentValues cv = new ContentValues();
            cv.put("value", "1");
            int r = cr.update(Uri.parse(base), cv, "data=?", new String[]{"adb"});
            log("A4b upd base data=adb rows=" + r);
        } catch (Exception e) { log("A4b:" + cut(e)); }

        try {
            ContentValues cv = new ContentValues();
            cv.put("value", "1");
            int r = cr.update(Uri.parse(info), cv, "ID=?", new String[]{"1"});
            log("A4c upd /info ID=1 rows=" + r);
        } catch (Exception e) { log("A4c:" + cut(e)); }

        try {
            ContentValues cv = new ContentValues();
            cv.put("value", "1");
            int r = cr.update(Uri.parse(base), cv, "ID=?", new String[]{"1"});
            log("A4d upd base ID=1 rows=" + r);
        } catch (Exception e) { log("A4d:" + cut(e)); }

        // A5: DELETE + re-INSERT
        try {
            int d = cr.delete(Uri.parse(info), "ID=?", new String[]{"1"});
            log("A5a del /info ID=1 rows=" + d);
        } catch (Exception e) { log("A5a del:" + cut(e)); }
        try {
            int d = cr.delete(Uri.parse(base), "data=?", new String[]{"adb"});
            log("A5b del base data=adb rows=" + d);
        } catch (Exception e) { log("A5b del:" + cut(e)); }
        try {
            ContentValues cv = new ContentValues();
            cv.put("ID", 1);
            cv.put("data", "adb");
            cv.put("value", "1");
            Uri r = cr.insert(Uri.parse(base), cv);
            log("A5c reinsert=" + r);
        } catch (Exception e) { log("A5c:" + cut(e)); }

        // A6: Try SQL injection
        try {
            Cursor c = cr.query(Uri.parse(info),
                null,
                "ID=1; UPDATE tch_info SET value='1' WHERE data='adb'",
                null, null);
            log("A6 inj1 cursor=" + (c != null ? "rows=" + c.getCount() : "null"));
            if (c != null) c.close();
        } catch (Exception e) { log("A6:" + cut(e)); }

        log("-- AFTER P1 --");
        dumpTchInfo();
        checkAdb();
    }

    private void phase2TriggerServices() {
        log("\n== P2: SERVICES ==");

        String[][] svcs = {
            {"com.technicolor.enableUpdates", "com.technicolor.enableUpdates.EnableService"},
            {"com.technicolor.atlsystemservice", "com.technicolor.atlsystemservice.AtlSystemService"},
            {"com.technicolor.tchvendorintf", "com.technicolor.tchvendorintf.TchVendorIntfService"},
            {"com.technicolor.android.remotecontrolservice", "com.technicolor.android.remotecontrolservice.RemoteControlService"},
        };
        for (String[] s : svcs) {
            try {
                Intent i = new Intent();
                i.setClassName(s[0], s[1]);
                startService(i);
                log("started " + s[1].substring(s[1].lastIndexOf('.') + 1));
            } catch (Exception e) {
                log("ERR " + s[1].substring(s[1].lastIndexOf('.') + 1));
            }
        }

        // Boot completed
        try {
            Intent i = new Intent("android.intent.action.BOOT_COMPLETED");
            i.setClassName("com.technicolor.enableUpdates",
                "com.technicolor.enableUpdates.STBBootcompleteReceiver");
            sendBroadcast(i);
            log("BOOT_COMPLETED sent");
        } catch (Exception e) {}

        checkAdb();
    }

    private void phase3Shell() {
        log("\n== P3: SHELL ==");
        String[] cmds = {
            "settings put global adb_enabled 1",
            "setprop persist.sys.usb.config adb",
            "start adbd",
            "setprop ctl.start adbd",
        };
        for (String cmd : cmds) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
                int exit = p.waitFor();
                String line = "";
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    line = br.readLine();
                    if (line == null) line = "";
                } catch (Exception ignore) {}
                log(cmd.substring(0, Math.min(25, cmd.length())) + " exit=" + exit
                    + (line.length() > 0 ? " " + line.substring(0, Math.min(40, line.length())) : ""));
            } catch (Exception e) {}
        }

        if (checkAdbEnabled()) {
            adbFlipped = true;
            log("*** ADB ON! ***");
        }
    }

    private void phase4UiAutomation() {
        log("\n== P4: UI AUTOMATION ==");

        // Open dev options
        try {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            log("Dev Options opened");
        } catch (Exception e) {
            log("Dev Options ERR:" + cut(e));
            try {
                Intent i = new Intent();
                i.setClassName("com.android.tv.settings",
                    "com.android.tv.settings.system.development.DevelopmentActivity");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                log("Direct dev activity opened");
            } catch (Exception e2) {
                log("Both failed");
                return;
            }
        }

        // Wait for UI load then scan
        handler.postDelayed(new Runnable() { public void run() {
            try { scanForToggle(); } catch (Exception e) { log("scan CRASH:" + e); }
        }}, 3000);
    }

    private void scanForToggle() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            log("No root window");
            return;
        }
        log("Window pkg=" + root.getPackageName());

        // Find USB debugging
        String[] texts = {"USB debugging", "USB Debugging", "debugging", "Debug"};
        boolean found = false;
        for (String text : texts) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
            if (nodes == null || nodes.isEmpty()) continue;
            found = true;
            for (AccessibilityNodeInfo node : nodes) {
                log("FOUND: '" + node.getText() + "' cls=" + node.getClassName()
                    + " en=" + node.isEnabled() + " ck=" + node.isClickable()
                    + " chk=" + node.isCheckable() + " val=" + node.isChecked());

                // Click node
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                // Click parent chain
                AccessibilityNodeInfo p = node.getParent();
                for (int depth = 0; p != null && depth < 5; depth++) {
                    if (p.isClickable()) {
                        p.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        log("  clicked parent d=" + depth);
                    }
                    p = p.getParent();
                }
            }
        }

        if (!found) {
            log("USB debugging text NOT found in tree");
            // Dump all visible text
            dumpTree(root, 0);
        }

        // Handle dialog after 2s
        handler.postDelayed(new Runnable() { public void run() {
            try {
                AccessibilityNodeInfo r = getRootInActiveWindow();
                if (r == null) return;
                String[] btns = {"OK", "Allow", "Yes", "Enable"};
                for (String b : btns) {
                    List<AccessibilityNodeInfo> ns = r.findAccessibilityNodeInfosByText(b);
                    if (ns != null) for (AccessibilityNodeInfo n : ns) {
                        if (n.isClickable()) {
                            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            log("Clicked dialog: " + b);
                        }
                    }
                }
            } catch (Exception e) {}
            checkAdb();
        }}, 2000);
    }

    private void dumpTree(AccessibilityNodeInfo node, int depth) {
        if (node == null || depth > 6) return;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        if (text != null || desc != null || node.isClickable() || node.isCheckable()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < depth; i++) sb.append(" ");
            sb.append(node.getClassName());
            if (text != null) sb.append(" t='").append(text).append("'");
            if (desc != null) sb.append(" d='").append(desc).append("'");
            if (node.isClickable()) sb.append(" CLICK");
            if (node.isCheckable()) sb.append(" CHK=").append(node.isChecked());
            if (!node.isEnabled()) sb.append(" DIS");
            log(sb.toString());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            try { dumpTree(node.getChild(i), depth + 1); } catch (Exception e) {}
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (handler != null) {
                handler.postDelayed(new Runnable() { public void run() {
                    try {
                        AccessibilityNodeInfo r = getRootInActiveWindow();
                        if (r == null) return;
                        String[] btns = {"OK", "Allow", "Yes", "Enable"};
                        for (String b : btns) {
                            List<AccessibilityNodeInfo> ns = r.findAccessibilityNodeInfosByText(b);
                            if (ns != null) for (AccessibilityNodeInfo n : ns)
                                if (n.isClickable()) n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    } catch (Exception e) {}
                }}, 500);
            }
        }
    }

    @Override public void onInterrupt() {}

    private void dumpTchInfo() {
        try {
            Cursor c = getContentResolver().query(
                Uri.parse("content://com.technicolor.contentprovider/info"),
                null, null, null, null);
            if (c != null) {
                while (c.moveToNext()) {
                    StringBuilder row = new StringBuilder("  ");
                    for (int i = 0; i < c.getColumnCount(); i++)
                        try { row.append(c.getColumnName(i)).append("=")
                                .append(c.getString(i)).append(" "); }
                        catch (Exception e) {}
                    log(row.toString());
                }
                c.close();
            }
        } catch (Exception e) { log("  tch_info ERR"); }
    }

    private void checkAdb() {
        try {
            log("  adb_enabled=" + Settings.Global.getString(
                getContentResolver(), "adb_enabled"));
        } catch (Exception e) {}
        try {
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress("127.0.0.1", 5555), 1500);
            s.close();
            log("  PORT 5555=OPEN!!!");
            adbFlipped = true;
        } catch (Exception e) {
            log("  port 5555=closed");
        }
    }

    private boolean checkAdbEnabled() {
        try {
            return "1".equals(Settings.Global.getString(
                getContentResolver(), "adb_enabled"));
        } catch (Exception e) { return false; }
    }

    private void createOverlay() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayTv = new TextView(this);
        overlayTv.setTextColor(Color.GREEN);
        overlayTv.setBackgroundColor(0xDD000000);
        overlayTv.setTextSize(9);
        overlayTv.setTypeface(Typeface.MONOSPACE);
        overlayTv.setPadding(12, 12, 12, 12);
        overlaySv = new ScrollView(this);
        overlaySv.addView(overlayTv);
        overlaySv.setFocusable(true);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        wm.addView(overlaySv, lp);
    }

    private void log(String line) {
        if (overlayTv != null) {
            overlayTv.append(line + "\n");
            overlaySv.post(new Runnable() {
                public void run() { overlaySv.fullScroll(ScrollView.FOCUS_DOWN); }
            });
        }
    }

    private String cut(Exception e) {
        String m = e.getMessage();
        if (m == null) return e.getClass().getSimpleName();
        return m.length() > 60 ? m.substring(0, 60) : m;
    }
}
