package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class IndirectEntityDamageSource extends EntityDamageSource {
    @Nullable
    private final Entity owner;

    public IndirectEntityDamageSource(String param0, Entity param1, @Nullable Entity param2) {
        super(param0, param1);
        this.owner = param2;
    }

    @Nullable
    @Override
    public Entity getDirectEntity() {
        return this.entity;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        return this.owner;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity param0) {
        Component var0 = this.owner == null ? this.entity.getDisplayName() : this.owner.getDisplayName();
        ItemStack var1 = this.owner instanceof LivingEntity ? ((LivingEntity)this.owner).getMainHandItem() : ItemStack.EMPTY;
        String var2 = "death.attack." + this.msgId;
        String var3 = var2 + ".item";
        return !var1.isEmpty() && var1.hasCustomHoverName()
            ? Component.translatable(var3, param0.getDisplayName(), var0, var1.getDisplayName())
            : Component.translatable(var2, param0.getDisplayName(), var0);
    }
}
