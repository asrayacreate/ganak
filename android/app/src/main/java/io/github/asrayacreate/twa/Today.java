package io.github.asrayacreate.twa;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/** Today in Nepal (Asia/Kathmandu) — the texts the widget and the status-bar date share. */
final class Today {
    static final TimeZone NEPAL = TimeZone.getTimeZone("Asia/Kathmandu");
    static final String[] AD_MONTHS = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    static final String[] AD_DAYS = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};

    final int y, m, d;          /* AD */
    final NepaliCal.Bs b;       /* BS (null outside the table) */
    final String tithi, fest, ad;

    private Today(int y, int m, int d, int adWd) {
        this.y = y; this.m = m; this.d = d;
        b = NepaliCal.adToBs(y, m, d);
        tithi = NepaliCal.tithiName(NepaliCal.tithiOfDay(y, m, d), false);
        StringBuilder f = new StringBuilder();
        List<String[]> fs = b != null ? NepaliCal.festivals(b) : java.util.Collections.<String[]>emptyList();
        for (String[] e : fs) f.append(f.length() > 0 ? " · " : "").append("h".equals(e[0]) ? "🔴 " : "🟡 ").append(e[1]);
        if (b != null && b.wd == 6 && fs.isEmpty()) f.append("शनिबार — बिदा");
        fest = f.toString();
        ad = AD_DAYS[adWd] + ", " + AD_MONTHS[m - 1] + " " + d + ", " + y;
    }

    static Today now() {
        Calendar c = Calendar.getInstance(NEPAL);
        return new Today(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_WEEK) - 1);
    }

    String month() { return b != null ? CalendarData.BS_MONTHS[b.m - 1] : ""; }
    String day() { return b != null ? NepaliCal.npd(b.d) : ""; }
    String weekday() { return b != null ? CalendarData.WEEKDAYS[b.wd] : ""; }
    String year() { return b != null ? NepaliCal.npd(b.y) : ""; }

    /** next midnight in Nepal (+5 s), in epoch ms */
    static long nextMidnight() {
        Calendar n = Calendar.getInstance(NEPAL);
        n.add(Calendar.DAY_OF_MONTH, 1);
        n.set(Calendar.HOUR_OF_DAY, 0);
        n.set(Calendar.MINUTE, 0);
        n.set(Calendar.SECOND, 5);
        n.set(Calendar.MILLISECOND, 0);
        return n.getTimeInMillis();
    }
}
