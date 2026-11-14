package com.sakan.iptv;
import org.junit.Test;import static org.junit.Assert.*;

public class PlaybackNavigationTest {
 @Test public void firstBackHidesControlsThenSecondReturnsToLibrary(){assertEquals(PlaybackNavigation.Back.HIDE_CONTROLS,PlaybackNavigation.back(false,true));assertEquals(PlaybackNavigation.Back.EXIT_PLAYER,PlaybackNavigation.back(false,false));}
 @Test public void channelPanelClosesBeforeExiting(){assertEquals(PlaybackNavigation.Back.CLOSE_OVERLAY,PlaybackNavigation.back(true,false));assertEquals(PlaybackNavigation.Back.CLOSE_OVERLAY,PlaybackNavigation.back(true,true));assertEquals(PlaybackNavigation.Back.EXIT_PLAYER,PlaybackNavigation.back(false,false));}
 @Test public void alreadyHiddenControlsAllowImmediateReturn(){assertEquals(PlaybackNavigation.Back.EXIT_PLAYER,PlaybackNavigation.back(false,false));}
}
