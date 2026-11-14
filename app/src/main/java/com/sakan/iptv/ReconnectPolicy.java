package com.sakan.iptv;
final class ReconnectPolicy {
 static final int MAX_ATTEMPTS=3;
 static long delay(int attempt){return attempt<0||attempt>=MAX_ATTEMPTS?-1:Math.min(12000,2000L<<attempt);}
 static boolean transientHttp(int status){return status==0||status==408||status==429||status==500||status==502||status==503||status==504;}
}
