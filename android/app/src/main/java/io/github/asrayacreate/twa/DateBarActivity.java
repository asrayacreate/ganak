package io.github.asrayacreate.twa;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

/** Invisible switch for the status-bar date, opened by the web app's 📲 button:
 *  ganak://datebar/on or ganak://datebar/off. Asks for notification permission when needed
 *  (Android 13+); if notifications are blocked for Ganak, opens the app's notification settings. */
public class DateBarActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Uri u = getIntent().getData();
        boolean on = u == null || !"off".equals(u.getLastPathSegment());
        if (!on) { DateBar.set(this, false); finish(); return; }
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }
        done();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        done();
    }

    private void done() {
        DateBar.set(this, true);
        if (!DateBar.allowed(this) && Build.VERSION.SDK_INT >= 26) {
            startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
        }
        finish();
    }
}
