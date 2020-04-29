package net.minecraft.world.level.storage;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface WorldData {
    @OnlyIn(Dist.CLIENT)
    int getMinecraftVersion();

    @OnlyIn(Dist.CLIENT)
    boolean isSnapshot();

    @OnlyIn(Dist.CLIENT)
    String getMinecraftVersionName();

    Set<String> getDisabledDataPacks();

    Set<String> getEnabledDataPacks();

    boolean wasModded();

    Set<String> getKnownServerBrands();

    void setModdedInfo(String var1, boolean var2);

    default void fillCrashReportCategory(CrashReportCategory param0) {
        param0.setDetail("Known server brands", () -> String.join(", ", this.getKnownServerBrands()));
        param0.setDetail("Level was modded", () -> Boolean.toString(this.wasModded()));
        param0.setDetail("Level storage version", () -> {
            int var0 = this.getVersion();
            return String.format("0x%05X - %s", var0, this.getStorageVersionName(var0));
        });
    }

    default String getStorageVersionName(int param0) {
        switch(param0) {
            case 19132:
                return "McRegion";
            case 19133:
                return "Anvil";
            default:
                return "Unknown?";
        }
    }

    @Nullable
    CompoundTag getCustomBossEvents();

    void setCustomBossEvents(@Nullable CompoundTag var1);

    ServerLevelData overworldData();

    LevelSettings getLevelSettings();

    CompoundTag createTag(@Nullable CompoundTag var1);

    boolean isHardcore();

    int getVersion();

    String getLevelName();

    GameType getGameType();

    void setGameType(GameType var1);

    @OnlyIn(Dist.CLIENT)
    long getLastPlayed();

    long getSeed();

    boolean getAllowCommands();

    Difficulty getDifficulty();

    void setDifficulty(Difficulty var1);

    boolean isDifficultyLocked();

    void setDifficultyLocked(boolean var1);

    GameRules getGameRules();

    CompoundTag getLoadedPlayerTag();

    CompoundTag getDimensionData(DimensionType var1);

    void setDimensionData(DimensionType var1, CompoundTag var2);
}
