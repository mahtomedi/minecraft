package net.minecraft.realms;

import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLevelSummary implements Comparable<RealmsLevelSummary> {
    private final LevelSummary levelSummary;

    public RealmsLevelSummary(LevelSummary param0) {
        this.levelSummary = param0;
    }

    public int getGameMode() {
        return this.levelSummary.getGameMode().getId();
    }

    public String getLevelId() {
        return this.levelSummary.getLevelId();
    }

    public boolean hasCheats() {
        return this.levelSummary.hasCheats();
    }

    public boolean isHardcore() {
        return this.levelSummary.isHardcore();
    }

    public boolean isRequiresConversion() {
        return this.levelSummary.isRequiresConversion();
    }

    public String getLevelName() {
        return this.levelSummary.getLevelName();
    }

    public long getLastPlayed() {
        return this.levelSummary.getLastPlayed();
    }

    public int compareTo(LevelSummary param0) {
        return this.levelSummary.compareTo(param0);
    }

    public long getSizeOnDisk() {
        return this.levelSummary.getSizeOnDisk();
    }

    public int compareTo(RealmsLevelSummary param0) {
        if (this.levelSummary.getLastPlayed() < param0.getLastPlayed()) {
            return 1;
        } else {
            return this.levelSummary.getLastPlayed() > param0.getLastPlayed() ? -1 : this.levelSummary.getLevelId().compareTo(param0.getLevelId());
        }
    }
}
