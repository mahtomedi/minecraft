package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum GraphicsStatus {
    FAST(0, "options.graphics.fast"),
    FANCY(1, "options.graphics.fancy"),
    FABULOUS(2, "options.graphics.fabulous");

    private static final GraphicsStatus[] BY_ID = Arrays.stream(values())
        .sorted(Comparator.comparingInt(GraphicsStatus::getId))
        .toArray(param0 -> new GraphicsStatus[param0]);
    private final int id;
    private final String key;

    private GraphicsStatus(int param0, String param1) {
        this.id = param0;
        this.key = param1;
    }

    public int getId() {
        return this.id;
    }

    public String getKey() {
        return this.key;
    }

    public GraphicsStatus cycleNext() {
        return byId(this.getId() + 1);
    }

    @Override
    public String toString() {
        switch(this) {
            case FAST:
                return "fast";
            case FANCY:
                return "fancy";
            case FABULOUS:
                return "fabulous";
            default:
                throw new IllegalArgumentException();
        }
    }

    public static GraphicsStatus byId(int param0) {
        return BY_ID[Mth.positiveModulo(param0, BY_ID.length)];
    }
}
