package com.sakan.iptv;
import android.net.Uri;
import androidx.media3.common.C;
import androidx.media3.datasource.*;
import java.net.*;
import java.io.*;
import java.util.*;

final class StreamDataSource extends BaseDataSource {
 private final Provider provider;private HttpURLConnection connection;private InputStream input;private long remaining;private boolean opened;private Uri uri;
 StreamDataSource(Provider p){super(true);provider=p;}
 @Override public long open(DataSpec spec)throws IOException {
  transferInitializing(spec);uri=spec.uri;
  try{
   Map<String,String> headers=provider.playbackHeaders(uri.toString());headers.putAll(spec.httpRequestHeaders);
   connection=StreamConnection.open(uri.toString(),headers,spec.position,spec.length);uri=Uri.parse(connection.getURL().toString());input=connection.getInputStream();
   long skip=connection.getResponseCode()==200?spec.position:0;
   while(skip>0){long n=input.skip(skip);if(n==0){if(input.read()==-1)throw new EOFException("Range exceeds stream");n=1;}skip-=n;}
   long count=-1;try{count=Long.parseLong(connection.getHeaderField("Content-Length"));}catch(Exception ignored){}
   if(connection.getResponseCode()==200&&count>=0)count-=spec.position;
   remaining=spec.length!=C.LENGTH_UNSET?spec.length:count;opened=true;transferStarted(spec);return remaining;
  }catch(IOException e){close();throw e;}
 }
 @Override public int read(byte[] buffer,int offset,int length)throws IOException{if(length==0)return 0;if(remaining==0)return C.RESULT_END_OF_INPUT;int wanted=remaining<0?length:(int)Math.min(length,remaining);int n=input.read(buffer,offset,wanted);if(n==-1)return C.RESULT_END_OF_INPUT;if(remaining>0)remaining-=n;bytesTransferred(n);return n;}
 @Override public Uri getUri(){return uri;}
 @Override public Map<String,List<String>> getResponseHeaders(){return connection==null?Collections.emptyMap():connection.getHeaderFields();}
 @Override public void close(){try{if(input!=null)input.close();}catch(Exception ignored){}finally{input=null;if(connection!=null)connection.disconnect();connection=null;if(opened){opened=false;transferEnded();}}}
}
