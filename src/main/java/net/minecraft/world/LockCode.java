package net.minecraft.world;

import javax.annotation.concurrent.Immutable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Immutable
public class LockCode {
    public static final LockCode NO_LOCK = new LockCode("");
    private final String key;

    public LockCode(String param0) {
        this.key = param0;
    }

    public boolean unlocksWith(ItemStack param0) {
        return this.key.isEmpty() || !param0.isEmpty() && param0.hasCustomHoverName() && this.key.equals(param0.getHoverName().getString());
    }

    public void addToTag(CompoundTag param0) {
        if (!this.key.isEmpty()) {
            param0.putString("Lock", this.key);
        }

    }

    public static LockCode fromTag(CompoundTag param0) {
        return param0.contains("Lock", 8) ? new LockCode(param0.getString("Lock")) : NO_LOCK;
    }
}
