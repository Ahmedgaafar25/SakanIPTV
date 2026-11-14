package com.sakan.iptv;

import org.json.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Provider {
 public static class Item {
  public String id,name,category,cmd,ext,cover="",rating="",plot="",direct=""; public String parentId="";public long durationSeconds=0;boolean playbackResolved=false; public boolean folder,archive=false,censored=false;public int archiveDays=0;public String year="",genre="",cast="",director="",duration="",parent="";
  JSONObject json(String kind,boolean favorite)throws Exception{return new JSONObject().put("id",id).put("uid",(folder?"folder:":"item:")+id).put("name",name).put("category",category).put("cover",cover).put("rating",rating).put("plot",plot).put("folder",folder).put("kind",kind).put("favorite",favorite).put("ext",ext).put("archive",archive).put("archiveDays",archiveDays).put("year",year).put("genre",genre).put("cast",cast).put("director",director).put("duration",duration).put("parent",parent).put("parentId",parentId).put("durationSeconds",durationSeconds);}
  Item(String i,String n,String c,String u,String e,boolean f){id=i;name=n;category=c;cmd=u;ext=mediaExtension(e);folder=f;}
 }
 static String mediaExtension(String raw){String value=raw==null?"":raw.trim().toLowerCase(Locale.ROOT);if(value.startsWith("."))value=value.substring(1);return Arrays.asList("mp4","mkv","ts","m3u8","mpd","avi","mov","webm","m4v","mpg","mpeg","wmv","flv","3gp").contains(value)?value:"mp4";}

 private static String value(JSONObject object,String key,String fallback){String result=object.optString(key,"").trim();return result.isEmpty()||result.equalsIgnoreCase("null")?fallback:result;}
 public final JSONObject profile; public final boolean macMode; public final String base;
 volatile SubscriptionExpiry expiry=SubscriptionExpiry.unknown();
 private String endpoint="",token="",stage=""; private java.util.TimeZone serverZone=java.util.TimeZone.getDefault();private final Set<String> allowedFormats=new HashSet<>();
 public static final String UA="Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG250 stbapp ver: 4 rev: 2721 Safari/533.3";
 public Provider(JSONObject p) throws Exception {profile=p; macMode=p.optString("mode").equals("MAC"); base=normalize(p.getString("url"));}
 static final class HttpFailure extends IOException {
  final int status;final String stage;
  HttpFailure(int value,String operation){super(ConnectionDiagnostics.http(value)+" · HTTP "+value+" · "+operation);status=value;stage=operation;}
 }
 static List<String> portalEndpoints(String base)throws Exception {
  LinkedHashSet<String> paths=new LinkedHashSet<>();String r=base;
  if(r.endsWith(".php")){
   paths.add(r);
   if(r.endsWith("/server/load.php"))r=r.substring(0,r.length()-"/server/load.php".length());
   else if(r.endsWith("/portal.php"))r=r.substring(0,r.length()-"/portal.php".length());
   else return new ArrayList<>(paths);
  }
  r=r.replaceAll("/c(?:/index\\.html?)?$","");
  paths.add(r+"/server/load.php");paths.add(r+"/portal.php");
  if(new URI(r).getPath().isEmpty()){paths.add(r+"/stalker_portal/server/load.php");paths.add(r+"/stalker_portal/portal.php");}
  return new ArrayList<>(paths);
 }
 public static String normalize(String raw) throws Exception {
  String s=raw.trim();if(!s.matches("(?i)^https?://.*"))s="http://"+s;
  URI u=new URI(s); if(u.getHost()==null || u.getUserInfo()!=null || u.getQuery()!=null || u.getFragment()!=null)throw new Exception("أدخل رابط سيرفر صحيح بدون بيانات دخول أو استعلام");
  if(!u.getScheme().equalsIgnoreCase("http")&&!u.getScheme().equalsIgnoreCase("https"))throw new Exception("رابط غير مدعوم");
  return s.replaceAll("/+$","");
 }
 static String enc(String s){try{return URLEncoder.encode(s,"UTF-8");}catch(Exception e){return "";}}
 String root(){return base.replaceAll("/(player_api\\.php|get\\.php)$","");}
 String xc(String action,String extra){return root()+"/player_api.php?username="+enc(profile.optString("user"))+"&password="+enc(profile.optString("pass"))+(action.isEmpty()?"":"&action="+action)+extra;}
 Map<String,String> headers(){Map<String,String> h=new HashMap<>();h.put("User-Agent",macMode?UA:profile.optString("agent","VLC/3.0.21 LibVLC/3.0.21"));if(macMode){h.put("Cookie","mac="+enc(profile.optString("mac").toUpperCase(Locale.ROOT))+"; stb_lang=en; timezone=Asia%2FDubai");h.put("X-User-Agent","Model: MAG250; Link: Ethernet");if(!token.isEmpty())h.put("Authorization","Bearer "+token);h.put("Referer",endpoint.replaceAll("/(server/load|portal)\\.php$","")+"/c/");}return h;}
 Object request(String url) throws Exception {
  URL current=new URL(url); Map<String,String> h=headers();
  for(int redirects=0;redirects<6;redirects++) {
   HttpURLConnection c=(HttpURLConnection)current.openConnection();c.setUseCaches(false);c.setRequestProperty("Cache-Control","no-cache");c.setConnectTimeout(12000);c.setReadTimeout(20000);c.setInstanceFollowRedirects(false);c.setRequestProperty("Accept","application/json");
   for(Map.Entry<String,String> e:h.entrySet())c.setRequestProperty(e.getKey(),e.getValue());
   try {
    int status=c.getResponseCode();
    if(status>=300&&status<400){String location=c.getHeaderField("Location");if(location==null)throw new Exception("تحويل غير صالح"); URL next=new URL(current,location); if(!Arrays.asList("http","https").contains(next.getProtocol()))throw new Exception("تحويل غير مدعوم"); if(current.getProtocol().equals("https")&&next.getProtocol().equals("http"))throw new Exception("السيرفر يحاول تحويل اتصال آمن إلى غير آمن"); if(!current.getAuthority().equalsIgnoreCase(next.getAuthority())){h.remove("Authorization");h.remove("Cookie");} current=next;continue;}
    if(status!=200)throw new HttpFailure(status,macMode?"MAC / "+stage:"Xtream API");
    try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()) {byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedException();out.write(b,0,n);if(out.size()>32*1024*1024)throw new Exception("قائمة السيرفر أكبر من الحد المدعوم");}return new JSONTokener(out.toString("UTF-8").replace("\uFEFF","")).nextValue();}
   }finally{c.disconnect();}
  }throw new Exception("تحويلات كثيرة من السيرفر");
 }
 Object portal(String type,String action,String extra) throws Exception {stage=action;Object o=request(endpoint+"?type="+type+"&action="+action+"&JsHttpRequest=1-xml"+extra);if(!(o instanceof JSONObject))throw new Exception("رد البوابة غير صالح");return ((JSONObject)o).opt("js");}
 public void login() throws Exception {
  if(!macMode){Object o=request(xc("",""));if(!(o instanceof JSONObject))throw new Exception("السيرفر لم يرجع بيانات Xtream صالحة. راجع رابط السيرفر والمنفذ.");JSONObject user=((JSONObject)o).optJSONObject("user_info");if(user==null||user.optInt("auth",0)!=1)throw new Exception("اسم المستخدم أو كلمة المرور غير صحيحة");JSONObject server=((JSONObject)o).optJSONObject("server_info");if(server!=null&&!server.optString("timezone").isEmpty())serverZone=java.util.TimeZone.getTimeZone(server.optString("timezone"));JSONArray formats=user.optJSONArray("allowed_output_formats");if(formats!=null)for(int i=0;i<formats.length();i++)allowedFormats.add(formats.optString(i));expiry=SubscriptionExpiry.parse(user,true,serverZone);String status=user.optString("status","Active");if(!status.equalsIgnoreCase("Active"))throw new Exception("حالة الاشتراك: "+status);return;}
  if(!profile.optString("mac").matches("(?i)[0-9a-f]{2}(:[0-9a-f]{2}){5}"))throw new Exception("عنوان MAC غير صحيح");
  List<String> paths=portalEndpoints(base);
  Exception last=null;
  for(String p:paths){endpoint=p;token="";try{Object o=portal("stb","handshake","&token=");token=o instanceof JSONObject?((JSONObject)o).optString("token"):"";if(token.isEmpty())throw new Exception("البوابة لم ترجع رمز دخول");break;}catch(Exception e){last=e;}}
  if(token.isEmpty())throw new Exception("تعذر تسجيل MAC. راجع رابط البوابة وتفعيل اشتراكك. "+(last==null?"":last.getMessage()));
  Object info=portal("stb","get_profile","&hd=1&stb_type=MAG250&client_type=STB&image_version=218&video_out=hdmi&auth_second_step=1&sn="+enc(profile.optString("serial"))+"&device_id="+enc(profile.optString("device"))+"&device_id2="+enc(profile.optString("device")));
  if(info instanceof JSONObject)expiry=SubscriptionExpiry.parse((JSONObject)info,false,serverZone);
  if(info instanceof JSONObject && ((JSONObject)info).optInt("blocked",0)==1)throw new Exception("هذا الاشتراك موقوف لدى المزود");
 }
 public void refreshSubscription()throws Exception{Object response=macMode?portal("account_info","get_main_info",""):request(xc("",""));if(!(response instanceof JSONObject))return;JSONObject data=(JSONObject)response;if(macMode){JSONObject nested=data.optJSONObject("account_info");if(nested!=null)data=nested;}else{data=data.optJSONObject("user_info");if(data==null||data.optInt("auth",0)!=1)return;}SubscriptionExpiry value=SubscriptionExpiry.parse(data,!macMode,serverZone);if(!value.status.equals("unknown"))expiry=value;}
 String artworkUrl(String raw){try{String value=raw==null?"":raw.trim();if(value.isEmpty()||value.equals("0")||value.equalsIgnoreCase("null")||value.length()>4096)return "";URL url=new URL(new URL(base.endsWith(".php")?base:base+"/"),value.replace("&amp;","&").replace(" ","%20"));URI uri=url.toURI().normalize();String scheme=url.getProtocol().toLowerCase(Locale.ROOT);if(!Arrays.asList("http","https").contains(scheme)||uri.getUserInfo()!=null)return "";String host=url.getHost().toLowerCase(Locale.ROOT);if(!host.contains(":"))host=IDN.toASCII(host);if(host.isEmpty())return "";int port=url.getPort();if(scheme.equals("http")&&port==80||scheme.equals("https")&&port==443)port=-1;String path=uri.getRawPath();if(path==null||path.isEmpty())path="/";return new URI(scheme+"://"+host+(port<0?"":":"+port)+path+(uri.getRawQuery()==null?"":"?"+uri.getRawQuery())).toASCIIString();}catch(Exception e){return "";}}

 public LinkedHashMap<String,String> categories(String kind) throws Exception {
  LinkedHashMap<String,String> result=new LinkedHashMap<>();result.put("*","كل التصنيفات");
  Object o=macMode?portal(kind.equals("live")?"itv":"vod",kind.equals("live")?"get_genres":"get_categories",""):request(xc("get_"+kind+"_categories",""));
  if(o instanceof JSONArray){JSONArray arr=(JSONArray)o;for(int i=0;i<arr.length();i++){JSONObject j=arr.optJSONObject(i);if(j!=null)result.put(j.optString(macMode?"id":"category_id"),j.optString(macMode?"title":"category_name"));}}
  return result;
 }
 public List<Item> items(String kind,String category) throws Exception {
  List<Item> list=new ArrayList<>();
  if(!macMode){String action=kind.equals("series")?"get_series":"get_"+kind+"_streams";Object o=request(xc(action,category.equals("*")?"":"&category_id="+enc(category)));if(!(o instanceof JSONArray))throw new Exception("لا توجد قائمة صالحة من السيرفر");JSONArray arr=(JSONArray)o;
   for(int i=0;i<arr.length();i++){JSONObject j=arr.getJSONObject(i);Item item=new Item(j.optString(kind.equals("series")?"series_id":"stream_id"),j.optString("name"),j.optString("category_id"),"",j.optString("container_extension",kind.equals("live")?"ts":"mp4"),kind.equals("series"));item.cover=value(j,kind.equals("series")?"cover":"stream_icon",value(j,"movie_image",value(j,"cover_big",value(j,"cover",""))));item.rating=j.optString("rating",j.optString("rating_5based",""));item.plot=j.optString("plot","");item.direct=j.optString("direct_source","");item.archive=j.optInt("tv_archive",0)==1;item.archiveDays=j.optInt("tv_archive_duration",0);item.censored=j.optInt("is_adult",0)==1;item.year=j.optString("year",j.optString("releaseDate",""));item.genre=j.optString("genre","");list.add(item);}return list;
  }
  if(kind.equals("live")){Object o=portal("itv","get_all_channels","");JSONArray arr=o instanceof JSONObject?((JSONObject)o).optJSONArray("data"):null;if(arr==null&&o instanceof JSONArray)arr=(JSONArray)o;if(arr==null)throw new Exception("البوابة لم ترجع قنوات");for(int i=0;i<arr.length();i++){JSONObject j=arr.getJSONObject(i);String cat=j.optString("tv_genre_id");if(category.equals("*")||category.equals(cat)){Item item=new Item(j.optString("id"),j.optString("name"),cat,j.optString("cmd"),"",false);item.cover=j.optString("logo","");item.archive=j.optInt("archive",j.optInt("enable_tv_archive",0))==1;item.archiveDays=j.optInt("tv_archive_duration",j.optInt("archive_days",0));item.censored=j.optInt("censored",0)==1;list.add(item);}}return list;}
  int page=1,total=1; Set<String> seen=new HashSet<>();do {Object o=portal("vod","get_ordered_list","&category="+enc(category.equals("*")?"*":category)+"&p="+page+"&sortby=added");if(!(o instanceof JSONObject))break;JSONObject data=(JSONObject)o;JSONArray arr=data.optJSONArray("data");if(arr==null||arr.length()==0)break;int before=list.size();for(int i=0;i<arr.length();i++){JSONObject j=arr.getJSONObject(i);String id=j.optString("id");if(seen.add(id)){Item item=new Item(id,j.optString("name"),category,j.optString("cmd"),"",false);item.cover=j.optString("screenshot_uri",j.optString("cover", ""));item.plot=j.optString("description","");list.add(item);}}if(list.size()==before)break;total=(int)Math.ceil(data.optDouble("total_items",arr.length())/Math.max(1,data.optDouble("max_page_items",arr.length())));page++;if(page>500)throw new Exception("اختر تصنيف أصغر لعرض مكتبة الأفلام");}while(page<=total);return list;
 }
 public List<Item> episodes(Item series) throws Exception {
  JSONObject o=(JSONObject)request(xc("get_series_info","&series_id="+enc(series.id)));JSONObject parentInfo=o.optJSONObject("info");if(parentInfo!=null)fillInfo(series,parentInfo);JSONObject eps=o.optJSONObject("episodes");List<Item> list=new ArrayList<>();if(eps==null)return list;List<String> seasons=new ArrayList<>();Iterator<String> it=eps.keys();while(it.hasNext())seasons.add(it.next());Collections.sort(seasons,(a,b)->{try{return Integer.compare(Integer.parseInt(a),Integer.parseInt(b));}catch(Exception e){return a.compareTo(b);}});
  for(String s:seasons){JSONArray arr=eps.optJSONArray(s);if(arr==null)continue;for(int i=0;i<arr.length();i++){JSONObject j=arr.getJSONObject(i);Item episode=new Item(value(j,"id",j.optString("stream_id")),"S"+s+" · "+j.optString("title","حلقة "+(i+1)),series.category,"",j.optString("container_extension","mp4"),false);episode.cover=series.cover;episode.parent=series.name;episode.parentId=series.id;JSONObject info=j.optJSONObject("info");if(info!=null){fillInfo(episode,info);episode.ext=mediaExtension(value(j,"container_extension",value(info,"container_extension","mp4")));}episode.direct=value(j,"direct_source",info==null?"":value(info,"direct_source",""));episode.playbackResolved=true;list.add(episode);}}return list;
 }
 static Item restored(JSONObject j){Item i=new Item(j.optString("id"),j.optString("name"),j.optString("category"),"",j.optString("ext","mp4"),false);i.cover=j.optString("cover");i.parent=j.optString("parent");i.parentId=j.optString("parentId");i.durationSeconds=j.optLong("durationSeconds");i.archive=j.optBoolean("archive");i.archiveDays=j.optInt("archiveDays");return i;}
 private void fillInfo(Item item,JSONObject info){item.direct=value(info,"direct_source",item.direct);if(!value(info,"container_extension","").isEmpty())item.ext=mediaExtension(value(info,"container_extension",item.ext));item.plot=info.optString("plot",info.optString("description",item.plot));item.cover=value(info,"movie_image",value(info,"cover_big",value(info,"cover",item.cover)));item.durationSeconds=info.optLong("duration_secs",item.durationSeconds);item.year=info.optString("releasedate",info.optString("releaseDate",info.optString("year",item.year)));item.genre=info.optString("genre",item.genre);item.cast=info.optString("cast",info.optString("actors",item.cast));item.director=info.optString("director",item.director);item.rating=info.optString("rating",item.rating);item.duration=info.optString("duration",item.duration);}
 public Item details(Item item,String kind)throws Exception{if(macMode||kind.equals("live")||kind.equals("series")&&!item.folder)return item;JSONObject result=(JSONObject)request(xc(kind.equals("series")?"get_series_info":"get_vod_info","&"+(kind.equals("series")?"series_id":"vod_id")+"="+enc(item.id)));JSONObject info=result.optJSONObject("info");if(info!=null)fillInfo(item,info);JSONObject data=result.optJSONObject("movie_data");if(kind.equals("vod")&&data!=null){item.ext=mediaExtension(value(data,"container_extension",item.ext));item.direct=value(data,"direct_source",item.direct);item.playbackResolved=true;}return item;}
 public List<Programme> epg(Item channel,boolean history)throws Exception{return epg(channel,history,"");}
 public List<Programme> epg(Item channel,boolean history,String day)throws Exception{
  JSONArray rows=null;
  if(!macMode){Object response=request(xc(history?"get_simple_data_table":"get_short_epg","&stream_id="+enc(channel.id)+"&limit="+(history?200:12)));if(response instanceof JSONObject)rows=((JSONObject)response).optJSONArray("epg_listings");}
  else{if(history){java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.ROOT);f.setTimeZone(serverZone);String date=day.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")?day:f.format(new Date());rows=new JSONArray();int page=1,total=1;do{Object response=portal("epg","get_simple_data_table","&ch_id="+enc(channel.id)+"&date="+date+"&p="+page);JSONObject data=response instanceof JSONObject?(JSONObject)response:new JSONObject();JSONArray batch=data.optJSONArray("data");if(batch==null||batch.length()==0)break;for(int i=0;i<batch.length();i++)rows.put(batch.get(i));total=Math.min(10,(int)Math.ceil(data.optDouble("total_items",batch.length())/Math.max(1,batch.length())));page++;}while(page<=total);}else{Object response=portal("itv","get_short_epg","&ch_id="+enc(channel.id)+"&size=12");if(response instanceof JSONArray)rows=(JSONArray)response;else if(response instanceof JSONObject)rows=((JSONObject)response).optJSONArray("data");}}
  List<Programme> parsed=Programme.parse(rows,serverZone,channel.archive,channel.archiveDays,System.currentTimeMillis()/1000);if(history&&!macMode&&day.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")){java.text.SimpleDateFormat date=new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.ROOT);date.setTimeZone(serverZone);List<Programme> selected=new ArrayList<>();for(Programme item:parsed)if(date.format(new Date(item.start*1000)).equals(day))selected.add(item);return selected;}return parsed;
 }
 public String catchup(Item channel,Programme programme)throws Exception{
  if(!programme.archive)throw new Exception("Archive is not available for this programme");
  if(macMode){Object o=portal("tv_archive","create_link","&cmd="+enc("ffmpeg http://localhost/media/"+programme.id+".mpg")+"&download=0");return extractUrl(o instanceof JSONObject?((JSONObject)o).optString("cmd"):"");}
  java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("yyyy-MM-dd:HH-mm",Locale.ROOT);f.setTimeZone(serverZone);long minutes=Math.max(1,(programme.end-programme.start+59)/60);return root()+"/timeshift/"+enc(profile.optString("user")).replace("+","%20")+"/"+enc(profile.optString("pass")).replace("+","%20")+"/"+minutes+"/"+f.format(new Date(programme.start*1000))+"/"+enc(channel.id)+".ts";
 }
 public TimeZone timeZone(){return (TimeZone)serverZone.clone();}
 public String stream(Item item,String kind) throws Exception {
  if(!macMode)return root()+"/"+(kind.equals("vod")?"movie":kind)+"/"+enc(profile.optString("user")).replace("+","%20")+"/"+enc(profile.optString("pass")).replace("+","%20")+"/"+enc(item.id)+"."+enc(item.ext);
  Object o=portal(kind.equals("live")?"itv":"vod","create_link","&cmd="+enc(item.cmd)+"&series=0&forced_storage=0&disable_ad=0&download=0");String cmd=o instanceof JSONObject?((JSONObject)o).optString("cmd"):"";String url=extractUrl(cmd);if(!url.startsWith("http://")&&!url.startsWith("https://"))throw new Exception("البوابة لم ترجع رابط تشغيل HTTP صالح");return url;
 }
 public static String extractUrl(String cmd) throws Exception {
  java.util.regex.Matcher m=java.util.regex.Pattern.compile("https?://[^\\s\"']+",java.util.regex.Pattern.CASE_INSENSITIVE).matcher(cmd);
  if(!m.find())throw new Exception("لم ترجع البوابة رابط HTTP صالحًا");return m.group().replace("&amp;","&");
 }
 public List<String> streams(Item item,String kind,String preference) throws Exception {
  LinkedHashSet<String> urls=new LinkedHashSet<>();
  if(!macMode&&!item.playbackResolved&&!kind.equals("live")){try{if(kind.equals("vod"))details(item,kind);else if(!item.parentId.isEmpty()){Item parent=new Item(item.parentId,item.parent,item.category,"","mp4",true);for(Item episode:episodes(parent))if(episode.id.equals(item.id)){item.ext=episode.ext;item.direct=episode.direct;item.playbackResolved=true;break;}}}catch(Exception ignored){}}
  if(macMode){urls.add(stream(item,kind));return new ArrayList<>(urls);}
  boolean directValid=item.direct.startsWith("https://")||item.direct.startsWith("http://");
  if(directValid&&(preference.equals("auto")||!kind.equals("live")))urls.add(item.direct);
  String original=stream(item,kind);
  if(kind.equals("live")){
   String stem=original.substring(0,original.lastIndexOf('.'));
   if(preference.equals("ts")){urls.add(stem+".ts");urls.add(stem+".m3u8");}
   else if(preference.equals("hls")||allowedFormats.isEmpty()||allowedFormats.contains("m3u8")){urls.add(stem+".m3u8");urls.add(stem+".ts");}
   else{urls.add(stem+".ts");urls.add(stem+".m3u8");}
  }else urls.add(original);
  if(directValid)urls.add(item.direct);
  return new ArrayList<>(urls);
 }
 public static String mime(String url){String s=url.toLowerCase(Locale.ROOT);if(s.contains(".m3u8")||s.matches(".*[?&](output|extension)=m3u8.*"))return "application/x-mpegURL";if(s.contains(".mpd"))return "application/dash+xml";return null;}
 public Map<String,String> playbackHeaders(String url){Map<String,String> h=new HashMap<>();h.put("User-Agent",macMode?UA:profile.optString("agent","VLC/3.0.21 LibVLC/3.0.21"));try{if(macMode&&new URI(url).getAuthority().equalsIgnoreCase(new URI(endpoint).getAuthority()))h.putAll(headers());}catch(Exception ignored){}return h;}
}
