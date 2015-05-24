package com.smilehacker.meemo.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kleist on 14-4-2.
 */
public class BoyerMoore {

    /**
     * use Boyer-Moore to match string
     * @param pattern
     * @param text
     * @return
     */
    public static int match(String pattern, String text) {
        int m = text.length();
        int n = pattern.length();
        Map<Character, Integer> rightMostIndexes = preprocessForBadCharacterShift(pattern);
        int alignedAt = 0;
        while (alignedAt + (n - 1) < m) {
            for (int indexInPattern = n - 1; indexInPattern >= 0; indexInPattern--) {
                int indexInText = alignedAt + indexInPattern;
                char x = text.charAt(indexInText);
                char y = pattern.charAt(indexInPattern);
                if (indexInText >= m) break;
                if (x != y) {
                    Integer r = rightMostIndexes.get(x);
                    if (r == null) {
                        alignedAt = indexInText + 1;
                    }
                    else {
                        int shift = indexInText - (alignedAt + r);
                        alignedAt += shift > 0 ? shift : 1;
                    }
                    break;
                }
                else if (indexInPattern == 0) {
                    return alignedAt;
                }
            }
        }

        return -1;
    }

    private static Map<Character, Integer> preprocessForBadCharacterShift(
            String pattern) {
        Map<Character, Integer> map = new HashMap<Character, Integer>();
        for (int i = pattern.length() - 1; i >= 0; i--) {
            char c = pattern.charAt(i);
            if (!map.containsKey(c)) map.put(c, i);
        }
        return map;
    }

}
