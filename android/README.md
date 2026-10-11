# Ganak — Android app (Google Play) + "आजको मिति" widget + status-bar date

The same app PWABuilder made (package `io.github.asrayacreate.twa`, opens
https://asrayacreate.github.io/ganak/ full screen through Chrome, verified by
`/.well-known/assetlinks.json`), plus a **home-screen widget**: today's Nepali date
as a calendar tile, the tithi, any festival/holiday and the AD date. It changes by
itself after midnight (Nepal time); tapping it opens the पात्रो. And **today's date always at
the top of the phone**: the status bar shows the Nepali day number (२५) all day, every day.

GitHub builds it on every change (Actions → "Android app (TWA + widget)"); the
files are under the run's **Artifacts**.

## एक पटक: Play मा पठाउन मिल्ने गरी sign गर्न (upload key)

PWABuilder बाट download गरेको zip मा `signing.keystore` (वा `.keystore`) र
`signing-key-info.txt` हुन्छ। त्यही key ले sign गरेको file मात्र Play ले update का रूपमा लिन्छ।

1. Keystore लाई base64 बनाउनुहोस्:
   - Windows PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("signing.keystore")) | Set-Clipboard`
   - Mac/Linux: `base64 -w0 signing.keystore` (Mac मा `base64 -i signing.keystore`)
2. GitHub → यो repository → **Settings → Secrets and variables → Actions → New repository secret**:
   - `ANDROID_KEYSTORE_BASE64` — माथिको base64 text
   - `ANDROID_KEYSTORE_PASSWORD` — signing-key-info.txt को "Key store password"
   - `ANDROID_KEY_ALIAS` — "Key alias"
   - `ANDROID_KEY_PASSWORD` — "Key password"
3. **Actions → Android app (TWA + widget) → Run workflow**.
4. Run सकिएपछि Artifacts बाट zip download → भित्रको `app-release.aab` लाई
   **Play Console → Production (वा Internal testing) → Create new release** मा upload।

Version code = 1000 + run number (Play मा पहिले भएको भन्दा ठूलो हुन्छ)।
Keystore कहिल्यै repository मा commit नगर्नुहोस् — secret मा मात्र राख्नुहोस्।

## Widget राख्ने
Home screen मा खाली ठाउँ थिचिराख्नुहोस् → **Widgets** → **Ganak — आजको मिति** → तानेर राख्नुहोस्।
Size ठूलो-सानो गर्न मिल्छ: सानोमा मिति मात्र, ठूलोमा तिथि, चाडपर्व र अंग्रेजी मिति पनि।

## स्क्रिनको माथि (status bar) मा आजको मिति — सधैं
App खोल्नुहोस् → **आज** card वा ☰ Settings मा **📲 फोनको माथि आजको मिति सधैं देखाउने** थिच्नुहोस्
(पहिलो पटक "Allow notifications" मा Allow)। त्यसपछि status bar मा आजको गते (जस्तै **२५**) सधैं
देखिन्छ; तल तान्दा "आज असोज २५ · आइतबार", वि.सं., तिथि र चाडपर्व। आवाज/कम्पन छैन, मध्यरातमा आफैँ
बदलिन्छ, phone restart पछि पनि आफैँ आउँछ। Swipe गरेर हटाए पनि तुरुन्तै फर्किन्छ — बन्द गर्न
app मा फेरि त्यही button थिच्नुहोस्।

Android ले app लाई status bar मा घडी (११:५३) जस्तो अक्षर लेख्न दिँदैन — एउटा सानो icon मात्र
दिन्छ। त्यसैले त्यो icon नै आजको गते (२५) बनाइएको छ, र तल तान्दा पूरा मिति देखिन्छ।

## Files
- `tools/gen_calendar.js` — BS month table, festivals and names copied from `index.html` into
  `CalendarData.java` (the build runs it, so the widget always matches the web app)
- `app/src/main/java/.../NepaliCal.java` — AD→BS and tithi (same method as the web पात्रो)
- `app/src/main/java/.../Today.java` — today in Nepal (texts shared by the widget and the status bar)
- `app/src/main/java/.../DateWidget.java` — the widget; also the midnight / restart refresh
- `app/src/main/java/.../DateBar.java` — the status-bar date (ongoing quiet notification, day-number icon)
- `app/src/main/java/.../DateBarActivity.java` — `ganak://datebar/on|off`, the web app's 📲 switch
- `app/src/main/java/.../LauncherActivity.java` — opens the web app with `?app=2` (so it knows it's in the app)
