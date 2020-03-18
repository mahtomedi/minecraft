package net.minecraft.world.level.storage;

import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.timers.TimerQueue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DerivedLevelData extends LevelData {
    private final LevelData wrapped;

    public DerivedLevelData(LevelData param0) {
        this.wrapped = param0;
    }

    @Override
    public CompoundTag createTag(@Nullable CompoundTag param0) {
        return this.wrapped.createTag(param0);
    }

    @Override
    public long getSeed() {
        return this.wrapped.getSeed();
    }

    @Override
    public int getXSpawn() {
        return this.wrapped.getXSpawn();
    }

    @Override
    public int getYSpawn() {
        return this.wrapped.getYSpawn();
    }

    @Override
    public int getZSpawn() {
        return this.wrapped.getZSpawn();
    }

    @Override
    public long getGameTime() {
        return this.wrapped.getGameTime();
    }

    @Override
    public long getDayTime() {
        return this.wrapped.getDayTime();
    }

    @Override
    public CompoundTag getLoadedPlayerTag() {
        return this.wrapped.getLoadedPlayerTag();
    }

    @Override
    public String getLevelName() {
        return this.wrapped.getLevelName();
    }

    @Override
    public int getVersion() {
        return this.wrapped.getVersion();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public long getLastPlayed() {
        return this.wrapped.getLastPlayed();
    }

    @Override
    public boolean isThundering() {
        return this.wrapped.isThundering();
    }

    @Override
    public int getThunderTime() {
        return this.wrapped.getThunderTime();
    }

    @Override
    public boolean isRaining() {
        return this.wrapped.isRaining();
    }

    @Override
    public int getRainTime() {
        return this.wrapped.getRainTime();
    }

    @Override
    public GameType getGameType() {
        return this.wrapped.getGameType();
    }

    @Override
    public void setGameTime(long param0) {
    }

    @Override
    public void setDayTime(long param0) {
    }

    @Override
    public void setSpawn(BlockPos param0) {
    }

    @Override
    public void setLevelName(String param0) {
    }

    @Override
    public void setVersion(int param0) {
    }

    @Override
    public void setThundering(boolean param0) {
    }

    @Override
    public void setThunderTime(int param0) {
    }

    @Override
    public void setRaining(boolean param0) {
    }

    @Override
    public void setRainTime(int param0) {
    }

    @Override
    public boolean isGenerateMapFeatures() {
        return this.wrapped.isGenerateMapFeatures();
    }

    @Override
    public boolean isHardcore() {
        return this.wrapped.isHardcore();
    }

    @Override
    public LevelType getGeneratorType() {
        return this.wrapped.getGeneratorType();
    }

    @Override
    public void setGeneratorProvider(ChunkGeneratorProvider param0) {
    }

    @Override
    public boolean getAllowCommands() {
        return this.wrapped.getAllowCommands();
    }

    @Override
    public void setAllowCommands(boolean param0) {
    }

    @Override
    public boolean isInitialized() {
        return this.wrapped.isInitialized();
    }

    @Override
    public void setInitialized(boolean param0) {
    }

    @Override
    public GameRules getGameRules() {
        return this.wrapped.getGameRules();
    }

    @Override
    public Difficulty getDifficulty() {
        return this.wrapped.getDifficulty();
    }

    @Override
    public void setDifficulty(Difficulty param0) {
    }

    @Override
    public boolean isDifficultyLocked() {
        return this.wrapped.isDifficultyLocked();
    }

    @Override
    public void setDifficultyLocked(boolean param0) {
    }

    @Override
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return this.wrapped.getScheduledEvents();
    }

    @Override
    public void setDimensionData(DimensionType param0, CompoundTag param1) {
        this.wrapped.setDimensionData(param0, param1);
    }

    @Override
    public CompoundTag getDimensionData(DimensionType param0) {
        return this.wrapped.getDimensionData(param0);
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory param0) {
        param0.setDetail("Derived", true);
        this.wrapped.fillCrashReportCategory(param0);
    }
}
