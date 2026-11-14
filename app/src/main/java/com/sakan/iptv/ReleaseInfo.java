package com.sakan.iptv;
import org.json.*;
import java.net.URI;

/** Public GitHub release metadata; never accepts an access token. */
final class ReleaseInfo {
 final int versionCode;final String versionName,apkName,sha256,url;final long bytes;
 private ReleaseInfo(int code,String version,String name,String sha,String link,long size){versionCode=code;versionName=version;apkName=name;sha256=sha;url=link;bytes=size;}
 static String repository(String raw){String r=raw.trim();if(!r.matches("[A-Za-z0-9][A-Za-z0-9-]{0,38}/[A-Za-z0-9_.-]{1,100}")||r.endsWith("/.")||r.endsWith("/.."))throw new IllegalArgumentException("Use owner/repository, without a URL or token");return r;}
 static String trustedAsset(String value,String repo)throws Exception{URI u=new URI(value);String prefix="/"+repository(repo)+"/releases/download/";if(!"https".equals(u.getScheme())||!"github.com".equalsIgnoreCase(u.getHost())||u.getUserInfo()!=null||u.getPort()!=-1||u.getFragment()!=null||!u.getPath().toLowerCase(java.util.Locale.ROOT).startsWith(prefix.toLowerCase(java.util.Locale.ROOT))||u.getPath().contains("/../"))throw new Exception("Release asset must belong to the configured GitHub repository");return value;}
 static JSONArray assets(JSONObject release)throws Exception{if(release.optBoolean("draft")||release.optBoolean("prerelease"))throw new Exception("Only stable published releases are supported");return release.getJSONArray("assets");}
 static String manifestUrl(JSONObject release,String repo)throws Exception{JSONArray list=assets(release);for(int i=0;i<list.length();i++){JSONObject a=list.getJSONObject(i);if(a.optString("name").equals("update.json"))return trustedAsset(a.getString("browser_download_url"),repo);}throw new Exception("The latest release has no update.json manifest");}
 static ReleaseInfo parse(JSONObject manifest,JSONObject release,String repo)throws Exception{
  if(!"com.sakan.iptv".equals(manifest.getString("packageName")))throw new Exception("Update package mismatch");int code=manifest.getInt("versionCode");String version=manifest.getString("versionName"),name=manifest.getString("apkFileName"),sha=manifest.getString("sha256");long size=manifest.getLong("sizeBytes");
  if(code<=0||version.isEmpty()||!name.matches("[A-Za-z0-9_.-]+\\.apk")||!sha.matches("[0-9a-fA-F]{64}")||size<1||size>50L*1024*1024)throw new Exception("Invalid update manifest");JSONArray list=assets(release);for(int i=0;i<list.length();i++){JSONObject a=list.getJSONObject(i);if(name.equals(a.optString("name"))){long advertised=a.optLong("size",size);if(advertised!=size)throw new Exception("Update size mismatch");return new ReleaseInfo(code,version,name,sha.toLowerCase(java.util.Locale.ROOT),trustedAsset(a.getString("browser_download_url"),repo),size);}}throw new Exception("The APK named in the manifest is missing from the release");
 }
}
