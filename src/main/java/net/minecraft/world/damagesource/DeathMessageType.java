package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DeathMessageType implements StringRepresentable {
    DEFAULT("default"),
    FALL_VARIANTS("fall_variants"),
    INTENTIONAL_GAME_DESIGN("intentional_game_design");

    public static final Codec<DeathMessageType> CODEC = StringRepresentable.fromEnum(DeathMessageType::values);
    private final String id;

    private DeathMessageType(String param0) {
        this.id = param0;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
