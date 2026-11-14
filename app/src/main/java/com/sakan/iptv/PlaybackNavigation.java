package com.sakan.iptv;

final class PlaybackNavigation {
 static final int CONTROLS_TIMEOUT_MS=3000;
 enum Back {CLOSE_OVERLAY,HIDE_CONTROLS,EXIT_PLAYER}
 static Back back(boolean overlay,boolean controls){return overlay?Back.CLOSE_OVERLAY:controls?Back.HIDE_CONTROLS:Back.EXIT_PLAYER;}
}
