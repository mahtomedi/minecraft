package net.minecraft.world;

import javax.annotation.concurrent.Immutable;
import net.minecraft.util.Mth;

@Immutable
public class DifficultyInstance {
    private static final float DIFFICULTY_TIME_GLOBAL_OFFSET = -72000.0F;
    private static final float MAX_DIFFICULTY_TIME_GLOBAL = 1440000.0F;
    private static final float MAX_DIFFICULTY_TIME_LOCAL = 3600000.0F;
    private final Difficulty base;
    private final float effectiveDifficulty;

    public DifficultyInstance(Difficulty param0, long param1, long param2, float param3) {
        this.base = param0;
        this.effectiveDifficulty = this.calculateDifficulty(param0, param1, param2, param3);
    }

    public Difficulty getDifficulty() {
        return this.base;
    }

    public float getEffectiveDifficulty() {
        return this.effectiveDifficulty;
    }

    public boolean isHard() {
        return this.effectiveDifficulty >= (float)Difficulty.HARD.ordinal();
    }

    public boolean isHarderThan(float param0) {
        return this.effectiveDifficulty > param0;
    }

    public float getSpecialMultiplier() {
        if (this.effectiveDifficulty < 2.0F) {
            return 0.0F;
        } else {
            return this.effectiveDifficulty > 4.0F ? 1.0F : (this.effectiveDifficulty - 2.0F) / 2.0F;
        }
    }

    private float calculateDifficulty(Difficulty param0, long param1, long param2, float param3) {
        if (param0 == Difficulty.PEACEFUL) {
            return 0.0F;
        } else {
            boolean var0 = param0 == Difficulty.HARD;
            float var1 = 0.75F;
            float var2 = Mth.clamp(((float)param1 + -72000.0F) / 1440000.0F, 0.0F, 1.0F) * 0.25F;
            var1 += var2;
            float var3 = 0.0F;
            var3 += Mth.clamp((float)param2 / 3600000.0F, 0.0F, 1.0F) * (var0 ? 1.0F : 0.75F);
            var3 += Mth.clamp(param3 * 0.25F, 0.0F, var2);
            if (param0 == Difficulty.EASY) {
                var3 *= 0.5F;
            }

            var1 += var3;
            return (float)param0.getId() * var1;
        }
    }
}
