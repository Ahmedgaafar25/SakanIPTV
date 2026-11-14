package com.sakan.iptv;

/** Keeps a discovered release while the app is hidden; prompts once per foreground visit. */
final class UpdateNotice {
 private ReleaseInfo pending;private int offered=0;private boolean showing=false;
 void opened(){offered=0;}
 void found(ReleaseInfo release){pending=release;}
 ReleaseInfo take(int installed,boolean visible,boolean downloading){
  if(pending!=null&&pending.versionCode<=installed)pending=null;
  if(pending==null||!visible||downloading||showing||pending.versionCode==offered)return null;
  offered=pending.versionCode;showing=true;return pending;
 }
 void dismissed(){showing=false;}
 void reset(){pending=null;offered=0;}
}
