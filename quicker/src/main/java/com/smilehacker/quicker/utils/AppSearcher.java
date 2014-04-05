package com.smilehacker.quicker.utils;

import com.smilehacker.quicker.data.model.AppInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by kleist on 14-4-2.
 */
public class AppSearcher {

    public static List<AppInfo> search(List<AppInfo> appInfos, String inputNum) {
        for (AppInfo appInfo: appInfos) {
            double priority = Double.MIN_VALUE;
            for (String num: appInfo.fullT9) {
                double p = computePriority(inputNum, num, true);
                if (p > priority) {
                    priority = p;
                }
            }
            for (String num: appInfo.shortT9) {
                double p = computePriority(inputNum, num, false);
                if (p > priority) {
                    priority = p;
                }
            }

            appInfo.priority = priority;
        }

        Collections.sort(appInfos, new SortByPriority());
        return appInfos;
    }

    private static double computePriority(String inputNum, String appNum, Boolean isFullNum) {
        double priority = 0;

        int matchPos = BoyerMoore.match(inputNum, appNum);
        if (matchPos == -1) {
            return 0;
        }

        int appNumLength = appNum.length();
        int inputNumLength = inputNum.length();

        //priority = inputNumLength / appNumLength * matchPos + (isFullNum ? 0 : 1) * 3;
        priority = (appNumLength - matchPos) / (appNumLength - inputNumLength + 1) * (isFullNum ? 1 : 1.2);


        return priority;
    }

    private static class SortByPriority implements Comparator<AppInfo> {

        @Override
        public int compare(AppInfo appInfo, AppInfo appInfo2) {
            if (appInfo.priority > appInfo2.priority) {
                return -1;
            } else if (appInfo.priority < appInfo2.priority) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
