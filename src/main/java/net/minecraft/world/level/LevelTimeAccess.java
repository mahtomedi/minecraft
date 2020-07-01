package net.minecraft.world.level;

import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface LevelTimeAccess extends LevelReader {
    long dayTime();

    default float getMoonBrightness() {
        return DimensionType.MOON_BRIGHTNESS_PER_PHASE[this.dimensionType().moonPhase(this.dayTime())];
    }

    default float getTimeOfDay(float param0) {
        return this.dimensionType().timeOfDay(this.dayTime());
    }

    @OnlyIn(Dist.CLIENT)
    default int getMoonPhase() {
        return this.dimensionType().moonPhase(this.dayTime());
    }
}
