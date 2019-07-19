package net.minecraft.world.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FoodData {
    private int foodLevel = 20;
    private float saturationLevel;
    private float exhaustionLevel;
    private int tickTimer;
    private int lastFoodLevel = 20;

    public FoodData() {
        this.saturationLevel = 5.0F;
    }

    public void eat(int param0, float param1) {
        this.foodLevel = Math.min(param0 + this.foodLevel, 20);
        this.saturationLevel = Math.min(this.saturationLevel + (float)param0 * param1 * 2.0F, (float)this.foodLevel);
    }

    public void eat(Item param0, ItemStack param1) {
        if (param0.isEdible()) {
            FoodProperties var0 = param0.getFoodProperties();
            this.eat(var0.getNutrition(), var0.getSaturationModifier());
        }

    }

    public void tick(Player param0) {
        Difficulty var0 = param0.level.getDifficulty();
        this.lastFoodLevel = this.foodLevel;
        if (this.exhaustionLevel > 4.0F) {
            this.exhaustionLevel -= 4.0F;
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
            } else if (var0 != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        boolean var1 = param0.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
        if (var1 && this.saturationLevel > 0.0F && param0.isHurt() && this.foodLevel >= 20) {
            ++this.tickTimer;
            if (this.tickTimer >= 10) {
                float var2 = Math.min(this.saturationLevel, 6.0F);
                param0.heal(var2 / 6.0F);
                this.addExhaustion(var2);
                this.tickTimer = 0;
            }
        } else if (var1 && this.foodLevel >= 18 && param0.isHurt()) {
            ++this.tickTimer;
            if (this.tickTimer >= 80) {
                param0.heal(1.0F);
                this.addExhaustion(6.0F);
                this.tickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.tickTimer;
            if (this.tickTimer >= 80) {
                if (param0.getHealth() > 10.0F || var0 == Difficulty.HARD || param0.getHealth() > 1.0F && var0 == Difficulty.NORMAL) {
                    param0.hurt(DamageSource.STARVE, 1.0F);
                }

                this.tickTimer = 0;
            }
        } else {
            this.tickTimer = 0;
        }

    }

    public void readAdditionalSaveData(CompoundTag param0) {
        if (param0.contains("foodLevel", 99)) {
            this.foodLevel = param0.getInt("foodLevel");
            this.tickTimer = param0.getInt("foodTickTimer");
            this.saturationLevel = param0.getFloat("foodSaturationLevel");
            this.exhaustionLevel = param0.getFloat("foodExhaustionLevel");
        }

    }

    public void addAdditionalSaveData(CompoundTag param0) {
        param0.putInt("foodLevel", this.foodLevel);
        param0.putInt("foodTickTimer", this.tickTimer);
        param0.putFloat("foodSaturationLevel", this.saturationLevel);
        param0.putFloat("foodExhaustionLevel", this.exhaustionLevel);
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public boolean needsFood() {
        return this.foodLevel < 20;
    }

    public void addExhaustion(float param0) {
        this.exhaustionLevel = Math.min(this.exhaustionLevel + param0, 40.0F);
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public void setFoodLevel(int param0) {
        this.foodLevel = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public void setSaturation(float param0) {
        this.saturationLevel = param0;
    }
}
