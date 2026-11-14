package com.sakan.iptv;

/** Only confirmed live playback updates recall; retries and failed requests do not. */
final class ChannelRecall {
 private String current="",previous="";
 void ready(String uid){if(uid==null||uid.isEmpty()||uid.equals(current))return;previous=current;current=uid;}
 String previous(){return previous;}
 void clear(){current="";previous="";}
}
