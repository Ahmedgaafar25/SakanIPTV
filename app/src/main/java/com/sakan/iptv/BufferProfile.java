package com.sakan.iptv;

final class BufferProfile {
 final String id; final int minimum,maximum,start,rebuffer;
 private BufferProfile(String id,int min,int max,int start,int retry){this.id=id;minimum=min;maximum=max;this.start=start;rebuffer=retry;}
 static BufferProfile of(String id){
  if("fast".equals(id))return new BufferProfile("fast",10000,20000,750,1500);
  if("stable".equals(id))return new BufferProfile("stable",60000,90000,5000,10000);
  return new BufferProfile("balanced",30000,50000,2500,5000);
 }
}
