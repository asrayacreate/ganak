package io.github.asrayacreate.twa;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

/** "आजको मिति" home-screen widget: today's Nepali (BS) date as a calendar tile, plus the tithi, any
 *  listed festival / holiday and the AD date. Today = the date in Nepal. This receiver also keeps the
 *  status-bar date (DateBar) current: it redraws both just after Nepal midnight (alarm), when the clock /
 *  time zone changes, after a restart or an app update, and every 30 min as a fallback.
 *  Tapping the widget opens Ganak's पात्रो. */
public class DateWidget extends AppWidgetProvider {
    static final String TICK = "io.github.asrayacreate.twa.WIDGET_TICK";
    static final String PATRO_URL = "https://asrayacreate.github.io/ganak/index.html#patro";

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        refreshAll(c);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, Bundle options) {
        render(c, m, id, Today.now());   /* resized: switch between the small and the wide layout */
    }

    @Override
    public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        String a = i.getAction();
        if (TICK.equals(a) || Intent.ACTION_TIME_CHANGED.equals(a) || Intent.ACTION_TIMEZONE_CHANGED.equals(a)
                || Intent.ACTION_BOOT_COMPLETED.equals(a) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)) refreshAll(c);
    }

    @Override
    public void onDisabled(Context c) {
        schedule(c);   /* last widget removed: keep the alarm only while the status-bar date is on */
    }

    static int[] widgetIds(Context c) {
        return AppWidgetManager.getInstance(c).getAppWidgetIds(new ComponentName(c, DateWidget.class));
    }

    /** redraw every widget and the status-bar date, then set the next midnight alarm */
    static void refreshAll(Context c) {
        Today t = Today.now();
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        for (int id : widgetIds(c)) render(c, m, id, t);
        DateBar.post(c, t);
        schedule(c);
    }

    static void render(Context c, AppWidgetManager m, int id, Today t) {
        Bundle o = m.getAppWidgetOptions(id);
        boolean wide = o == null || o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250) >= 180;
        RemoteViews v = new RemoteViews(c.getPackageName(), wide ? R.layout.widget_wide : R.layout.widget_small);
        v.setTextViewText(R.id.w_month, t.month());
        v.setTextViewText(R.id.w_day, t.day());
        v.setTextViewText(R.id.w_wd, t.weekday());
        if (wide) {
            v.setTextViewText(R.id.w_head, t.b != null ? "आज · वि.सं. " + t.year() : "आज");
            v.setTextViewText(R.id.w_tithi, "🌙 " + t.tithi);
            v.setTextViewText(R.id.w_fest, t.fest);
            v.setViewVisibility(R.id.w_fest, t.fest.isEmpty() ? View.GONE : View.VISIBLE);
            v.setTextViewText(R.id.w_ad, t.ad);
        } else {
            v.setTextViewText(R.id.w_tithi, t.tithi);
        }
        v.setOnClickPendingIntent(R.id.w_root, openPatro(c));
        m.updateAppWidget(id, v);
    }

    /** tap → Ganak at the पात्रो */
    static PendingIntent openPatro(Context c) {
        Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(PATRO_URL));
        open.setClass(c, LauncherActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(c, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static PendingIntent tickIntent(Context c) {
        Intent t = new Intent(c, DateWidget.class).setAction(TICK);
        return PendingIntent.getBroadcast(c, 1, t, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** redraw just after the next midnight in Nepal while a widget or the status-bar date is in use
     *  (not a wake-up alarm: it runs as soon as the phone is next in use) */
    static void schedule(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        if (widgetIds(c).length == 0 && !DateBar.isOn(c)) { am.cancel(tickIntent(c)); return; }
        am.set(AlarmManager.RTC, Today.nextMidnight(), tickIntent(c));
    }
}
