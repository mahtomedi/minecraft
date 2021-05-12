package net.minecraft.world.food;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;

public class FoodProperties {
    private final int nutrition;
    private final float saturationModifier;
    private final boolean isMeat;
    private final boolean canAlwaysEat;
    private final boolean fastFood;
    private final List<Pair<MobEffectInstance, Float>> effects;

    FoodProperties(int param0, float param1, boolean param2, boolean param3, boolean param4, List<Pair<MobEffectInstance, Float>> param5) {
        this.nutrition = param0;
        this.saturationModifier = param1;
        this.isMeat = param2;
        this.canAlwaysEat = param3;
        this.fastFood = param4;
        this.effects = param5;
    }

    public int getNutrition() {
        return this.nutrition;
    }

    public float getSaturationModifier() {
        return this.saturationModifier;
    }

    public boolean isMeat() {
        return this.isMeat;
    }

    public boolean canAlwaysEat() {
        return this.canAlwaysEat;
    }

    public boolean isFastFood() {
        return this.fastFood;
    }

    public List<Pair<MobEffectInstance, Float>> getEffects() {
        return this.effects;
    }

    public static class Builder {
        private int nutrition;
        private float saturationModifier;
        private boolean isMeat;
        private boolean canAlwaysEat;
        private boolean fastFood;
        private final List<Pair<MobEffectInstance, Float>> effects = Lists.newArrayList();

        public FoodProperties.Builder nutrition(int param0) {
            this.nutrition = param0;
            return this;
        }

        public FoodProperties.Builder saturationMod(float param0) {
            this.saturationModifier = param0;
            return this;
        }

        public FoodProperties.Builder meat() {
            this.isMeat = true;
            return this;
        }

        public FoodProperties.Builder alwaysEat() {
            this.canAlwaysEat = true;
            return this;
        }

        public FoodProperties.Builder fast() {
            this.fastFood = true;
            return this;
        }

        public FoodProperties.Builder effect(MobEffectInstance param0, float param1) {
            this.effects.add(Pair.of(param0, param1));
            return this;
        }

        public FoodProperties build() {
            return new FoodProperties(this.nutrition, this.saturationModifier, this.isMeat, this.canAlwaysEat, this.fastFood, this.effects);
        }
    }
}
