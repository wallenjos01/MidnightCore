package me.m1dnightninja.midnightcore.common;

import java.util.concurrent.TimeUnit;

public final class FormatUtil {

    public static String formatTime(long milliseconds) {
        final long days = TimeUnit.MILLISECONDS.toDays(milliseconds);
        final long hours = TimeUnit.MILLISECONDS.toHours(milliseconds - TimeUnit.DAYS.toMillis(days));
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours));
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes));

        if(days > 0) return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        if(hours > 0) return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return String.format("%02d:%02d", minutes, seconds);
    }

}
