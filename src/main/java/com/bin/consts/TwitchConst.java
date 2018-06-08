package com.bin.consts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TwitchConst {
    public static final String BADGES = "badges";
    public static final String BADGE_SUB = "subscriber";
    public static final String BADGE_MODERATOR = "moderator";
    public static final String BADGE_BITS = "bits";
    public static final String BADGE_PREMIUM = "premium";
    public static final String BADGE_SUB_GIFTER = "sub-gifter";

    public static final Map<String, Character> mapOfBadge;

    static {
        Map<String, Character> map = new HashMap<>();
        map.put(BADGE_SUB, 'S');
        map.put(BADGE_MODERATOR, 'M');
        map.put(BADGE_BITS, 'B');
        map.put(BADGE_PREMIUM, 'P');
        map.put(BADGE_SUB_GIFTER, 'G');

        mapOfBadge = Collections.unmodifiableMap(map);
    }
}
