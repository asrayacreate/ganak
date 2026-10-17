# R8 rules for Ganak. androidbrowserhelper and androidx bring their own consumer rules; the manifest's
# activities, receiver, service and provider are kept by the build. Our own package is tiny, so keep it
# whole: the widget (RemoteViews), the status-bar date and the calendar tables must survive as written.
-keep class io.github.asrayacreate.twa.** { *; }
