package com.sakan.iptv;
import org.junit.*;
import static org.junit.Assert.*;
import org.json.*;
import java.io.*;
import java.util.concurrent.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class ProviderTest {
 static class HttpServer {
  ServerSocket socket;volatile Handler handler;Thread thread;volatile boolean running;
  interface Handler {void accept(Exchange e)throws IOException;}
  static HttpServer create(InetSocketAddress addr,int backlog)throws IOException{HttpServer s=new HttpServer();s.socket=new ServerSocket();s.socket.bind(addr);return s;}
  InetSocketAddress getAddress(){return (InetSocketAddress)socket.getLocalSocketAddress();}
  void createContext(String path,Handler h){handler=h;}
  void start(){running=true;thread=new Thread(()->{while(running){try{Socket incoming=socket.accept();handler.accept(new Exchange(incoming));}catch(Exception ignored){}}});thread.setDaemon(true);thread.start();}
  void stop(int delay){running=false;try{socket.close();thread.join(1000);}catch(Exception ignored){}}
 }
 static class Headers extends HashMap<String,String>{String getFirst(String key){return get(key.toLowerCase(Locale.ROOT));}}
 static class Exchange {
  Socket socket;URI uri;Headers headers=new Headers();OutputStream out;
  Exchange(Socket s)throws IOException{socket=s;BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream(),StandardCharsets.US_ASCII));String line=in.readLine();uri=URI.create(line.split(" ")[1]);while((line=in.readLine())!=null&&!line.isEmpty()){int i=line.indexOf(':');headers.put(line.substring(0,i).toLowerCase(Locale.ROOT),line.substring(i+1).trim());}out=s.getOutputStream();}
  URI getRequestURI(){return uri;} Headers getRequestHeaders(){return headers;}
  void sendResponseHeaders(int status,long count)throws IOException{out.write(("HTTP/1.1 "+status+" Result\r\nContent-Type: application/json\r\nContent-Length: "+Math.max(0,count)+"\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));}
  OutputStream getResponseBody(){return out;} void close(){try{socket.close();}catch(Exception ignored){}}
 }
 HttpServer server;String url;List<String> requests;boolean blocked;
 @Before public void setup() throws Exception {requests=new ArrayList<>();server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);url="http://127.0.0.1:"+server.getAddress().getPort();server.createContext("/",e->{String q=e.getRequestURI().getRawQuery();String path=e.getRequestURI().getPath();requests.add(path+"?"+q);String data="{}";
 if(path.equals("/player_api.php")){if(q.contains("action=get_live_categories"))data="[{\"category_id\":\"7\",\"category_name\":\"News\"}]";else if(q.contains("action=get_live_streams"))data="[{\"stream_id\":42,\"name\":\"News Channel\",\"category_id\":\"7\"}]";else if(q.contains("action=get_series_info"))data="{\"episodes\":{\"2\":[{\"id\":55,\"title\":\"Episode\",\"container_extension\":\"mkv\"}]}}";else data="{\"user_info\":{\"auth\":"+(blocked?"0":"1")+",\"status\":\"Active\"}}";}
 else if(path.equals("/portal.php")){String cookie=e.getRequestHeaders().getFirst("Cookie");if(cookie==null||!cookie.contains("00%3A1A%3A79%3A11%3A22%3A33")){e.sendResponseHeaders(403,-1);e.close();return;}if(q.contains("action=handshake"))data="{\"js\":{\"token\":\"mock-token\"}}";else if(!"Bearer mock-token".equals(e.getRequestHeaders().getFirst("Authorization"))){e.sendResponseHeaders(401,-1);e.close();return;}else if(q.contains("action=get_profile"))data="{\"js\":{\"blocked\":0}}";else if(q.contains("action=get_all_channels"))data="{\"js\":{\"data\":[{\"id\":1,\"name\":\"Portal News\",\"tv_genre_id\":\"7\",\"cmd\":\"ffmpeg http://localhost/ch/1\"}]}}";else if(q.contains("action=create_link"))data="{\"js\":{\"cmd\":\"ffmpeg "+url+"/stream.m3u8\"}}";else if(q.contains("action=get_ordered_list"))data="{\"js\":{\"total_items\":2,\"max_page_items\":1,\"data\":[{\"id\":\""+(q.contains("p=2")?"2":"1")+"\",\"name\":\"Movie\",\"cmd\":\"ffmpeg http://localhost/movie\"}]}}";}
 else {e.sendResponseHeaders(404,-1);e.close();return;}
 byte[] bytes=data.getBytes(StandardCharsets.UTF_8);e.sendResponseHeaders(200,bytes.length);e.getResponseBody().write(bytes);e.close();});server.start();}
 @After public void stop(){server.stop(0);}
 Provider xc()throws Exception{return new Provider(new JSONObject().put("mode","XTREAM").put("url",url).put("user","a+b").put("pass","p &?"));}
 @Test public void xtreamLoginCategoriesAndLinks()throws Exception{Provider p=xc();p.login();assertEquals("News",p.categories("live").get("7"));List<Provider.Item> items=p.items("live","7");assertEquals(1,items.size());assertEquals(url+"/live/a%2Bb/p%20%26%3F/42.ts",p.stream(items.get(0),"live"));assertTrue(requests.get(0).contains("username=a%2Bb"));assertTrue(requests.get(2).contains("category_id=7"));}
 @Test public void rejectedCredentials()throws Exception{blocked=true;try{xc().login();fail();}catch(Exception expected){assertTrue(expected.getMessage().contains("غير صحيحة"));}}
 @Test public void seriesEpisodeExtension()throws Exception{List<Provider.Item> eps=xc().episodes(new Provider.Item("9","Show","","","",true));assertEquals(1,eps.size());assertTrue(xc().stream(eps.get(0),"series").endsWith("/55.mkv"));}
 @Test public void portalFallbackHandshakeAndCreateLink()throws Exception{Provider p=new Provider(new JSONObject().put("mode","MAC").put("url",url+"/c/").put("mac","00:1a:79:11:22:33"));p.login();List<Provider.Item> items=p.items("live","7");assertEquals(1,items.size());assertEquals(url+"/stream.m3u8",p.stream(items.get(0),"live"));assertEquals("Bearer mock-token",p.playbackHeaders(url+"/stream").get("Authorization"));assertFalse(p.playbackHeaders("https://example.com/video").containsKey("Authorization"));assertEquals(2,p.items("vod","*").size());}
 @Test public void urlValidation()throws Exception{assertEquals("http://example.com:8080",Provider.normalize("example.com:8080/"));try{Provider.normalize("https://user:pass@example.com");fail();}catch(Exception expected){}}

 @Test public void autoPlaybackOffersHlsThenTsAndManualTsOrder()throws Exception{
  Provider p=xc();Provider.Item item=p.items("live","7").get(0);List<String> auto=p.streams(item,"live","auto");assertEquals(2,auto.size());assertTrue(auto.get(0).endsWith(".m3u8"));assertTrue(auto.get(1).endsWith(".ts"));assertTrue(p.streams(item,"live","ts").get(0).endsWith(".ts"));
 }
 @Test public void directSourceAndHlsMimeArePreserved()throws Exception{
  Provider.Item item=new Provider.Item("4","Direct","","","ts",false);item.direct="https://cdn.example.test/stream?output=m3u8&token=private";List<String> links=xc().streams(item,"live","auto");assertEquals(item.direct,links.get(0));assertTrue(xc().streams(item,"live","ts").get(0).endsWith(".ts"));assertEquals("application/x-mpegURL",Provider.mime(item.direct));assertEquals("application/x-mpegURL",Provider.mime("https://host.test/hls/1.m3u8?token=x"));assertNull(Provider.mime("https://host.test/live/1.ts"));
 }
 @Test public void portalCommandParsesOptionsAndSignedQuery()throws Exception{
  assertEquals("https://host.test/play?id=4&token=abc",Provider.extractUrl("ffmpeg -i https://host.test/play?id=4&amp;token=abc -vcodec copy"));
 }
 @Test public void crossOriginRedirectStripsPrivateHeadersAndKeepsAgent()throws Exception{
  HttpServer target=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);final Headers[] got=new Headers[1];target.createContext("/",e->{got[0]=e.getRequestHeaders();byte[] bytes="ok".getBytes(StandardCharsets.UTF_8);e.sendResponseHeaders(200,bytes.length);e.getResponseBody().write(bytes);e.close();});target.start();
  try{String location="http://127.0.0.1:"+target.getAddress().getPort()+"/final";server.createContext("/",e->{e.out.write(("HTTP/1.1 302 Found\r\nLocation: "+location+"\r\nContent-Length: 0\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));e.close();});Map<String,String> h=new HashMap<>();h.put("Authorization","Bearer secret");h.put("Cookie","mac=private");h.put("User-Agent","VLC/test");HttpURLConnection c=StreamConnection.open(url+"/redirect",h,0,-1);assertEquals(200,c.getResponseCode());assertEquals("VLC/test",got[0].getFirst("User-Agent"));assertNull(got[0].getFirst("Cookie"));assertNull(got[0].getFirst("Authorization"));c.disconnect();}finally{target.stop(0);}
 }
 @Test public void httpErrorsCarryStatusForPlayerDiagnostics()throws Exception{
  server.createContext("/",e->{e.sendResponseHeaders(403,-1);e.close();});try{StreamConnection.open(url+"/forbidden",Collections.emptyMap(),0,-1);fail();}catch(StreamConnection.Status e){assertEquals(403,e.code);}
 }
}
