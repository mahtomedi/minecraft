package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CombatTracker {
    private final List<CombatEntry> entries = Lists.newArrayList();
    private final LivingEntity mob;
    private int lastDamageTime;
    private int combatStartTime;
    private int combatEndTime;
    private boolean inCombat;
    private boolean takingDamage;
    private String nextLocation;

    public CombatTracker(LivingEntity param0) {
        this.mob = param0;
    }

    public void prepareForDamage() {
        this.resetPreparedStatus();
        Optional<BlockPos> var0 = this.mob.getLastClimbablePos();
        if (var0.isPresent()) {
            BlockState var1 = this.mob.level.getBlockState(var0.get());
            if (var1.is(Blocks.LADDER) || var1.is(BlockTags.TRAPDOORS)) {
                this.nextLocation = "ladder";
            } else if (var1.is(Blocks.VINE)) {
                this.nextLocation = "vines";
            } else if (var1.is(Blocks.WEEPING_VINES) || var1.is(Blocks.WEEPING_VINES_PLANT)) {
                this.nextLocation = "weeping_vines";
            } else if (var1.is(Blocks.TWISTING_VINES) || var1.is(Blocks.TWISTING_VINES_PLANT)) {
                this.nextLocation = "twisting_vines";
            } else if (var1.is(Blocks.SCAFFOLDING)) {
                this.nextLocation = "scaffolding";
            } else {
                this.nextLocation = "other_climbable";
            }
        } else if (this.mob.isInWater()) {
            this.nextLocation = "water";
        }

    }

    public void recordDamage(DamageSource param0, float param1, float param2) {
        this.recheckStatus();
        this.prepareForDamage();
        CombatEntry var0 = new CombatEntry(param0, this.mob.tickCount, param1, param2, this.nextLocation, this.mob.fallDistance);
        this.entries.add(var0);
        this.lastDamageTime = this.mob.tickCount;
        this.takingDamage = true;
        if (var0.isCombatRelated() && !this.inCombat && this.mob.isAlive()) {
            this.inCombat = true;
            this.combatStartTime = this.mob.tickCount;
            this.combatEndTime = this.combatStartTime;
            this.mob.onEnterCombat();
        }

    }

    public Component getDeathMessage() {
        if (this.entries.isEmpty()) {
            return new TranslatableComponent("death.attack.generic", this.mob.getDisplayName());
        } else {
            CombatEntry var0 = this.getMostSignificantFall();
            CombatEntry var1 = this.entries.get(this.entries.size() - 1);
            Component var2 = var1.getAttackerName();
            Entity var3 = var1.getSource().getEntity();
            Component var5;
            if (var0 != null && var1.getSource() == DamageSource.FALL) {
                Component var4 = var0.getAttackerName();
                if (var0.getSource() == DamageSource.FALL || var0.getSource() == DamageSource.OUT_OF_WORLD) {
                    var5 = new TranslatableComponent("death.fell.accident." + this.getFallLocation(var0), this.mob.getDisplayName());
                } else if (var4 != null && (var2 == null || !var4.equals(var2))) {
                    Entity var6 = var0.getSource().getEntity();
                    ItemStack var7 = var6 instanceof LivingEntity ? ((LivingEntity)var6).getMainHandItem() : ItemStack.EMPTY;
                    if (!var7.isEmpty() && var7.hasCustomHoverName()) {
                        var5 = new TranslatableComponent("death.fell.assist.item", this.mob.getDisplayName(), var4, var7.getDisplayName());
                    } else {
                        var5 = new TranslatableComponent("death.fell.assist", this.mob.getDisplayName(), var4);
                    }
                } else if (var2 != null) {
                    ItemStack var10 = var3 instanceof LivingEntity ? ((LivingEntity)var3).getMainHandItem() : ItemStack.EMPTY;
                    if (!var10.isEmpty() && var10.hasCustomHoverName()) {
                        var5 = new TranslatableComponent("death.fell.finish.item", this.mob.getDisplayName(), var2, var10.getDisplayName());
                    } else {
                        var5 = new TranslatableComponent("death.fell.finish", this.mob.getDisplayName(), var2);
                    }
                } else {
                    var5 = new TranslatableComponent("death.fell.killer", this.mob.getDisplayName());
                }
            } else {
                var5 = var1.getSource().getLocalizedDeathMessage(this.mob);
            }

            return var5;
        }
    }

    @Nullable
    public LivingEntity getKiller() {
        LivingEntity var0 = null;
        Player var1 = null;
        float var2 = 0.0F;
        float var3 = 0.0F;

        for(CombatEntry var4 : this.entries) {
            if (var4.getSource().getEntity() instanceof Player && (var1 == null || var4.getDamage() > var3)) {
                var3 = var4.getDamage();
                var1 = (Player)var4.getSource().getEntity();
            }

            if (var4.getSource().getEntity() instanceof LivingEntity && (var0 == null || var4.getDamage() > var2)) {
                var2 = var4.getDamage();
                var0 = (LivingEntity)var4.getSource().getEntity();
            }
        }

        return (LivingEntity)(var1 != null && var3 >= var2 / 3.0F ? var1 : var0);
    }

    @Nullable
    private CombatEntry getMostSignificantFall() {
        CombatEntry var0 = null;
        CombatEntry var1 = null;
        float var2 = 0.0F;
        float var3 = 0.0F;

        for(int var4 = 0; var4 < this.entries.size(); ++var4) {
            CombatEntry var5 = this.entries.get(var4);
            CombatEntry var6 = var4 > 0 ? this.entries.get(var4 - 1) : null;
            if ((var5.getSource() == DamageSource.FALL || var5.getSource() == DamageSource.OUT_OF_WORLD)
                && var5.getFallDistance() > 0.0F
                && (var0 == null || var5.getFallDistance() > var3)) {
                if (var4 > 0) {
                    var0 = var6;
                } else {
                    var0 = var5;
                }

                var3 = var5.getFallDistance();
            }

            if (var5.getLocation() != null && (var1 == null || var5.getDamage() > var2)) {
                var1 = var5;
                var2 = var5.getDamage();
            }
        }

        if (var3 > 5.0F && var0 != null) {
            return var0;
        } else {
            return var2 > 5.0F && var1 != null ? var1 : null;
        }
    }

    private String getFallLocation(CombatEntry param0) {
        return param0.getLocation() == null ? "generic" : param0.getLocation();
    }

    public int getCombatDuration() {
        return this.inCombat ? this.mob.tickCount - this.combatStartTime : this.combatEndTime - this.combatStartTime;
    }

    private void resetPreparedStatus() {
        this.nextLocation = null;
    }

    public void recheckStatus() {
        int var0 = this.inCombat ? 300 : 100;
        if (this.takingDamage && (!this.mob.isAlive() || this.mob.tickCount - this.lastDamageTime > var0)) {
            boolean var1 = this.inCombat;
            this.takingDamage = false;
            this.inCombat = false;
            this.combatEndTime = this.mob.tickCount;
            if (var1) {
                this.mob.onLeaveCombat();
            }

            this.entries.clear();
        }

    }

    public LivingEntity getMob() {
        return this.mob;
    }
}
