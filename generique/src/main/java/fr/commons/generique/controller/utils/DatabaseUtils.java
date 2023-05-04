package fr.commons.generique.controller.utils;

import android.content.Context;
import android.database.Cursor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class DatabaseUtils {

    /******** STATIC ********/

    public static String arrayToDelemiteString(final Object[] params,
                                               final boolean encapsulate) {
        if (params != null && params.length > 0) {
            final StringBuilder b = new StringBuilder();
            for (Object t : params) {
                if (t != null) {
                    final String objectToString;
                    if (encapsulate) {
                        objectToString = String.valueOf(t);
                    } else {
                        objectToString = toStringWithQuotes(t);
                    }
                    b.append(objectToString);
                    b.append(",");
                }
            }
            int idxLastComa = b.lastIndexOf(",");
            if (idxLastComa >= 0) {
                b.replace(idxLastComa, idxLastComa + 1, "");
            }
            return b.toString();
        }

        return "";
    }

    /**
     * Add quotes to string for sql request
     *
     * @param o
     * @return
     */
    public static String toStringWithQuotes(final Object o) {
        return "'" + o + "'";
    }

    public static String getStringCheckNullColumn(final Cursor c,
                                                  final String columnName) {
        int columnIndex = c.getColumnIndex(columnName);
        if (columnIndex == -1 || c.isNull(columnIndex)) {
            return null;
        }
        return c.getString(columnIndex);
    }

    public static int getIntCheckNullColumn(final Cursor c,
                                            final String columnName) {
        int columnIndex = c.getColumnIndex(columnName);
        if (columnIndex == -1 || c.isNull(columnIndex)) {
            return -1;
        }
        return c.getInt(columnIndex);
    }

    public static long getLongCheckNullColumn(final Cursor c,
                                              final String columnName) {
        int columnIndex = c.getColumnIndex(columnName);
        if (columnIndex == -1 || c.isNull(columnIndex)) {
            return -1L;
        }
        return c.getLong(columnIndex);
    }

    public static double getDoubleCheckNullColumn(final Cursor c,
                                                  final String columnName) {
        int columnIndex = c.getColumnIndex(columnName);
        if (columnIndex == -1 || c.isNull(columnIndex)) {
            return -1D;
        }
        return c.getDouble(columnIndex);
    }

    public static float getFloatCheckNullColumn(final Cursor c,
                                                final String columnName) {
        int columnIndex = c.getColumnIndex(columnName);
        if (columnIndex == -1 || c.isNull(columnIndex)) {
            return -1F;
        }
        return c.getFloat(columnIndex);
    }

    public static float getShortCheckNullColumn(final Cursor c,
                                                final String columnName) {
        int columnIndex = c.getColumnIndex(columnName);
        if (columnIndex == -1 || c.isNull(columnIndex)) {
            return -1;
        }
        return c.getShort(columnIndex);
    }

    public static byte[] getBlobCheckNullColumn(final Cursor c,
                                                final String columnName) {
        int columnIndex = c.getColumnIndex(columnName);
        if (columnIndex == -1 || c.isNull(columnIndex)) {
            return null;
        }
        return c.getBlob(columnIndex);
    }

    public static boolean getBoolCheckNullColumn(final Cursor c,
                                                 final String columnName) {
        int columnIndex = c.getColumnIndex(columnName);
        if (columnIndex == -1 || c.isNull(columnIndex)) {
            return false;
        }
        return c.getInt(columnIndex) == 1;
    }

    public static String getLang() {
        List<String> listSupportLang = Arrays.asList("fr_FR","en_US");

        String locale = Locale.getDefault().toString();
        // find if current locale is supported
        boolean isLocalSupported = listSupportLang.contains(locale);

        String result = null;
        if (!isLocalSupported) {
            // if not supported find code for others country
            // e.g : en_GB not found => display en_US if exist
            for (String l : listSupportLang) {
                if (l.startsWith(Locale.getDefault().getLanguage())) {
                    result = l;
                    break;
                }
            }
        } else {
            result = locale;
        }

        if (result == null || result.isEmpty()) {
            // FIXME result = c.getString(R.string.default_lang);
            result = "fr-fr";
        }

        return result;
    }


    public static String buildWhereIn(final String col, List<String> values, boolean isStringType) {
        return " " + col + " IN (" + arrayToDelemiteString(values.toArray(), isStringType) + ") ";
    }

    public static String buildWhere(final String col, final String value) {
        return " " + col + "=" + value + " ";
    }

    public static String buildWhereLike(final String col, final String value) {
        return " " + col + " LIKE %" + value + "% ";
    }

}