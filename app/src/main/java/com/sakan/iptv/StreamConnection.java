package com.sakan.iptv;
import java.net.*;
import java.io.*;
import java.util.*;

/** HTTP redirects with per-origin credential stripping; independent of Android for tests. */
final class StreamConnection {
 static final class Status extends IOException {final int code;Status(int c){super("HTTP "+c);code=c;}}
 static HttpURLConnection open(String url,Map<String,String> initial,long position,long length)throws IOException {
  URL current=new URL(url);Map<String,String> headers=new HashMap<>(initial);
  for(int i=0;i<8;i++){
   HttpURLConnection c=(HttpURLConnection)current.openConnection();c.setConnectTimeout(12000);c.setReadTimeout(15000);c.setInstanceFollowRedirects(false);
   c.setRequestProperty("Accept-Encoding","identity");
   for(Map.Entry<String,String> h:headers.entrySet())c.setRequestProperty(h.getKey(),h.getValue());
   if(position>0||length>=0)c.setRequestProperty("Range","bytes="+position+"-"+(length>=0?Long.toString(position+length-1):""));
   int status;
   try{status=c.getResponseCode();}catch(IOException e){c.disconnect();throw e;}
   if(status==301||status==302||status==303||status==307||status==308){
    String location=c.getHeaderField("Location");if(location==null){c.disconnect();throw new IOException("Missing redirect location");}
    URL next=new URL(current,location);c.disconnect();
    if(!next.getProtocol().equals("http")&&!next.getProtocol().equals("https"))throw new IOException("Unsupported redirect protocol");
    // IPTV servers commonly redirect between HTTP and HTTPS. TLS validation stays enabled.
    if(!current.getAuthority().equalsIgnoreCase(next.getAuthority())||!current.getProtocol().equalsIgnoreCase(next.getProtocol())){
     Iterator<String> keys=headers.keySet().iterator();while(keys.hasNext()){String k=keys.next();if(k.equalsIgnoreCase("Cookie")||k.equalsIgnoreCase("Authorization"))keys.remove();}
    }
    current=next;continue;
   }
   if(status<200||status>299){c.disconnect();throw new Status(status);}return c;
  }throw new IOException("Too many redirects");
 }
}
