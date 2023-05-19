package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

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

    public CombatTracker(LivingEntity param0) {
        this.mob = param0;
    }

    public void recordDamage(DamageSource param0, float param1) {
        this.recheckStatus();
        FallLocation var0 = FallLocation.getCurrentFallLocation(this.mob);
        CombatEntry var1 = new CombatEntry(param0, param1, var0, this.mob.fallDistance);
        this.entries.add(var1);
        this.lastDamageTime = this.mob.tickCount;
        this.takingDamage = true;
        if (!this.inCombat && this.mob.isAlive() && shouldEnterCombat(param0)) {
            this.inCombat = true;
            this.combatStartTime = this.mob.tickCount;
            this.combatEndTime = this.combatStartTime;
            this.mob.onEnterCombat();
        }

    }

    private static boolean shouldEnterCombat(DamageSource param0) {
        return param0.getEntity() instanceof LivingEntity;
    }

    private Component getMessageForAssistedFall(Entity param0, Component param1, String param2, String param3) {
        ItemStack var1 = param0 instanceof LivingEntity var0 ? var0.getMainHandItem() : ItemStack.EMPTY;
        return !var1.isEmpty() && var1.hasCustomHoverName()
            ? Component.translatable(param2, this.mob.getDisplayName(), param1, var1.getDisplayName())
            : Component.translatable(param3, this.mob.getDisplayName(), param1);
    }

    private Component getFallMessage(CombatEntry param0, @Nullable Entity param1) {
        DamageSource var0 = param0.source();
        if (!var0.is(DamageTypeTags.IS_FALL) && !var0.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)) {
            Component var2 = getDisplayName(param1);
            Entity var3 = var0.getEntity();
            Component var4 = getDisplayName(var3);
            if (var4 != null && !var4.equals(var2)) {
                return this.getMessageForAssistedFall(var3, var4, "death.fell.assist.item", "death.fell.assist");
            } else {
                return (Component)(var2 != null
                    ? this.getMessageForAssistedFall(param1, var2, "death.fell.finish.item", "death.fell.finish")
                    : Component.translatable("death.fell.killer", this.mob.getDisplayName()));
            }
        } else {
            FallLocation var1 = Objects.requireNonNullElse(param0.fallLocation(), FallLocation.GENERIC);
            return Component.translatable(var1.languageKey(), this.mob.getDisplayName());
        }
    }

    @Nullable
    private static Component getDisplayName(@Nullable Entity param0) {
        return param0 == null ? null : param0.getDisplayName();
    }

    public Component getDeathMessage() {
        if (this.entries.isEmpty()) {
            return Component.translatable("death.attack.generic", this.mob.getDisplayName());
        } else {
            CombatEntry var0 = this.entries.get(this.entries.size() - 1);
            DamageSource var1 = var0.source();
            CombatEntry var2 = this.getMostSignificantFall();
            DeathMessageType var3 = var1.type().deathMessageType();
            if (var3 == DeathMessageType.FALL_VARIANTS && var2 != null) {
                return this.getFallMessage(var2, var1.getEntity());
            } else if (var3 == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
                String var4 = "death.attack." + var1.getMsgId();
                Component var5 = ComponentUtils.wrapInSquareBrackets(Component.translatable(var4 + ".link")).withStyle(INTENTIONAL_GAME_DESIGN_STYLE);
                return Component.translatable(var4 + ".message", this.mob.getDisplayName(), var5);
            } else {
                return var1.getLocalizedDeathMessage(this.mob);
            }
        }
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
            DamageSource var7 = var5.source();
            boolean var8 = var7.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL);
            float var9 = var8 ? Float.MAX_VALUE : var5.fallDistance();
            if ((var7.is(DamageTypeTags.IS_FALL) || var8) && var9 > 0.0F && (var0 == null || var9 > var3)) {
                if (var4 > 0) {
                    var0 = var6;
                } else {
                    var0 = var5;
                }

                var3 = var9;
            }

            if (var5.fallLocation() != null && (var1 == null || var5.damage() > var2)) {
                var1 = var5;
                var2 = var5.damage();
            }
        }

        if (var3 > 5.0F && var0 != null) {
            return var0;
        } else {
            return var2 > 5.0F && var1 != null ? var1 : null;
        }
    }

    public int getCombatDuration() {
        return this.inCombat ? this.mob.tickCount - this.combatStartTime : this.combatEndTime - this.combatStartTime;
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
}
