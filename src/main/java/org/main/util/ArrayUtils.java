package org.main.util;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {
    public static <T> boolean arrayContains(T[] array, T t) {
        for (T item : array) {
            if (item == t) {
                return true;
            }
        }
        return false;
    }

    public static boolean arrayContains(int[] array, int t) {
        for (int item : array) {
            if (item == t) {
                return true;
            }
        }
        return false;
    }

    public static <T> String join(T[] array) {
        String s = "";
        for (int i = 0; i < array.length; ++i) {
            s += array[i];
            if (i == array.length - 2) {
                s += " or ";
            } else if (i != array.length - 1) {
                s += ", ";
            }
        }
        return s;
    }

    public static <T extends List> T wrapInListIfNecessary(Object o) {
        if(o instanceof List) {
            return (T) o;
        } else {
            var t = new ArrayList<Object>();
            t.add(o);
            return (T) t;
        }
    }
}
