package com.sakan.iptv;
import java.util.Locale;
import javax.net.ssl.SSLException;

/** Evidence-based connection messages; no credentials or URLs in diagnostics. */
final class ConnectionDiagnostics {
 static boolean plaintextTls(Throwable failure){
  for(Throwable t=failure;t!=null;t=t.getCause()){
   String m=t.getMessage()==null?"":t.getMessage().toLowerCase(Locale.ROOT);
   if(m.contains("unable to parse tls packet header")||m.contains("unrecognized ssl message")||m.contains("plaintext connection")||m.contains("wrong version number"))return true;
  }return false;
 }
 static boolean ssl(Throwable failure){for(Throwable t=failure;t!=null;t=t.getCause())if(t instanceof SSLException)return true;return false;}
 static String tls(Throwable failure){return plaintextTls(failure)?"المنفذ لم يرجع رد TLS صالحًا. غالبًا الرابط يستخدم HTTPS على منفذ HTTP. استخدم البروتوكول والمنفذ اللذين حددهما مزودك؛ يمكنك تعديل الرابط إلى HTTP إذا كان هو رابط الخدمة الصحيح.":"فشل اتصال TLS. راجع بروتوكول الرابط والمنفذ وصلاحية شهادة السيرفر. لا يتم تعطيل التحقق من الشهادات.";}
 static String http(int code){
  switch(code){
   case 401:case 403:return "رُفض طلب الوصول. راجع بيانات الاشتراك وتفعيل الجهاز مع مزودك.";
   case 404:return "المسار أو رابط المحتوى غير موجود. راجع رابط البوابة والمنفذ.";
   case 429:return "السيرفر حدّد عدد الطلبات أو الاتصالات. أوقف التشغيل على الأجهزة الأخرى ثم أعد المحاولة.";
   case 488:return "رد HTTP 488 غير قياسي؛ قد يصدر من البوابة أو وسيط الاتصال. تأكد من رابط MAC الصحيح وتفعيله لدى المزود. الكود وحده لا يحدد سبب الرفض.";
   case 511:return "HTTP 511 يشير إلى طلب تصريح وصول للشبكة. قد يصدر من بوابة شبكة أو وسيط اتصال، وقد يستخدمه المزود بمعنى خاص. جرّب شبكة أخرى، ثم تحقق من رابط البث والتفعيل مع المزود. تغيير صيغة الفيديو لا يفسّر هذا الرفض.";
   default:return code>=500?"خطأ من خادم أو وسيط اتصال أثناء الطلب.":"الطرف الذي استقبل الطلب أعاد خطأ HTTP.";
  }
 }
}
