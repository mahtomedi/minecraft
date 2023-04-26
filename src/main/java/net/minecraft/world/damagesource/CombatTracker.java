package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CombatTracker {
    public static final int RESET_DAMAGE_STATUS_TIME = 100;
    public static final int RESET_COMBAT_STATUS_TIME = 300;
    private static final Style INTENTIONAL_GAME_DESIGN_STYLE = Style.EMPTY
        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723"))
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("MCPE-28723")));
    private final List<CombatEntry> entries = Lists.newArrayList();
    private final LivingEntity mob;
    private int lastDamageTime;
    private int combatStartTime;
    private int combatEndTime;
    private boolean inCombat;
    private boolean takingDamage;
    @Nullable
    private String nextLocation;

    public CombatTracker(LivingEntity param0) {
        this.mob = param0;
    }

    public void prepareForDamage() {
        this.resetPreparedStatus();
        Optional<BlockPos> var0 = this.mob.getLastClimbablePos();
        if (var0.isPresent()) {
            BlockState var1 = this.mob.level().getBlockState(var0.get());
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
            return Component.translatable("death.attack.generic", this.mob.getDisplayName());
        } else {
            CombatEntry var0 = this.getMostSignificantFall();
            CombatEntry var1 = this.entries.get(this.entries.size() - 1);
            Component var2 = var1.getAttackerName();
            DamageSource var3 = var1.getSource();
            Entity var4 = var3.getEntity();
            DeathMessageType var5 = var3.type().deathMessageType();
            Component var8;
            if (var0 != null && var5 == DeathMessageType.FALL_VARIANTS) {
                Component var6 = var0.getAttackerName();
                DamageSource var7 = var0.getSource();
                if (var7.is(DamageTypeTags.IS_FALL) || var7.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)) {
                    var8 = Component.translatable("death.fell.accident." + this.getFallLocation(var0), this.mob.getDisplayName());
                } else if (var6 != null && !var6.equals(var2)) {
                    Entity var9 = var7.getEntity();
                    ItemStack var11 = var9 instanceof LivingEntity var10 ? var10.getMainHandItem() : ItemStack.EMPTY;
                    if (!var11.isEmpty() && var11.hasCustomHoverName()) {
                        var8 = Component.translatable("death.fell.assist.item", this.mob.getDisplayName(), var6, var11.getDisplayName());
                    } else {
                        var8 = Component.translatable("death.fell.assist", this.mob.getDisplayName(), var6);
                    }
                } else if (var2 != null) {
                    ItemStack var15 = var4 instanceof LivingEntity var14 ? var14.getMainHandItem() : ItemStack.EMPTY;
                    if (!var15.isEmpty() && var15.hasCustomHoverName()) {
                        var8 = Component.translatable("death.fell.finish.item", this.mob.getDisplayName(), var2, var15.getDisplayName());
                    } else {
                        var8 = Component.translatable("death.fell.finish", this.mob.getDisplayName(), var2);
                    }
                } else {
                    var8 = Component.translatable("death.fell.killer", this.mob.getDisplayName());
                }
            } else {
                if (var5 == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
                    String var19 = "death.attack." + var3.getMsgId();
                    Component var20 = ComponentUtils.wrapInSquareBrackets(Component.translatable(var19 + ".link")).withStyle(INTENTIONAL_GAME_DESIGN_STYLE);
                    return Component.translatable(var19 + ".message", this.mob.getDisplayName(), var20);
                }

                var8 = var3.getLocalizedDeathMessage(this.mob);
            }

            return var8;
        }
    }

    @Nullable
    public LivingEntity getKiller() {
        LivingEntity var0 = null;
        Player var1 = null;
        float var2 = 0.0F;
        float var3 = 0.0F;

        for(CombatEntry var4 : this.entries) {
            Entity var8 = var4.getSource().getEntity();
            if (var8 instanceof Player var5 && (var1 == null || var4.getDamage() > var3)) {
                var3 = var4.getDamage();
                var1 = var5;
            }

            var8 = var4.getSource().getEntity();
            if (var8 instanceof LivingEntity var6 && (var0 == null || var4.getDamage() > var2)) {
                var2 = var4.getDamage();
                var0 = var6;
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
            DamageSource var7 = var5.getSource();
            boolean var8 = var7.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL);
            float var9 = var8 ? Float.MAX_VALUE : var5.getFallDistance();
            if ((var7.is(DamageTypeTags.IS_FALL) || var8) && var9 > 0.0F && (var0 == null || var9 > var3)) {
                if (var4 > 0) {
                    var0 = var6;
                } else {
                    var0 = var5;
                }

                var3 = var9;
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

    public boolean isTakingDamage() {
        this.recheckStatus();
        return this.takingDamage;
    }

    public boolean isInCombat() {
        this.recheckStatus();
        return this.inCombat;
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

    @Nullable
    public CombatEntry getLastEntry() {
        return this.entries.isEmpty() ? null : this.entries.get(this.entries.size() - 1);
    }

    public int getKillerId() {
        LivingEntity var0 = this.getKiller();
        return var0 == null ? -1 : var0.getId();
    }
}
