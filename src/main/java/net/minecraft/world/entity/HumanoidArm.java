package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.StringRepresentable;

public enum HumanoidArm implements OptionEnum, StringRepresentable {
    LEFT(0, "left", "options.mainHand.left"),
    RIGHT(1, "right", "options.mainHand.right");

    public static final Codec<HumanoidArm> CODEC = StringRepresentable.fromEnum(HumanoidArm::values);
    public static final IntFunction<HumanoidArm> BY_ID = ByIdMap.continuous(HumanoidArm::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    private final int id;
    private final String name;
    private final String translationKey;

    private HumanoidArm(int param0, String param1, String param2) {
        this.id = param0;
        this.name = param1;
        this.translationKey = param2;
    }

    public HumanoidArm getOpposite() {
        return this == LEFT ? RIGHT : LEFT;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.translationKey;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
