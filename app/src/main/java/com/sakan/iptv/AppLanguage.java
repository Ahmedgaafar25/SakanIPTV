package com.sakan.iptv;
import java.util.*;
import org.json.*;

/** Translates app-owned strings only. Provider names and credentials are never passed here. */
final class AppLanguage {
 private final List<Map.Entry<String,String>> entries=new ArrayList<>();
 AppLanguage(String json)throws Exception{JSONObject data=new JSONObject(json);Iterator<String> keys=data.keys();while(keys.hasNext()){String key=keys.next();entries.add(new AbstractMap.SimpleImmutableEntry<>(key,data.getString(key)));}Collections.sort(entries,(a,b)->Integer.compare(b.getKey().length(),a.getKey().length()));}
 String text(String source,String language){if(source==null||language.equals("ar"))return source;String output=source;for(Map.Entry<String,String> e:entries)output=output.replace(e.getKey(),e.getValue());return output;}
 static String language(String input){return "ar".equals(input)?"ar":"en";}
}
