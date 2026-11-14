package com.sakan.iptv;
import org.junit.Test;import static org.junit.Assert.*;import org.json.*;import java.util.*;

public class SubscriptionExpiryTest {
 private final TimeZone utc=TimeZone.getTimeZone("UTC");
 @Test public void distinguishesMissingAndExplicitUnlimited()throws Exception{assertEquals("unknown",SubscriptionExpiry.parse(new JSONObject(),true,utc).status);assertEquals("unlimited",SubscriptionExpiry.parse(new JSONObject().put("exp_date",JSONObject.NULL),true,utc).status);assertEquals("unknown",SubscriptionExpiry.parse(new JSONObject().put("end_date",JSONObject.NULL),false,utc).status);}
 @Test public void normalizesSecondsMillisecondsAndPortalDates()throws Exception{long expected=1893456000000L;assertEquals(expected,SubscriptionExpiry.parse(new JSONObject().put("exp_date","1893456000"),true,utc).at);assertEquals(expected,SubscriptionExpiry.parse(new JSONObject().put("exp_date",expected),true,utc).at);assertEquals(expected,SubscriptionExpiry.parse(new JSONObject().put("expire_billing_date","2030-01-01 00:00:00"),false,utc).at);}
 @Test public void rejectsInvalidDatesAndIgnoresPersonalFields()throws Exception{for(String text:Arrays.asList("bad","2030-02-31","0000-00-00 00:00:00","0"))assertEquals("unknown",SubscriptionExpiry.parse(new JSONObject().put("expire_billing_date",text),false,utc).status);JSONObject data=new JSONObject().put("phone","971501234567").put("password","secret");assertEquals("unknown",SubscriptionExpiry.parse(data,false,utc).status);assertEquals(2,SubscriptionExpiry.parse(data,false,utc).json().length());}
}
