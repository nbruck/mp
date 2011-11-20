package com.motorpast.additional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

public class MotorUtils
{
    final public static String convertThrowable(Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();

        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /**
     * useful for debug-output
     */
    final public static<T> String arrayToString(final T[] array, final String name) {
        final StringBuilder logBuilder = new StringBuilder(name + "=[");
        for(int i = 0; i < array.length; i++) {
            logBuilder.append(i)
                .append("=")
                .append(array[i])
                .append(", ");
        }

        logBuilder.insert(logBuilder.length() - 2, "");
        logBuilder.append("]");

        return logBuilder.toString();
    }

    /**
     * enum names to message key-strings
     */
    final public static String errorCodeToMessageString(final MotorpastException e) {
        final String errorMessage = e.getErrorCode().replace("_", ".").toLowerCase();

        return "error." + errorMessage;
    }

    /**
     * extracts the ip from request
     */
    final public static String getIP(final HttpServletRequest request) {
        String ip = null;
        final String ipFromRequest = request.getRemoteAddr();

        if(ipFromRequest != null) {
            if(ipFromRequest.length() > 15) {
                ip = ipFromRequest.substring(0, 15);
            } else {
                ip = ipFromRequest;
            }
        }

        return ip;
    }

    /**
     * gets the hostinfo from request (browser, os...)
     */
    final public static String getUserAgent(final HttpServletRequest request) {
        String hostInfo = null;
        final String userAgentHeader = request.getHeader("user-agent");

        if(userAgentHeader != null) {
            if(userAgentHeader.length() > 120) {
                hostInfo = userAgentHeader.substring(0, 120);
            } else {
                hostInfo = userAgentHeader;
            }
        }

        return hostInfo;
    }
/*
    final public static String[] createRangeArray(final int from, final int to) {
        final String[] rangeArray = new String[to - from  + 1];

        for(int i = from, j = 0; i <= to; i++, j++) {
            rangeArray[j] = String.valueOf(i);
        }

        return rangeArray;
    }

    final public static String[] createYearRangeArray(final int yearRange) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); //now

        final int currentyear = cal.get(Calendar.YEAR);

        final String[] years = new String[yearRange + 1];

        for(int i = 0; i <= yearRange; i++) {
            years[i] = String.valueOf(currentyear - i);
        }

        return years;
    }
*/
    final public static Date createDateFromInput(final int year, final int month, final int day) {
        final Calendar calendar = new GregorianCalendar(year, month - 1, day);
        return calendar.getTime();
    }

    /**
     * generates a random Number between 0 and n
     */
    final public static int getRandomInt(final int n) {
        final Random random = new Random();

        return random.nextInt(n);
    }

    /**
     * simply converts an array into a arraylist
     */
    public final static<T> List<T> arrayToList(T[] array) {
        List<T> list = new ArrayList<T>(array.length);

        for(T current : array) {
            list.add(current);
        }

        return list;
    }
}
