package net.minecraft.network.chat.contents;

import java.util.Locale;

public class TranslatableFormatException extends IllegalArgumentException {
    public TranslatableFormatException(TranslatableContents param0, String param1) {
        super(String.format(Locale.ROOT, "Error parsing: %s: %s", param0, param1));
    }

    public TranslatableFormatException(TranslatableContents param0, int param1) {
        super(String.format(Locale.ROOT, "Invalid index %d requested for %s", param1, param0));
    }

    public TranslatableFormatException(TranslatableContents param0, Throwable param1) {
        super(String.format(Locale.ROOT, "Error while parsing: %s", param0), param1);
    }
}
