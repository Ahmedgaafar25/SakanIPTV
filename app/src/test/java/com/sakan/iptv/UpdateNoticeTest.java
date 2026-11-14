package com.sakan.iptv;
import org.junit.Test;import static org.junit.Assert.*;import org.json.*;

public class UpdateNoticeTest {
 private ReleaseInfo release(int version)throws Exception{String name="SakanIPTV-"+version+".apk";String url="https://github.com/Owner/Player/releases/download/v"+version+"/"+name;JSONObject asset=new JSONObject().put("name",name).put("size",1234).put("browser_download_url",url);return ReleaseInfo.parse(new JSONObject().put("packageName","com.sakan.iptv").put("versionCode",version).put("versionName",String.valueOf(version)).put("apkFileName",name).put("sizeBytes",1234).put("sha256",new String(new char[64]).replace('\0','a')),new JSONObject().put("assets",new JSONArray().put(asset)),"Owner/Player");}
 @Test public void retainsResultReceivedInBackgroundUntilVisible()throws Exception{UpdateNotice n=new UpdateNotice();n.opened();n.found(release(9));assertNull(n.take(8,false,false));assertEquals(9,n.take(8,true,false).versionCode);}
 @Test public void laterDoesNotRepeatInSameVisitButRemindsOnReopening()throws Exception{UpdateNotice n=new UpdateNotice();n.opened();n.found(release(9));assertNotNull(n.take(8,true,false));n.dismissed();n.found(release(9));assertNull(n.take(8,true,false));n.opened();assertNotNull(n.take(8,true,false));}
 @Test public void suppressesDuplicatesWhileDialogIsVisible()throws Exception{UpdateNotice n=new UpdateNotice();n.found(release(9));assertNotNull(n.take(8,true,false));n.opened();n.found(release(10));assertNull(n.take(8,true,false));n.dismissed();assertEquals(10,n.take(8,true,false).versionCode);}
 @Test public void doesNotPromptDuringDownloadAndDropsInstalledVersion()throws Exception{UpdateNotice n=new UpdateNotice();n.found(release(9));assertNull(n.take(8,true,true));assertNotNull(n.take(8,true,false));n.dismissed();n.opened();assertNull(n.take(9,true,false));assertNull(n.take(10,true,false));}
 @Test public void repositoryResetDiscardsOldResult()throws Exception{UpdateNotice n=new UpdateNotice();n.found(release(9));n.reset();assertNull(n.take(8,true,false));n.found(release(10));assertEquals(10,n.take(8,true,false).versionCode);}
}
