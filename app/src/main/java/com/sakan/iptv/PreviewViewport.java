package com.sakan.iptv;

final class PreviewViewport {
 static int[] map(float x,float y,float width,float height,float viewport,int actualWidth,int actualHeight){for(float value:new float[]{x,y,width,height,viewport})if(Float.isNaN(value)||Float.isInfinite(value))return null;if(viewport<=0||width<=0||height<=0||x<0||y<0||x+width>viewport+1||actualWidth<=0||actualHeight<=0)return null;float scale=actualWidth/viewport;if((y+height)*scale>actualHeight+2)return null;int left=Math.round(x*scale),top=Math.round(y*scale),w=Math.min(actualWidth-left,Math.round(width*scale)),h=Math.min(actualHeight-top,Math.round(height*scale));return w>0&&h>0?new int[]{left,top,w,h}:null;}
}
