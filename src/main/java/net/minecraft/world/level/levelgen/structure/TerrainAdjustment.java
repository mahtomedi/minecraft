package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TerrainAdjustment implements StringRepresentable {
    NONE("none"),
    BURY("bury"),
    BEARD_THIN("beard_thin"),
    BEARD_BOX("beard_box");

    public static final Codec<TerrainAdjustment> CODEC = StringRepresentable.fromEnum(TerrainAdjustment::values);
    private final String id;

    private TerrainAdjustment(String param0) {
        this.id = param0;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
