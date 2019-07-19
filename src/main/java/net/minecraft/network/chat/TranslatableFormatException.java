package net.minecraft.network.chat;

public class TranslatableFormatException extends IllegalArgumentException {
    public TranslatableFormatException(TranslatableComponent param0, String param1) {
        super(String.format("Error parsing: %s: %s", param0, param1));
    }

    public TranslatableFormatException(TranslatableComponent param0, int param1) {
        super(String.format("Invalid index %d requested for %s", param1, param0));
    }

    public TranslatableFormatException(TranslatableComponent param0, Throwable param1) {
        super(String.format("Error while parsing: %s", param0), param1);
    }
}
