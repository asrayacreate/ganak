package io.github.asrayacreate.twa;

/** Nepali (BS) date, tithi and festivals for the home-screen widget — the same tables
 *  (CalendarData, generated from index.html) and the same Sun / Moon method as the web app's पात्रो
 *  (Paul Schlyter's orbital elements with the main lunar perturbations). Plain Java, no Android types. */
final class NepaliCal {
  private NepaliCal(){}

  /** A BS date: year, month (1–12), day, weekday (0 = Sunday). */
  static final class Bs {
    final int y, m, d, wd;
    Bs(int y, int m, int d, int wd){ this.y = y; this.m = m; this.d = d; this.wd = wd; }
  }

  /** days since 1970-01-01 for a Gregorian date (proleptic) */
  static long epochDay(int y, int m, int d){
    long yy = y - (m <= 2 ? 1 : 0), era = (yy >= 0 ? yy : yy - 399) / 400, yoe = yy - era * 400;
    long doy = (153L * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1, doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
    return era * 146097 + doe - 719468;
  }

  /** AD date → BS, or null outside the table */
  static Bs adToBs(int y, int m, int d){
    long ed = epochDay(y, m, d), days = ed - CalendarData.BS_EPOCH_DAY;
    if(days < 0) return null;
    int wd = (int) Math.floorMod(ed + 4, 7L);   /* 1970-01-01 was a Thursday */
    int by = CalendarData.BS_START_YEAR, bm = 1;
    while(true){
      int i = by - CalendarData.BS_START_YEAR;
      if(i >= CalendarData.BS_DATA.length) return null;
      int dim = CalendarData.BS_DATA[i][bm - 1];
      if(days < dim) break;
      days -= dim; bm++;
      if(bm > 12){ bm = 1; by++; }
    }
    return new Bs(by, bm, (int) days + 1, wd);
  }

  private static double rev(double x){ return ((x % 360) + 360) % 360; }
  private static double kepler(double M, double e){
    double r = Math.PI / 180, E = M + e / r * Math.sin(M * r) * (1 + e * Math.cos(M * r));
    for(int k = 0; k < 8; k++) E -= (E - e / r * Math.sin(E * r) - M) / (1 - e * Math.cos(E * r));
    return E;
  }

  /** tithi 0–29 (0 = Shukla Pratipada, 14 = Purnima, 29 = Aunsi) at the instant utcMillis */
  static int tithiAt(long utcMillis){
    double d = utcMillis / 86400000.0 - 10956, r = Math.PI / 180;
    double ws = 282.9404 + 4.70935e-5 * d, es = .016709 - 1.151e-9 * d, Ms = rev(356.0470 + .9856002585 * d);
    double Es = kepler(Ms, es), xs = Math.cos(Es * r) - es, ys = Math.sqrt(1 - es * es) * Math.sin(Es * r);
    double sun = rev(Math.atan2(ys, xs) / r + ws);
    double Nm = 125.1228 - .0529538083 * d, wm = 318.0634 + .1643573223 * d, Mm = rev(115.3654 + 13.0649929509 * d), em = .0549, im = 5.1454;
    double Em = kepler(Mm, em), xv = 60.2666 * (Math.cos(Em * r) - em), yv = 60.2666 * Math.sqrt(1 - em * em) * Math.sin(Em * r);
    double u = (Math.atan2(yv, xv) / r + wm) * r, R = Math.sqrt(xv * xv + yv * yv);
    double x = R * (Math.cos(Nm * r) * Math.cos(u) - Math.sin(Nm * r) * Math.sin(u) * Math.cos(im * r));
    double y = R * (Math.sin(Nm * r) * Math.cos(u) + Math.cos(Nm * r) * Math.sin(u) * Math.cos(im * r));
    double D = (Nm + wm + Mm - Ms - ws) * r, F = (wm + Mm) * r, a = Mm * r, b = Ms * r;
    double moon = rev(Math.atan2(y, x) / r - 1.274 * Math.sin(a - 2 * D) + .658 * Math.sin(2 * D) - .186 * Math.sin(b) - .059 * Math.sin(2 * a - 2 * D)
      - .057 * Math.sin(a - 2 * D + b) + .053 * Math.sin(a + 2 * D) + .046 * Math.sin(2 * D - b) + .041 * Math.sin(a - b) - .035 * Math.sin(D)
      - .031 * Math.sin(a + b) - .015 * Math.sin(2 * F - 2 * D) + .011 * Math.sin(a - 4 * D));
    return (int) Math.floor(rev(moon - sun) / 12);
  }

  /** the tithi at sunrise (06:00 Nepal time) of an AD date — as the पात्रो shows it */
  static int tithiOfDay(int y, int m, int d){ return tithiAt(epochDay(y, m, d) * 86400000L + 900000L); }

  static String tithiName(int ti, boolean en){
    if(ti == 14) return en ? "Purnima" : "पूर्णिमा";
    if(ti == 29) return en ? "Aunsi" : "औंसी";
    return (ti < 15 ? (en ? "Shukla " : "शुक्ल ") : (en ? "Krishna " : "कृष्ण ")) + (en ? CalendarData.TITHI_EN : CalendarData.TITHI)[ti % 15];
  }

  /** festivals / holidays listed for a BS date: {kind, ne, en} rows (may be empty) */
  static java.util.List<String[]> festivals(Bs b){
    java.util.List<String[]> out = new java.util.ArrayList<>();
    for(int i = 0; i < CalendarData.FEST_DATE.length; i++){
      int[] f = CalendarData.FEST_DATE[i];
      if(f[0] == b.y && f[1] == b.m && f[2] == b.d) out.add(CalendarData.FEST_TEXT[i]);
    }
    return out;
  }

  /** Devanagari digits */
  static String npd(int n){
    String s = String.valueOf(n);
    StringBuilder o = new StringBuilder();
    for(char c : s.toCharArray()) o.append(c >= '0' && c <= '9' ? (char) ('०' + (c - '0')) : c);
    return o.toString();
  }
}
