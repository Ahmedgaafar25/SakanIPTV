package com.sakan.iptv;
final class DiagnosticText {
 static String redact(String message){String value=message.replaceAll("https?://\\S+","[server address]").replaceAll("(?i)(password|username|token|authorization|mac)(\\s*[=:]\\s*)[^\\s&,;]+","$1$2[redacted]");return value.substring(0,Math.min(value.length(),240));}
}
