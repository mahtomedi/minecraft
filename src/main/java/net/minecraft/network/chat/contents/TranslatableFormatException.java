package net.minecraft.network.chat.contents;

public class TranslatableFormatException extends IllegalArgumentException {
    public TranslatableFormatException(TranslatableContents param0, String param1) {
        super(String.format("Error parsing: %s: %s", param0, param1));
    }

    public TranslatableFormatException(TranslatableContents param0, int param1) {
        super(String.format("Invalid index %d requested for %s", param1, param0));
    }

    public TranslatableFormatException(TranslatableContents param0, Throwable param1) {
        super(String.format("Error while parsing: %s", param0), param1);
    }
}
