package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;

public abstract class OptionalDispenseItemBehavior extends DefaultDispenseItemBehavior {
    private boolean success = true;

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean param0) {
        this.success = param0;
    }

    @Override
    protected void playSound(BlockSource param0) {
        param0.getLevel().levelEvent(this.isSuccess() ? 1000 : 1001, param0.getPos(), 0);
    }
}
