package com.sakan.iptv;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;

public class ChannelNavigatorTest {
 @Test public void advancesAndWrapsInBothDirections(){List<String> ids=Arrays.asList("a","b","c");assertEquals(1,ChannelNavigator.adjacent(ids,"a",1));assertEquals(0,ChannelNavigator.adjacent(ids,"c",1));assertEquals(2,ChannelNavigator.adjacent(ids,"a",-1));assertEquals(0,ChannelNavigator.adjacent(ids,"b",-1));}
 @Test public void startsAtCorrectEndWhenCurrentChannelIsNotInVisibleList(){List<String> ids=Arrays.asList("b","c");assertEquals(0,ChannelNavigator.adjacent(ids,"hidden",1));assertEquals(1,ChannelNavigator.adjacent(ids,"hidden",-1));}
 @Test public void handlesSingleChannelAndEmptyLibraries(){assertEquals(0,ChannelNavigator.adjacent(Collections.singletonList("a"),"a",1));assertEquals(-1,ChannelNavigator.adjacent(Collections.emptyList(),"a",-1));}
}
