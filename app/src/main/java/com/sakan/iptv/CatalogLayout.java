package com.sakan.iptv;
import org.json.*;
import java.util.*;

/** Account/kind scoped layout, independent of provider data and playback URLs. */
final class CatalogLayout {
 private final JSONObject data;
 private final Map<String,Set<String>> hiddenCache=new HashMap<>();
 CatalogLayout(String saved){JSONObject parsed;try{parsed=new JSONObject(saved);}catch(Exception e){parsed=new JSONObject();}data=parsed;}
 private String orderKey(boolean categories){return categories?"categoryOrder":"itemOrder";}
 private String hiddenKey(boolean categories){return categories?"hiddenCategories":"hiddenItems";}
 private List<String> values(String key){List<String> result=new ArrayList<>();Set<String> seen=new HashSet<>();JSONArray a=data.optJSONArray(key);if(a!=null)for(int i=0;i<a.length();i++){String s=a.optString(i);if(!s.isEmpty()&&seen.add(s))result.add(s);}return result;}
 private void put(String key,List<String> values){try{data.put(key,new JSONArray(values));hiddenCache.remove(key);}catch(JSONException e){throw new IllegalStateException(e);}}
 boolean hidden(boolean categories,String id){String key=hiddenKey(categories);Set<String> set=hiddenCache.get(key);if(set==null){set=new HashSet<>(values(key));hiddenCache.put(key,set);}return set.contains(id);}
 void hide(boolean categories,String id,boolean hide){List<String> list=values(hiddenKey(categories));list.remove(id);if(hide)list.add(id);put(hiddenKey(categories),list);}
 List<String> ordered(boolean categories,Collection<String> available){List<String> result=new ArrayList<>();Set<String> seen=new HashSet<>(),availableSet=new HashSet<>(available);for(String id:values(orderKey(categories)))if(availableSet.contains(id)&&seen.add(id))result.add(id);for(String id:available)if(seen.add(id))result.add(id);return result;}
 void move(boolean categories,String id,String before,Collection<String> available){if(id.equals(before))return;List<String> current=ordered(categories,available);if(!current.remove(id))return;int position=before==null?current.size():current.indexOf(before);if(position<0)position=current.size();current.add(position,id);List<String> missing=values(orderKey(categories));missing.removeAll(current);current.addAll(missing);put(orderKey(categories),current);}
 void reset(boolean categories){data.remove(orderKey(categories));data.remove(hiddenKey(categories));hiddenCache.remove(hiddenKey(categories));}
 @Override public String toString(){return data.toString();}
}
