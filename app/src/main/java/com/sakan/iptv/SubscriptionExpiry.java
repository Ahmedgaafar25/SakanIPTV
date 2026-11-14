package com.sakan.iptv;
import org.json.*;import java.util.*;import java.text.*;

final class SubscriptionExpiry {
 final String status;final long at;
 private SubscriptionExpiry(String status,long at){this.status=status;this.at=at;}
 static SubscriptionExpiry unknown(){return new SubscriptionExpiry("unknown",0);}
 static SubscriptionExpiry parse(JSONObject data,boolean xtream,TimeZone zone){if(data==null)return unknown();for(String field:xtream?new String[]{"exp_date"}:new String[]{"expire_billing_date","expirydate","end_date","exp_date"}){if(!data.has(field))continue;Object value=data.opt(field);if(value==JSONObject.NULL)return xtream?new SubscriptionExpiry("unlimited",0):unknown();String text=String.valueOf(value).trim();if(text.equalsIgnoreCase("unlimited")||text.equalsIgnoreCase("never"))return new SubscriptionExpiry("unlimited",0);try{long stamp=Long.parseLong(text);if(stamp>100000000000L)stamp/=1000;if(stamp>0&&stamp<10413792000L)return new SubscriptionExpiry("dated",stamp*1000);}catch(Exception ignored){}for(String pattern:new String[]{"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd","dd.MM.yyyy","MMMM d, yyyy"})try{SimpleDateFormat format=new SimpleDateFormat(pattern,Locale.ENGLISH);format.setLenient(false);format.setTimeZone(zone);ParsePosition pos=new ParsePosition(0);Date date=format.parse(text,pos);if(date!=null&&pos.getIndex()==text.length()&&date.getTime()>0)return new SubscriptionExpiry("dated",date.getTime());}catch(Exception ignored){}}return unknown();}
 JSONObject json(){try{return new JSONObject().put("status",status).put("at",at);}catch(Exception e){return new JSONObject();}}
}
