package net.minecraft.world.level;

public enum TickPriority {
    EXTREMELY_HIGH(-3),
    VERY_HIGH(-2),
    HIGH(-1),
    NORMAL(0),
    LOW(1),
    VERY_LOW(2),
    EXTREMELY_LOW(3);

    private final int value;

    private TickPriority(int param0) {
        this.value = param0;
    }

    public static TickPriority byValue(int param0) {
        for(TickPriority var0 : values()) {
            if (var0.value == param0) {
                return var0;
            }
        }

        return param0 < EXTREMELY_HIGH.value ? EXTREMELY_HIGH : EXTREMELY_LOW;
    }

    public int getValue() {
        return this.value;
    }
}
