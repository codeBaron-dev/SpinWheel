# Consumer proguard rules for spinwheel-widget library
# Keep serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep widget DTOs
-keep class com.codebaron.spinwheel.widget.data.remote.dto.** { *; }
-keep class com.codebaron.spinwheel.widget.data.local.entity.** { *; }
-keep class com.codebaron.spinwheel.widget.domain.model.** { *; }
