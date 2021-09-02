package com.masonsoft.imsdk.uikit.util;

import android.text.TextUtils;

import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FormatUtil {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    /**
     * 格式化时间展示样式.
     */
    public static String getHumanTimeDistance(final long timeMs, DateFormatOptions dateFormatOptions) {
        TimeDistance timeDistance = new TimeDistance(timeMs);

        // 不同年
        if (!timeDistance.isSameYear()) {
            return dateFormatOptions.full().format(new Date(timeMs));
        }

        // 同年不同周
        if (!timeDistance.isSameWeekOfYear()) {
            return dateFormatOptions.sameYear().format(new Date(timeMs));
        }

        // 同年同月同周
        final int diffDay = timeDistance.getCurrentDayOfWeek() - timeDistance.getTargetDayOfWeek();
        if (diffDay > 1) {
            // 差距不小于一天
            return dateFormatOptions.sameWeek().format(new Date(timeMs));
        } else if (diffDay == 1) {
            // 昨天
            return dateFormatOptions.lastDay().format(new Date(timeMs));
        } else {
            // 同一天
            if (DEBUG) {
                if (diffDay != 0) {
                    Throwable e = new IllegalArgumentException("invalid diff day " + diffDay);
                    MSIMUikitLog.e(e);
                }
            }
            return dateFormatOptions.sameDay().format(new Date(timeMs));
        }
    }

    public interface DateFormatOptions {
        SimpleDateFormat sameDay();

        SimpleDateFormat lastDay();

        SimpleDateFormat sameWeek();

        SimpleDateFormat sameYear();

        SimpleDateFormat full();
    }

    /**
     * HH:mm
     * M-d HH:mm
     * M-d HH:mm
     * M-d HH:mm
     * yyyy-M-d HH:mm
     */
    public static class DefaultDateFormatOptions implements DateFormatOptions {

        @Override
        public SimpleDateFormat sameDay() {
            return new SimpleDateFormat("HH:mm", Locale.CHINA);
        }

        @Override
        public SimpleDateFormat lastDay() {
            return new SimpleDateFormat("M-d HH:mm", Locale.CHINA);
        }

        @Override
        public SimpleDateFormat sameWeek() {
            return new SimpleDateFormat("M-d HH:mm", Locale.CHINA);
        }

        @Override
        public SimpleDateFormat sameYear() {
            return new SimpleDateFormat("M-d HH:mm", Locale.CHINA);
        }

        @Override
        public SimpleDateFormat full() {
            return new SimpleDateFormat("yyyy-M-d HH:mm", Locale.CHINA);
        }
    }

    /**
     * HH:mm
     * M-d
     * M-d
     * M-d
     * yyyy-M-d
     */
    public static class DefaultShortDateFormatOptions implements DateFormatOptions {

        @Override
        public SimpleDateFormat sameDay() {
            return new SimpleDateFormat("HH:mm", Locale.CHINA);
        }

        @Override
        public SimpleDateFormat lastDay() {
            return new SimpleDateFormat("M-d", Locale.CHINA);
        }

        @Override
        public SimpleDateFormat sameWeek() {
            return new SimpleDateFormat("M-d", Locale.CHINA);
        }

        @Override
        public SimpleDateFormat sameYear() {
            return new SimpleDateFormat("M-d", Locale.CHINA);
        }

        @Override
        public SimpleDateFormat full() {
            return new SimpleDateFormat("yyyy-M-d", Locale.CHINA);
        }
    }

    /**
     * 时长格式化显示，例如用于显示语音的时长为 60"
     */
    public static String getHumanTimeDuration(final long durationMs) {
        long s = durationMs / 1000;
        if (s < 0) {
            s = 0;
        }

        return s + "\"";
    }

    private static final DecimalFormat TIME_M_FORMAT = new DecimalFormat("00");
    private static final DecimalFormat TIME_S_FORMAT = new DecimalFormat("00");

    /**
     * 时长格式化显示，例如用于显示视频的时长为 00:40
     */
    public static String getHumanTimeDuration2(final long durationMs) {
        long s = durationMs / 1000;
        if (s < 0) {
            s = 0;
        }

        long formatMin = s / 60;
        long formatS = s % 60;
        return TIME_M_FORMAT.format(formatMin) + ":" + TIME_S_FORMAT.format(formatS);
    }

    private static final SimpleDateFormat TIME_FORMAT_YYYYMMDD = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private static final SimpleDateFormat TIME_FORMAT_YYYYMMDDHHMM = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    /**
     * 将 yyyy-MM-dd 格式的时间转换为毫秒，转换失败返回 0
     */
    public static long parseTimeMsFromYYYYMMDD(String timeYYYYMMDD) {
        try {
            if (TextUtils.isEmpty(timeYYYYMMDD)) {
                return 0;
            }
            return TIME_FORMAT_YYYYMMDD.parse(timeYYYYMMDD).getTime();
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
        }
        return 0;
    }

    /**
     * 判断时间区域，如用于个人主页显示 UGC 时间线.
     */
    public static TimeDistance parseTimeDistance(final long timeMs) {
        return new TimeDistance(timeMs);
    }

    public static class TimeDistance {
        private final long mCurrentTimeMs;
        private final int mCurrentYear;
        private final int mCurrentDayOfWeek;
        private final int mCurrentDayOfMonth;
        private final int mCurrentMonth;
        private final int mCurrentWeekOfYear;

        private final long mTargetTimeMs;
        private final int mTargetYear;
        private final int mTargetDayOfWeek;
        private final int mTargetDayOfMonth;
        private final int mTargetMonth;
        private final int mTargetWeekOfYear;

        private TimeDistance(final long timeMs) {
            Calendar calendar = Calendar.getInstance();

            this.mCurrentTimeMs = System.currentTimeMillis();
            calendar.setTimeInMillis(this.mCurrentTimeMs);
            this.mCurrentYear = calendar.get(Calendar.YEAR);
            this.mCurrentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            this.mCurrentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            this.mCurrentMonth = calendar.get(Calendar.MONTH);
            this.mCurrentWeekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

            this.mTargetTimeMs = timeMs;
            calendar.setTimeInMillis(this.mTargetTimeMs);
            this.mTargetYear = calendar.get(Calendar.YEAR);
            this.mTargetDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            this.mTargetDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            this.mTargetMonth = calendar.get(Calendar.MONTH);
            this.mTargetWeekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        }

        public boolean isSameYear() {
            return mCurrentYear == mTargetYear;
        }

        public boolean isSameWeekOfYear() {
            return mCurrentWeekOfYear == mTargetWeekOfYear;
        }

        public boolean isSameDayOfWeek() {
            return mCurrentDayOfWeek == mTargetDayOfWeek;
        }

        public int getCurrentYear() {
            return mCurrentYear;
        }

        public int getCurrentDayOfWeek() {
            return mCurrentDayOfWeek;
        }

        public int getCurrentWeekOfYear() {
            return mCurrentWeekOfYear;
        }

        public int getTargetYear() {
            return mTargetYear;
        }

        public int getTargetDayOfWeek() {
            return mTargetDayOfWeek;
        }

        public int getTargetWeekOfYear() {
            return mTargetWeekOfYear;
        }

        public int getCurrentDayOfMonth() {
            return mCurrentDayOfMonth;
        }

        public int getCurrentMonth() {
            return mCurrentMonth;
        }

        public int getTargetDayOfMonth() {
            return mTargetDayOfMonth;
        }

        public int getTargetMonth() {
            return mTargetMonth;
        }

        public long getCurrentTimeMs() {
            return mCurrentTimeMs;
        }

        public long getTargetTimeMs() {
            return mTargetTimeMs;
        }
    }

    /**
     * 格式化毫秒时间为 yyyy-MM-dd 格式
     */
    public static String formatMsAsYYYYMMDD(long timeMs) {
        return TIME_FORMAT_YYYYMMDD.format(new Date(timeMs));
    }

    /**
     * 格式化毫秒时间为 yyyy-MM-dd HH:mm 格式
     */
    public static String formatMsAsYYYYMMDDHHMM(long timeMs) {
        return TIME_FORMAT_YYYYMMDDHHMM.format(new Date(timeMs));
    }

}
