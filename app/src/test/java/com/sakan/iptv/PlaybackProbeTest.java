package com.sakan.iptv;
import org.junit.Test;import static org.junit.Assert.*;import java.nio.charset.StandardCharsets;
public class PlaybackProbeTest {
 private String detect(String text,String type,String url)throws Exception{byte[] b=text.getBytes(StandardCharsets.UTF_8);return PlaybackProbe.detect(b,b.length,type,url);}
 @Test public void identifiesManifestEvenWhenFileExtensionIsMp4()throws Exception{assertEquals("application/x-mpegURL",detect("#EXTM3U\n#EXT-X-ENDLIST","application/octet-stream","https://x.test/7.mp4"));assertEquals("application/dash+xml",detect("<?xml version=\"1.0\"?><MPD></MPD>","text/xml","https://x.test/movie.php"));}
 @Test public void rejectsWebAndJsonErrorsWithoutEchoingPrivateBody()throws Exception{for(String text:new String[]{"<!DOCTYPE html><html>login private</html>","{\"password\":\"private\"}"})try{detect(text,"text/html","https://x.test/7.mp4");fail();}catch(PlaybackProbe.NotVideo e){assertFalse(e.getMessage().contains("private"));}}
 @Test public void recognizesProgressiveSignaturesOverMisleadingExtension()throws Exception{assertEquals("video/mp4",detect("xxxxftypisom","application/octet-stream","https://x.test/file.m3u8"));byte[] mkv={(byte)0x1a,0x45,(byte)0xdf,(byte)0xa3,0};assertEquals("video/x-matroska",PlaybackProbe.detect(mkv,mkv.length,"video/mp4","https://x.test/file.mp4"));}
}
