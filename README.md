# المسجل الصوتي (VoiceRecorder)

## طريقة التشغيل
1. افتح مجلد VoiceRecorder في Android Studio (Ladybug أو أحدث).
2. Android Studio هيعمل Gradle Sync ويولد ملفات gradle wrapper تلقائياً.
3. وصّل موبايل أندرويد بوضع المطور أو استخدم Emulator، واضغط Run.
4. أو من الترمنال: `./gradlew assembleDebug` وهيطلع الـ APK في `app/build/outputs/apk/debug/`.

## الملفات
- `RecordingService.kt`: خدمة Foreground للتسجيل، تكمل حتى لو قفلت الشاشة أو خرجت من التطبيق.
- `Scheduler.kt` و `ScheduleReceiver.kt`: التسجيل التلقائي في وقت محدد ولمدة محددة.
- `MainActivity.kt` و `RecAdapter.kt`: الواجهة، قائمة التسجيلات، تشغيل / حذف / مشاركة.
- `Storage.kt`: مكان الحفظ، المجلد `Recordings` باسم التاريخ والوقت بصيغة M4A (AAC).

## مكان الملفات
التسجيلات تتحفظ في: `Android/data/com.example.voicerecorder/files/Recordings/`
وده مجلد خاص بالتطبيق، فمش محتاج أي إذن تخزين.

## ملاحظات مهمة
- أندرويد 14 وما بعده بيفرض إشعار ظاهر للمستخدم أثناء أي تسجيل بالخلفية، فمش ممكن التسجيل يتم بدون إشعار.
- التسجيل التلقائي المجدول يحتاج إذن "المنبهات والتذكيرات" (SCHEDULE_EXACT_ALARM) من إعدادات الجهاز عشان يشتغل في الوقت بالظبط.
- التطبيق اتبنى بنفس منطق أندرويد الرسمي، لكن ما تمش تجربته على جهاز حقيقي، فلازم تجربه وتبلغ لو فيه أي مشكلة.
