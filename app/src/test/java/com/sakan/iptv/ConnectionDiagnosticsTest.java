package com.sakan.iptv;
import org.junit.Test;
import static org.junit.Assert.*;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.util.*;
public class ConnectionDiagnosticsTest {
 @Test public void plaintextTlsOffersProtocolCorrection(){SSLException e=new SSLException("Unable to parse TLS packet header");assertTrue(ConnectionDiagnostics.plaintextTls(e));assertTrue(ConnectionDiagnostics.tls(e).contains("HTTP"));}
 @Test public void certificateFailureDoesNotOfferPlaintext(){SSLException e=new SSLHandshakeException("Trust anchor for certification path not found");assertFalse(ConnectionDiagnostics.plaintextTls(e));assertTrue(ConnectionDiagnostics.tls(e).contains("شهادة"));}
 @Test public void networkAuthenticationIsDistinctFromServerFault(){String m=ConnectionDiagnostics.http(511);assertTrue(m.contains("تصريح وصول"));assertFalse(m.equals(ConnectionDiagnostics.http(500)));}
 @Test public void custom488DoesNotInventCause(){assertTrue(ConnectionDiagnostics.http(488).contains("غير قياسي"));assertTrue(ConnectionDiagnostics.http(488).contains("لا يحدد"));}
 @Test public void portalCIndexAndExplicitEndpointsNormalize()throws Exception{
  List<String> urls=Provider.portalEndpoints("http://example.test/c/index.html");assertEquals("http://example.test/server/load.php",urls.get(0));assertTrue(urls.contains("http://example.test/portal.php"));assertTrue(urls.contains("http://example.test/stalker_portal/server/load.php"));
  urls=Provider.portalEndpoints("http://example.test/stalker_portal/c");assertEquals("http://example.test/stalker_portal/server/load.php",urls.get(0));
  urls=Provider.portalEndpoints("http://example.test/stalker_portal/server/load.php");assertEquals("http://example.test/stalker_portal/server/load.php",urls.get(0));assertTrue(urls.contains("http://example.test/stalker_portal/portal.php"));
 }
}
