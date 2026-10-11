package io.github.asrayacreate.twa;

import android.net.Uri;
import android.os.Bundle;

/** Opens Ganak (the Trusted Web Activity). Adds ?app=2&bar=0|1 so the web app knows it runs inside this
 *  Android app (its 📲 button then switches the status-bar date here) and whether that is on, and
 *  refreshes the widget and the status-bar date each time the app is opened. */
public class LauncherActivity extends com.google.androidbrowserhelper.trusted.LauncherActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        DateWidget.refreshAll(this);
    }

    @Override
    protected Uri getLaunchingUrl() {
        return super.getLaunchingUrl().buildUpon().appendQueryParameter("app", "2")
                .appendQueryParameter("bar", DateBar.isOn(this) ? "1" : "0").build();
    }
}
