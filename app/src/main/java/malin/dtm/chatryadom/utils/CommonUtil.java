package malin.dtm.chatryadom.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by dmt on 14.09.2015.
 */
public class CommonUtil {
    public static String joinArray(ArrayList arr) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : arr) {
            sb.append(obj);
        }
        return sb.toString();
    }

    public static Date getOnlyDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.AM_PM, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static String firstUpperCase(String string) {
        char first = Character.toUpperCase(string.charAt(0));
        return first + string.substring(1);
    }
}
