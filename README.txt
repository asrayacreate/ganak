GANAK v3.9 — LIVE गर्ने अन्तिम निर्देशन
=========================================
यो folder हुबहु host गर्नुहोस् (HTTPS अनिवार्य)।

★ सबैभन्दा सजिलो — Netlify (free):
  1) app.netlify.com → "Add new site" → "Deploy manually"
  2) यो पूरै folder drag-drop गर्नुहोस्
  3) 1 मिनेटमा https://XXXX.netlify.app URL आउँछ — त्यही नै live app हो
  4) Phone मा खोल्दा "Install" prompt आउँछ (PWA, offline चल्छ)

★ Play Store (चाहेमा):
  1) npx @bubblewrap/cli init --manifest https://YOUR-URL/manifest.json
  2) npx @bubblewrap/cli build  →  .aab बन्छ
  3) play.google.com/console ($25 one-time) → app बनाएर .aab upload
  4) Listing assets यहीँ छन्: icon-512.png, feature-graphic.png, screenshots/
  5) Privacy policy URL: https://YOUR-URL/privacy.html  (file included छ)

★ Live भएपछि (एक पटक):
  - Settings ⚙ → "सामग्रीको दर" मा आफ्नो बजार दर हालेर "दर Save ✓" थिच्नुहोस्
    → सबै ⚠️ नमुना-दर warning हट्छ
  - Settings → Profile मा नाम/व्यवसाय/logo save गर्नुहोस्

भर्सन: v3.9 · cache ganak-v3-9 · 21 tools · पूर्ण offline


TWA / PLAY STORE:
- .well-known/assetlinks.json ftp/hosting ma sangai upload garnuhos (URL: https://YOURDOMAIN/.well-known/assetlinks.json ma khulnu parchha)
- Package: io.github.asrayacreate.twa
- Yo file thik bhaye TWA app ma browser address bar dekhidaina.
