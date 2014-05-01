package com.smilehacker.meemo.utils;

import android.text.TextUtils;

import com.smilehacker.meemo.data.model.AppInfo;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kleist on 14-4-2.
 */
public class AppT9Parser {

    private HanyuPinyinOutputFormat mPinYinFormat;

    public AppT9Parser() {
        mPinYinFormat = new HanyuPinyinOutputFormat();
        mPinYinFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        mPinYinFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        mPinYinFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
    }


    public AppInfo parseAppNameToT9(AppInfo app) {




        List<String> words = spliteWords(app.appName);
        List<String[]> pinyins = parseStringToPinyin(words);
        app.shortT9 = parseShortToT9(pinyins);
        app.fullT9 = parseWholeToT9(pinyins);
        return app;
    }

    private List<String> spliteWords(String text) {
        int startPoint = 0;
        int endPoint = 0;
        List<String> words = new LinkedList<String>();

        text = text.toLowerCase();
        char[] letters = text.toCharArray();
        if (letters.length <= 0) {
            return  Collections.emptyList();
        }

        for (int i = 0; i < letters.length; i++) {
            if (letters[i] >= 'a' && letters[i] <= 'z' || letters[i] >= 'A' && letters[i] <= 'Z' || letters[i] >= '0' && letters[i] <= '9') {
                // is English letter
                if (i == letters.length - 1) {
                    String word = new String(letters, startPoint, i - startPoint + 1);
                    words.add(word);
                }
                endPoint = i;
            } else if (letters[i] > 128) {
                // is Chinese letter
                if (endPoint == startPoint) {
                    words.add(String.valueOf(letters[i]));
                } else {
                    String word = new String(letters, startPoint, endPoint - startPoint + 1);
                    words.add(word);
                }

                startPoint = endPoint = i + 1;
            } else {
                if (i != startPoint) {
                    String word = new String(letters, startPoint, endPoint - startPoint + 1);
                    words.add(word);
                }

                startPoint = endPoint = i + 1;
            }
        }

        return words;
    }

    private List<String[]> parseStringToPinyin(List<String> words) {
        List<String[]> pinyinList = new ArrayList<String[]>();
        for (String word : words) {
            if (TextUtils.isEmpty(word)) {
                continue;
            }
            char[] letters = word.toCharArray();
            if (letters.length == 1 && letters[0] > 128) {
                try {
                    String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(letters[0], mPinYinFormat);

                    if (pinyin != null) {
                        pinyinList.add(reduceDuplication(pinyin));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    DLog.e(e.getMessage());
                }
            } else {
                pinyinList.add(new String[] {word});
            }
        }
        return pinyinList;
    }

    private String[] parseWholeToT9(List<String[]> words) {
        List<String[]> numList = new ArrayList<String[]>();
        for (String[] word : words) {
            String[] tmp = new String[word.length];
            for (int i = 0; i < word.length; i++) {
                tmp[i] = parseToNum(word[i]);
            }
            tmp = reduceDuplication(tmp);
            numList.add(tmp);
        }

        return joinNums(numList);
    }

    private String[] parseShortToT9(List<String[]> words) {
        List<String[]> numList = new ArrayList<String[]>();
        for (String[] word: words) {
            String[] tmp = new String[word.length];
            for (int i = 0; i < word.length; i++) {
                tmp[i] = Character.toString(parseToNum(word[i].charAt(0)));
            }
            tmp = reduceDuplication(tmp);
            numList.add(tmp);
        }

        return joinNums(numList);
    }

    private String[] joinNums(List<String[]> words) {
        List<String> list = new LinkedList<String>();
        joinNextNum(list, words, new StringBuilder(), 0);
        return list.toArray(new String[list.size()]);
    }

    private void joinNextNum(List<String> nums, List<String[]> words, StringBuilder numBuilder, int pos) {

        for (int i = 0, max = words.get(pos).length; i < max; i++) {
            StringBuilder builder = new StringBuilder(numBuilder);
            builder.append(words.get(pos)[i]);
            if (pos < words.size() - 1) {
                joinNextNum(nums, words, builder, pos + 1);
            } else {
                nums.add(builder.toString());
            }
        }
    }



    private String parseToNum(String word) {
        char[] letters = word.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char letter : letters) {
            builder.append(parseToNum(letter));
        }
        return builder.toString();
    }

    private char parseToNum(char letter) {
        switch (letter) {
            case '1':
                return '1';
            case '2':
            case 'a':
            case 'b':
            case 'c':
                return '2';
            case '3':
            case 'd':
            case 'e':
            case 'f':
                return '3';
            case '4':
            case 'g':
            case 'h':
            case 'i':
                return '4';
            case '5':
            case 'j':
            case 'k':
            case 'l':
                return '5';
            case '6':
            case 'm':
            case 'n':
            case 'o':
                return '6';
            case '7':
            case 'p':
            case 'q':
            case 'r':
            case 's':
                return '7';
            case '8':
            case 't':
            case 'u':
            case 'v':
                return '8';
            case '9':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                return '9';
            case '0':
                return '1';
            default:
                return '1';
        }
    }

    private List reduceDuplication(List list) {
        List tmpList = new LinkedList();
        for (Object element : list) {
            if (!tmpList.contains(element)) {
                tmpList.add(element);
            }
        }
        list.clear();
        list.addAll(list);
        return list;
    }

    private String[] reduceDuplication(String[] list) {
        List<String> tmpList = new LinkedList<String>();
        for (String element : list) {
            if (!tmpList.contains(element)) {
                tmpList.add(element);
            }
        }
        return tmpList.toArray(new String[tmpList.size()]);
    }

}
