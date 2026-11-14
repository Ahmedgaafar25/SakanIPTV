package com.sakan.iptv;
import org.junit.Test;import static org.junit.Assert.*;
public class PreviewViewportTest {
 @Test public void mapsBrowserPreviewToPhysicalTvPixels(){assertArrayEquals(new int[]{1000,200,800,450},PreviewViewport.map(500,100,400,225,960,1920,1080));}
 @Test public void rejectsOffscreenOrMalformedSlots(){assertNull(PreviewViewport.map(0,-1,100,50,360,1080,1920));assertNull(PreviewViewport.map(300,0,100,50,360,1080,1920));assertNull(PreviewViewport.map(0,700,100,100,360,360,750));assertNull(PreviewViewport.map(Float.NaN,0,10,10,360,360,800));assertNull(PreviewViewport.map(0,0,100,50,0,360,800));}
 @Test public void mapsPhoneSlotWithoutChangingAspect(){assertArrayEquals(new int[]{60,100,300,169},PreviewViewport.map(60,100,300,168.75f,390,390,844));}
}
