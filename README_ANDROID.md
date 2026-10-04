# RideClick Android

نسخة Android Native أولية لـ RideClick باستخدام Kotlin + Jetpack Compose.

## الحالة الحالية

- شاشة إعدادات عربية RTL.
- حفظ محلي للإعدادات.
- RuleEngine مطابق لمنطق Prototype.
- TextRequestParser للنصوص العربية/الإنجليزية البسيطة.
- AccessibilityService بوضع **مراقبة وقراءة فقط**.
- Adapters منفصلة لـ Jeeny وPetra Ride.
- لا يوجد auto-click أو auto-accept.
- لا يوجد Firebase أو قاعدة بيانات أو اشتراك أو إعلانات.

## البناء

افتح مجلد `android/` في Android Studio حديث، ثم نفّذ Gradle Sync وبناء Debug.

المتطلبات المقترحة:
- Android Studio حديث يدعم AGP 8.7.x.
- JDK 17.
- Android SDK 35.

## Accessibility

بعد تثبيت التطبيق:
1. افتح إعدادات Android.
2. Accessibility.
3. Installed/Downloaded apps.
4. فعّل RideClick.
5. ارجع للتطبيق.

الخدمة تقرأ شجرة الواجهة فقط عند وصول Accessibility events، وتحاول استخراج السعر وETA والمسافة.

## الاختبار

شغّل Unit Tests من Android Studio.

## حدود التكامل

أسماء حزم التطبيقات ومواقع النصوص وعناصر الواجهة الخاصة بـ Jeeny/Petra Ride لم يتم افتراضها. يجب اختبارها على جهاز حقيقي ولقطات/واجهات حالية قبل تفعيل أي Adapter فعلي.

المرحلة الحالية لا تنفذ قبولًا تلقائيًا ولا ترسل gestures أو نقرات إلى التطبيقات الأخرى.

## الخصوصية

لا يتم إرسال البيانات إلى خادم. يفضّل عدم حفظ نصوص الواجهة الخام؛ في الإنتاج يجب تخزين أقل قدر ممكن من بيانات التشخيص.

## الخطوة التالية

اختبار Accessibility على جهاز Android حقيقي، تحديد بنية الطلب في كل تطبيق، ثم بناء parser/adapters موثقة لكل منصة. بعد نجاح المراقبة، يمكن تصميم خطوة قبول اختيارية وواضحة للمستخدم مع مراجعة سياسات المنصات.
