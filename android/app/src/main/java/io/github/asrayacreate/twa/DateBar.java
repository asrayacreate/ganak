package io.github.asrayacreate.twa;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.Build;

/** The date always at the top of the phone: an ongoing notification whose status-bar icon is today's
 *  Nepali day number (२५), with "आज असोज २५ · आइतबार" and the tithi / festival when pulled down.
 *  Quiet (no sound, no vibration, no pop-up); switched on from the app's 📲 button (DateBarActivity). */
final class DateBar {
    private DateBar() {}
    static final String PREFS = "ganak", KEY = "datebar", CHANNEL = "date_bar";
    static final int ID = 2083;

    static boolean isOn(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false);
    }

    static void set(Context c, boolean on) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, on).apply();
        if (on) post(c, Today.now()); else cancel(c);
        DateWidget.schedule(c);
    }

    static boolean allowed(Context c) {
        if (Build.VERSION.SDK_INT >= 33
                && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false;
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        return nm != null && (Build.VERSION.SDK_INT < 24 || nm.areNotificationsEnabled());
    }

    static void cancel(Context c) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(ID);
    }

    static void post(Context c, Today t) {
        if (!isOn(c) || !allowed(c) || t.b == null) return;
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder nb;
        if (Build.VERSION.SDK_INT >= 26) {
            /* default importance keeps the icon in the status bar; no sound / vibration so it never disturbs */
            NotificationChannel ch = new NotificationChannel(CHANNEL, "आजको मिति (स्क्रिनको माथि)", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("आजको नेपाली मिति, तिथि र चाडपर्व — सधैं देखिने");
            ch.setSound(null, null);
            ch.enableVibration(false);
            ch.setShowBadge(false);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(ch);
            nb = new Notification.Builder(c, CHANNEL);
        } else {
            nb = new Notification.Builder(c);
            nb.setPriority(Notification.PRIORITY_DEFAULT);
            nb.setDefaults(0);
        }
        String sub = "वि.सं. " + t.year() + " · " + t.tithi + (t.fest.isEmpty() ? "" : " · " + t.fest);
        nb.setContentTitle("आज " + t.month() + " " + t.day() + " · " + t.weekday())
          .setContentText(sub)
          .setStyle(new Notification.BigTextStyle().bigText(sub + "\n" + t.ad))
          .setOngoing(true)
          .setOnlyAlertOnce(true)
          .setShowWhen(false)
          .setContentIntent(DateWidget.openPatro(c))
          .setLargeIcon(tile(c, t));
        if (Build.VERSION.SDK_INT >= 21) nb.setVisibility(Notification.VISIBILITY_PUBLIC).setCategory(Notification.CATEGORY_STATUS);
        if (Build.VERSION.SDK_INT >= 23) nb.setSmallIcon(Icon.createWithBitmap(dayIcon(c, t.day())));
        else nb.setSmallIcon(R.drawable.ic_notification_icon);
        nm.notify(ID, nb.build());
    }

    /** status-bar icon: the day number, white on transparent (Android draws it in the status-bar colour) */
    static Bitmap dayIcon(Context c, String day) {
        int s = Math.round(24 * c.getResources().getDisplayMetrics().density);
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextAlign(Paint.Align.CENTER);
        float size = s * 0.95f;
        p.setTextSize(size);
        float w = p.measureText(day);
        if (w > s * 0.98f) { size *= s * 0.98f / w; p.setTextSize(size); }
        Paint.FontMetrics fm = p.getFontMetrics();
        new Canvas(b).drawText(day, s / 2f, s / 2f - (fm.ascent + fm.descent) / 2f, p);
        return b;
    }

    /** the picture when pulled down: a calendar tile — red month band, big day, weekday */
    static Bitmap tile(Context c, Today t) {
        int s = Math.round(64 * c.getResources().getDisplayMetrics().density);
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float r = s * 0.14f, band = s * 0.28f;
        RectF all = new RectF(0, 0, s, s);
        p.setColor(Color.parseColor("#FFFEF8"));
        cv.drawRoundRect(all, r, r, p);
        cv.save();
        cv.clipRect(0, 0, s, band);
        p.setColor(Color.parseColor("#C62828"));
        cv.drawRoundRect(all, r, r, p);
        cv.restore();
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setColor(Color.WHITE);
        p.setTextSize(s * 0.16f);
        cv.drawText(t.month(), s / 2f, band * 0.72f, p);
        p.setColor(Color.parseColor("#15202A"));
        p.setTextSize(s * 0.42f);
        cv.drawText(t.day(), s / 2f, s * 0.72f, p);
        p.setColor(Color.parseColor("#5B6672"));
        p.setTextSize(s * 0.12f);
        cv.drawText(t.weekday(), s / 2f, s * 0.92f, p);
        return b;
    }
}
