package net.minecraft.world.level;

import net.minecraft.core.BlockPos;

public interface LevelHeightAccessor {
    int getSectionsCount();

    int getMinSection();

    default int getMaxSection() {
        return this.getMinSection() + this.getSectionsCount();
    }

    default int getHeight() {
        return this.getSectionsCount() * 16;
    }

    default int getMinBuildHeight() {
        return this.getMinSection() * 16;
    }

    default int getMaxBuildHeight() {
        return this.getMinBuildHeight() + this.getHeight();
    }

    default boolean isOutsideBuildHeight(BlockPos param0) {
        return this.isOutsideBuildHeight(param0.getY());
    }

    default boolean isOutsideBuildHeight(int param0) {
        return param0 < this.getMinBuildHeight() || param0 >= this.getMaxBuildHeight();
    }

    default int getSectionIndex(int param0) {
        return this.getSectionIndexFromSectionY(param0 >> 4);
    }

    default int getSectionIndexFromSectionY(int param0) {
        return param0 - this.getMinSection();
    }

    default int getSectionYFromSectionIndex(int param0) {
        return param0 + this.getMinSection();
    }
}
