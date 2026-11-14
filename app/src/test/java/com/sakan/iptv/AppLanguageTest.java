package com.sakan.iptv;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
public class AppLanguageTest {
 private AppLanguage strings()throws Exception{try(InputStream in=new FileInputStream("src/main/assets/i18n.json");ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return new AppLanguage(out.toString("UTF-8"));}}
 @Test public void defaultsToEnglishAndAllowsExplicitArabic(){assertEquals("en",AppLanguage.language(""));assertEquals("en",AppLanguage.language("fr"));assertEquals("ar",AppLanguage.language("ar"));}
 @Test public void nativePlaybackLabelsUseTheAppLanguage()throws Exception{AppLanguage s=strings();assertEquals("Rotate screen",s.text("تدوير الشاشة","en"));assertEquals("Playback diagnostics",s.text("تشخيص التشغيل","en"));assertEquals("رجوع",s.text("رجوع","ar"));}
 @Test public void completeNetworkMessagesAreTranslatedBeforeShortFragments()throws Exception{AppLanguage s=strings();String m=s.text(ConnectionDiagnostics.http(511)+" · HTTP 511","en");assertTrue(m.contains("network access authentication"));assertFalse(m.matches(".*[\\u0600-\\u06ff].*"));assertTrue(m.endsWith("HTTP 511"));assertTrue(s.text(ConnectionDiagnostics.tls(new javax.net.ssl.SSLException("Unable to parse TLS packet header")),"en").contains("TLS response"));}
}
