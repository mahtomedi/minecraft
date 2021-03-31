package net.minecraft.world.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum HumanoidArm {
    LEFT(new TranslatableComponent("options.mainHand.left")),
    RIGHT(new TranslatableComponent("options.mainHand.right"));

    private final Component name;

    private HumanoidArm(Component param0) {
        this.name = param0;
    }

    public HumanoidArm getOpposite() {
        return this == LEFT ? RIGHT : LEFT;
    }

    @Override
    public String toString() {
        return this.name.getString();
    }

    public Component getName() {
        return this.name;
    }
}
