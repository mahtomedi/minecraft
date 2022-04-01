package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum CaveSurface implements StringRepresentable {
    CEILING(Direction.UP, 1, "ceiling"),
    FLOOR(Direction.DOWN, -1, "floor");

    public static final Codec<CaveSurface> CODEC = StringRepresentable.fromEnum(CaveSurface::values, CaveSurface::byName);
    private final Direction direction;
    private final int y;
    private final String id;
    private static final CaveSurface[] VALUES = values();

    private CaveSurface(Direction param0, int param1, String param2) {
        this.direction = param0;
        this.y = param1;
        this.id = param2;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public int getY() {
        return this.y;
    }

    public static CaveSurface byName(String param0) {
        for(CaveSurface var0 : VALUES) {
            if (var0.getSerializedName().equals(param0)) {
                return var0;
            }
        }

        throw new IllegalArgumentException("Unknown Surface type: " + param0);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
