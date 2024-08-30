/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.utils;

public class StringUtils {
    public static String trimChars(String src) {
        return src.replaceAll("(.)\\1+", "$1");
    }

    public static String concatStrings(String ... str) {
        StringBuffer sbf = new StringBuffer();
        String[] arrstring = str;
        int n = str.length;
        for (int i = 0; i < n; ++i) {
            String adder = arrstring[i];
            sbf.append(adder);
        }
        return sbf.toString();
    }

    public static String concatMassive(String[] src, int start) {
        StringBuffer sbf = new StringBuffer();
        for (int i = start; i < src.length; ++i) {
            sbf.append(src[i]);
            if (i == src.length - 1) continue;
            sbf.append(' ');
        }
        return sbf.toString();
    }
}

