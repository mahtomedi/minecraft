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
        ItemStack var0 = this.entity instanceof LivingEntity ? ((LivingEntity)this.entity).getMainHandItem() : ItemStack.EMPTY;
        String var1 = "death.attack." + this.msgId;
        return !var0.isEmpty() && var0.hasCustomHoverName()
            ? Component.translatable(var1 + ".item", param0.getDisplayName(), this.entity.getDisplayName(), var0.getDisplayName())
            : Component.translatable(var1, param0.getDisplayName(), this.entity.getDisplayName());
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
