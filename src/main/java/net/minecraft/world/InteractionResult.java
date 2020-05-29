package net.minecraft.world;

public enum InteractionResult {
    SUCCESS,
    CONSUME,
    PASS,
    FAIL;

    public boolean consumesAction() {
        return this == SUCCESS || this == CONSUME;
    }

    public boolean shouldSwing() {
        return this == SUCCESS;
    }

    public static InteractionResult sidedSuccess(boolean param0) {
        return param0 ? SUCCESS : CONSUME;
    }
}
