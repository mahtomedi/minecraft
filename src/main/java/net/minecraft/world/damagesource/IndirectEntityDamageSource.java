package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class IndirectEntityDamageSource extends EntityDamageSource {
    @Nullable
    private final Entity cause;

    public IndirectEntityDamageSource(String param0, Entity param1, @Nullable Entity param2) {
        super(param0, param1);
        this.cause = param2;
    }

    @Nullable
    @Override
    public Entity getDirectEntity() {
        return this.entity;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        return this.cause;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity param0) {
        Component var0 = this.cause == null ? this.entity.getDisplayName() : this.cause.getDisplayName();
        Entity var4 = this.cause;
        ItemStack var2 = var4 instanceof LivingEntity var1 ? var1.getMainHandItem() : ItemStack.EMPTY;
        String var3 = "death.attack." + this.msgId;
        if (!var2.isEmpty() && var2.hasCustomHoverName()) {
            String var4x = var3 + ".item";
            return Component.translatable(var4x, param0.getDisplayName(), var0, var2.getDisplayName());
        } else {
            return Component.translatable(var3, param0.getDisplayName(), var0);
        }
    }
}
