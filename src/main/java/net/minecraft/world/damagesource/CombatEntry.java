package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class CombatEntry {
    private final DamageSource source;
    private final int time;
    private final float damage;
    private final float health;
    private final String location;
    private final float fallDistance;

    public CombatEntry(DamageSource param0, int param1, float param2, float param3, String param4, float param5) {
        this.source = param0;
        this.time = param1;
        this.damage = param3;
        this.health = param2;
        this.location = param4;
        this.fallDistance = param5;
    }

    public DamageSource getSource() {
        return this.source;
    }

    public int getTime() {
        return this.time;
    }

    public float getDamage() {
        return this.damage;
    }

    public float getHealthBeforeDamage() {
        return this.health;
    }

    public float getHealthAfterDamage() {
        return this.health - this.damage;
    }

    public boolean isCombatRelated() {
        return this.source.getEntity() instanceof LivingEntity;
    }

    @Nullable
    public String getLocation() {
        return this.location;
    }

    @Nullable
    public Component getAttackerName() {
        return this.getSource().getEntity() == null ? null : this.getSource().getEntity().getDisplayName();
    }

    @Nullable
    public Entity getAttacker() {
        return this.getSource().getEntity();
    }

    public float getFallDistance() {
        return this.source == DamageSource.OUT_OF_WORLD ? Float.MAX_VALUE : this.fallDistance;
    }
}
