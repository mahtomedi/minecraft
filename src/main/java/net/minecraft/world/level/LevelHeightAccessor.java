package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LevelHeightAccessor {
    int getHeight();

    int getMinBuildHeight();

    default int getMaxBuildHeight() {
        return this.getMinBuildHeight() + this.getHeight();
    }

    default int getSectionsCount() {
        return this.getMaxSection() - this.getMinSection();
    }

    default int getMinSection() {
        return SectionPos.blockToSectionCoord(this.getMinBuildHeight());
    }

    default int getMaxSection() {
        return SectionPos.blockToSectionCoord(this.getMaxBuildHeight() - 1) + 1;
    }

    default boolean isOutsideBuildHeight(BlockPos param0) {
        return this.isOutsideBuildHeight(param0.getY());
    }

    default boolean isOutsideBuildHeight(int param0) {
        return param0 < this.getMinBuildHeight() || param0 >= this.getMaxBuildHeight();
    }

    default int getSectionIndex(int param0) {
        return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(param0));
    }

    default int getSectionIndexFromSectionY(int param0) {
        return param0 - this.getMinSection();
    }

    default int getSectionYFromSectionIndex(int param0) {
        return param0 + this.getMinSection();
    }

    static LevelHeightAccessor create(final int param0, final int param1) {
        return new LevelHeightAccessor() {
            @Override
            public int getHeight() {
                return param1;
            }

            @Override
            public int getMinBuildHeight() {
                return param0;
            }
        };
    }
}
