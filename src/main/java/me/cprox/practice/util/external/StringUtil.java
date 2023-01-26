package me.cprox.practice.util.external;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class StringUtil {
	public static final String NO_PERMISSION;
	public static final String PLAYER_ONLY;
	public static final String PLAYER_NOT_FOUND;
	public static final String LOAD_ERROR;
	public static final char NICE_CHAR = '●';
	public static final String SPLIT_PATTERN;
	private static final String MAX_LENGTH = "11111111111111111111111111111111111111111111111111111";
	private static final List<String> VOWELS;
	private static final ThreadLocal<DecimalFormat> seconds = ThreadLocal.withInitial(() -> new DecimalFormat("0.#"));
	private static final ThreadLocal<DecimalFormat> trailing = ThreadLocal.withInitial(() -> new DecimalFormat("0"));
	static {
		NO_PERMISSION = ChatColor.RED + "You don't enough permissions.";
		PLAYER_ONLY = ChatColor.RED + "Only players can use this command.";
		PLAYER_NOT_FOUND = ChatColor.RED + "%s not found.";
		LOAD_ERROR = ChatColor.RED + "An error occured, please contact an administrator.";
		SPLIT_PATTERN = Pattern.compile("\\s").pattern();
		VOWELS = Arrays.asList("a", "e", "u", "i", "o");
	}

	public static String buildString(final String[] args, final int start) {
		return String.join(" ", (CharSequence[])Arrays.copyOfRange(args, start, args.length));
	}

	private StringUtil() {
		throw new RuntimeException("Cannot instantiate a utility class.");
	}

	public static String toNiceString(String string) {
		string = ChatColor.stripColor(string).replace('_', ' ').toLowerCase();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < string.toCharArray().length; ++i) {
			char c = string.toCharArray()[i];
			if (i > 0) {
				final char prev = string.toCharArray()[i - 1];
				if ((prev == ' ' || prev == '[' || prev == '(') && (i == string.toCharArray().length - 1 || c != 'x' || !Character.isDigit(string.toCharArray()[i + 1]))) {
					c = Character.toUpperCase(c);
				}
			} else if (c != 'x' || !Character.isDigit(string.toCharArray()[i + 1])) {
				c = Character.toUpperCase(c);
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public static String buildMessage(final String[] args, final int start) {
		if (start >= args.length) {
			return "";
		}
		return ChatColor.stripColor(String.join(" ", (CharSequence[]) Arrays.copyOfRange(args, start, args.length)));
	}

	public static String getFirstSplit(final String s) {
		return s.split(StringUtil.SPLIT_PATTERN)[0];
	}

	public static String getAOrAn(final String input) {
		return StringUtil.VOWELS.contains(input.substring(0, 1).toLowerCase()) ? "an" : "a";
	}

	public static String niceTime(int i) {
		int r = i * 1000;
		int sec = r / 1000 % 60;
		int min = r / 60000 % 60;
		int h = r / 3600000 % 24;
		return (h > 0 ? (h < 10 ? "0" : "") + h + ":" : "") + (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
	}

	public static String niceTime(long millis, boolean milliseconds) {
		return niceTime(millis, milliseconds, true);
	}

	public static String niceTime(long duration, boolean milliseconds, boolean trail) {
		if (milliseconds && duration < TimeUnit.MINUTES.toMillis(1)) {
			return (trail ? trailing : seconds).get().format((double) duration * 001);
		}

		return DurationFormatUtils.formatDuration(duration, (duration >= TimeUnit.HOURS.toMillis(1) ? "HH:" : "") + "mm:ss");
	}

	private static long handleConvert(int value, char charType) {
		switch(charType) {
			case 'y':
				return value * TimeUnit.DAYS.toMillis(365L);
			case 'M':
				return value * TimeUnit.DAYS.toMillis(30L);
			case 'w':
				return value * TimeUnit.DAYS.toMillis(7L);
			case 'd':
				return value * TimeUnit.DAYS.toMillis(1L);
			case 'h':
				return value * TimeUnit.HOURS.toMillis(1L);
			case 'm':
				return value * TimeUnit.MINUTES.toMillis(1L);
			case 's':
				return value * TimeUnit.SECONDS.toMillis(1L);
			default:
				return -1L;
		}
	}

}
