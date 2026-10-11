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

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/** "आजको मिति" home-screen widget: today's Nepali (BS) date as a calendar tile, plus the tithi, any
 *  listed festival / holiday and the AD date. Today = the date in Nepal (Asia/Kathmandu). It redraws just
 *  after Nepal midnight (alarm), when the clock / time zone changes, and every 30 min as a fallback.
 *  Tapping it opens Ganak's पात्रो. */
public class DateWidget extends AppWidgetProvider {
    static final String TICK = "io.github.asrayacreate.twa.WIDGET_TICK";
    static final String PATRO_URL = "https://asrayacreate.github.io/ganak/index.html#patro";
    static final TimeZone NEPAL = TimeZone.getTimeZone("Asia/Kathmandu");
    static final String[] AD_MONTHS = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    static final String[] AD_DAYS = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) render(c, m, id);
        scheduleMidnight(c);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, Bundle options) {
        render(c, m, id);   /* resized: switch between the small and the wide layout */
    }

    @Override
    public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        String a = i.getAction();
        if (TICK.equals(a) || Intent.ACTION_TIME_CHANGED.equals(a) || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) refreshAll(c);
    }

    @Override
    public void onDisabled(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(tickIntent(c));
    }

    static void refreshAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        for (int id : m.getAppWidgetIds(new ComponentName(c, DateWidget.class))) render(c, m, id);
        scheduleMidnight(c);
    }

    static void render(Context c, AppWidgetManager m, int id) {
        Calendar now = Calendar.getInstance(NEPAL);
        int y = now.get(Calendar.YEAR), mo = now.get(Calendar.MONTH) + 1, d = now.get(Calendar.DAY_OF_MONTH);
        Bundle o = m.getAppWidgetOptions(id);
        boolean wide = o == null || o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250) >= 180;
        RemoteViews v = new RemoteViews(c.getPackageName(), wide ? R.layout.widget_wide : R.layout.widget_small);

        NepaliCal.Bs b = NepaliCal.adToBs(y, mo, d);
        String tithi = NepaliCal.tithiName(NepaliCal.tithiOfDay(y, mo, d), false);
        if (b != null) {
            v.setTextViewText(R.id.w_month, CalendarData.BS_MONTHS[b.m - 1]);
            v.setTextViewText(R.id.w_day, NepaliCal.npd(b.d));
            v.setTextViewText(R.id.w_wd, CalendarData.WEEKDAYS[b.wd]);
        }
        if (wide) {
            v.setTextViewText(R.id.w_head, b != null ? "आज · वि.सं. " + NepaliCal.npd(b.y) : "आज");
            v.setTextViewText(R.id.w_tithi, "🌙 " + tithi);
            StringBuilder f = new StringBuilder();
            List<String[]> fs = b != null ? NepaliCal.festivals(b) : java.util.Collections.<String[]>emptyList();
            for (String[] e : fs) f.append(f.length() > 0 ? " · " : "").append("h".equals(e[0]) ? "🔴 " : "🟡 ").append(e[1]);
            if (b != null && b.wd == 6 && fs.isEmpty()) f.append("शनिबार — बिदा");
            v.setTextViewText(R.id.w_fest, f.toString());
            v.setViewVisibility(R.id.w_fest, f.length() > 0 ? View.VISIBLE : View.GONE);
            v.setTextViewText(R.id.w_ad, AD_DAYS[now.get(Calendar.DAY_OF_WEEK) - 1] + ", " + AD_MONTHS[mo - 1] + " " + d + ", " + y);
        } else {
            v.setTextViewText(R.id.w_tithi, tithi);
        }

        Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(PATRO_URL));
        open.setClassName(c, "com.google.androidbrowserhelper.trusted.LauncherActivity");
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        v.setOnClickPendingIntent(R.id.w_root, PendingIntent.getActivity(c, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        m.updateAppWidget(id, v);
    }

    static PendingIntent tickIntent(Context c) {
        Intent t = new Intent(c, DateWidget.class).setAction(TICK);
        return PendingIntent.getBroadcast(c, 1, t, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** redraw just after the next midnight in Nepal (not a wake-up alarm: it runs when the phone is next in use) */
    static void scheduleMidnight(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Calendar n = Calendar.getInstance(NEPAL);
        n.add(Calendar.DAY_OF_MONTH, 1);
        n.set(Calendar.HOUR_OF_DAY, 0);
        n.set(Calendar.MINUTE, 0);
        n.set(Calendar.SECOND, 5);
        n.set(Calendar.MILLISECOND, 0);
        am.set(AlarmManager.RTC, n.getTimeInMillis(), tickIntent(c));
    }
}
