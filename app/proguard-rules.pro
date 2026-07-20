# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# PDFBox
-dontwarn org.apache.pdfbox.**
-keep class com.tom_roush.pdfbox.** { *; }
