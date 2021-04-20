package me.m1dnightninja.midnightcore.common;

import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.util.ArrayList;
import java.util.List;
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

/*
    public static MComponent formatMessage(String message, ILangModule mod, Object... args) {

        if(message == null) return MComponent.createTextComponent("");

        // Scan for placeholders before converting to component

        StringBuilder currentLiteral = new StringBuilder();
        StringBuilder currentPlaceholder = new StringBuilder();

        boolean placeholderStarted = false;

        for(int i = 0 ; i < message.length() ; i++) {
            char c = message.charAt(i);

            if(c == '%') {

                if(placeholderStarted) {
                    placeholderStarted = false;
                    currentLiteral.append(mod.getStringPlaceholderValue(currentPlaceholder.toString(), args));
                    currentPlaceholder = new StringBuilder();
                } else {
                    placeholderStarted = true;
                }

            } else {

                if (placeholderStarted) {
                    currentPlaceholder.append(c);
                } else {
                    currentLiteral.append(c);
                }
            }

        }
        currentLiteral.append(currentPlaceholder);
        message = currentLiteral.toString();

        MComponent orig = MComponent.Parser.fromJson(message);

        MComponent out = MComponent.createTextComponent("").withStyle(orig.getStyle());

        List<MComponent> unformatted = new ArrayList<>();
        unformatted.add(orig);
        unformatted.addAll(orig.getAllChildren());

        List<MComponent> texts = new ArrayList<>();

        for(MComponent cmp : unformatted) {

            placeholderStarted = false;
            currentLiteral = new StringBuilder();
            currentPlaceholder = new StringBuilder();

            String msg = cmp.getContent();

            for (int i = 0; i < msg.length(); i++) {

                char c = msg.charAt(i);

                if (c == '%') {

                    if (placeholderStarted) {
                        placeholderStarted = false;
                        texts.add(MComponent.createTextComponent(currentLiteral.toString()).withStyle(cmp.getStyle()));

                        MComponent placeholder = mod.getRawPlaceholderValue(currentPlaceholder.toString(), args);
                        if(placeholder != null) texts.add(placeholder);

                        currentLiteral = new StringBuilder();
                        currentPlaceholder = new StringBuilder();
                    } else {
                        placeholderStarted = true;
                    }

                } else {

                    if (placeholderStarted) {
                        currentPlaceholder.append(c);
                    } else {
                        currentLiteral.append(c);
                    }

                }
            }

            currentLiteral.append(currentPlaceholder);
            if(currentLiteral.length() > 0) {
                texts.add(MComponent.createTextComponent(currentLiteral.toString()).withStyle(cmp.getStyle()));
            }

        }

        for(MComponent c : texts) {
            out.addChild(c);
        }

        return out;
    }
*/

}
