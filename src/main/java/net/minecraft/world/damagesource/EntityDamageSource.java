package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EntityDamageSource extends DamageSource {
    protected final Entity entity;
    private boolean isThorns;

    public EntityDamageSource(String param0, Entity param1) {
        super(param0);
        this.entity = param1;
    }

    public EntityDamageSource setThorns() {
        this.isThorns = true;
        return this;
    }

    public boolean isThorns() {
        return this.isThorns;
    }

    @Override
    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity param0) {
        Entity var4 = this.entity;
        ItemStack var1 = var4 instanceof LivingEntity var0 ? var0.getMainHandItem() : ItemStack.EMPTY;
        String var2 = "death.attack." + this.msgId;
        return !var1.isEmpty() && var1.hasCustomHoverName()
            ? Component.translatable(var2 + ".item", param0.getDisplayName(), this.entity.getDisplayName(), var1.getDisplayName())
            : Component.translatable(var2, param0.getDisplayName(), this.entity.getDisplayName());
    }

    @Override
    public boolean scalesWithDifficulty() {
        return this.entity instanceof LivingEntity && !(this.entity instanceof Player);
    }

    @Nullable
    @Override
    public Vec3 getSourcePosition() {
        return this.entity.position();
    }

    @Override
    public String toString() {
        return "EntityDamageSource (" + this.entity + ")";
    }
}
