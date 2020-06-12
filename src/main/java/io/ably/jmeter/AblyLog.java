package io.ably.jmeter;

import io.ably.lib.util.Log;
import io.ably.lib.util.Log.LogHandler;
import org.slf4j.Logger;

public class AblyLog {
	public static final String[] levelNames = new String[]{
		"NONE",
		"VERBOSE",
		"DEBUG",
		"INFO",
		"WARN",
		"ERROR"
	};

	private static final int[] ablyLevels = new int[]{
        Log.NONE,
        Log.VERBOSE,
        Log.DEBUG,
        Log.INFO,
        Log.WARN,
        Log.ERROR
	};

	public static int asAblyLevel(int index) {
        return ablyLevels[index];
    }

   public static LogHandler getAblyHandler(final Logger sl4jLogger) {
		return (ablyLevel, tag, msg, tr) -> {
			if(tag != null && !tag.isEmpty()) {
				msg = tag + ": " + msg;
			}
			switch (ablyLevel) {
				case Log.VERBOSE:
					sl4jLogger.trace(msg, tr);
					break;
				case Log.DEBUG:
					sl4jLogger.debug(msg, tr);
					break;
				case Log.INFO:
					sl4jLogger.info(msg, tr);
					break;
				case Log.WARN:
					sl4jLogger.warn(msg, tr);
					break;
				case Log.ERROR:
					sl4jLogger.error(msg, tr);
					break;
				default:
					sl4jLogger.warn(msg, tr);
					break;
			}
		};
	}
}
