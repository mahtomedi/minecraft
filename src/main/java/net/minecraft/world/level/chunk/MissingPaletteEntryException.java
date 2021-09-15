package net.minecraft.world.level.chunk;

public class MissingPaletteEntryException extends RuntimeException {
    public MissingPaletteEntryException(int param0) {
        super("Missing Palette entry for index " + param0 + ".");
    }
}
