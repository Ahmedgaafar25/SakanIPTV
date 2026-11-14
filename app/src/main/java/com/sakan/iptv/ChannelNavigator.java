package com.sakan.iptv;
import java.util.List;

final class ChannelNavigator {
 static int adjacent(List<String> ids,String current,int direction){if(ids.isEmpty())return -1;int at=ids.indexOf(current);if(at<0)return direction<0?ids.size()-1:0;return (at+(direction<0?-1:1)+ids.size())%ids.size();}
}
