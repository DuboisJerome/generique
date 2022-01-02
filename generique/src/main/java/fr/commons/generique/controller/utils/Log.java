package fr.commons.generique.controller.utils;

public final class Log {

	public static void error(String msg, Throwable t) {
		android.util.Log.e(TagUtils.ERR, msg, t);
	}

	public static void warn(String msg) {
		android.util.Log.w(TagUtils.WARN, msg);
	}

	public static void info(String msg) {
		android.util.Log.i(TagUtils.INFO, msg);
	}

	public static void debug(String msg) {
		android.util.Log.d(TagUtils.DEBUG, msg);
	}
}