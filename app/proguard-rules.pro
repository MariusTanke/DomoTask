# ── Firebase & Firestore ──────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ── Domain model classes (used by Firestore toObject) ────────────────
-keep class com.mariustanke.domotask.domain.model.** { *; }

# ── Hilt / Dagger ────────────────────────────────────────────────────
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.** class * { *; }

# ── Coroutines ───────────────────────────────────────────────────────
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ── Crash reporting: keep line numbers ───────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
