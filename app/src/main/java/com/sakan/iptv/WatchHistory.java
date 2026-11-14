package com.sakan.iptv;
import org.json.*;
final class WatchHistory {
 static boolean resumable(long position,long duration){return position>=10000&&(duration<=0||position<duration-Math.min(90000,Math.max(10000,duration/20))&&position<duration*0.95);}
 static JSONArray remember(JSONArray old,JSONObject entry,int cap)throws Exception{JSONArray out=new JSONArray();out.put(entry);String identity=entry.getString("kind")+":"+entry.getString("uid");for(int i=0;i<old.length()&&out.length()<cap;i++){JSONObject e=old.optJSONObject(i);if(e!=null&&!identity.equals(e.optString("kind")+":"+e.optString("uid")))out.put(e);}return out;}
 static JSONObject find(JSONArray history,String kind,String uid){for(int i=0;i<history.length();i++){JSONObject e=history.optJSONObject(i);if(e!=null&&kind.equals(e.optString("kind"))&&uid.equals(e.optString("uid")))return e;}return null;}
}
