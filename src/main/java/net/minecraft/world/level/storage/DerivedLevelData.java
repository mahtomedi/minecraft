package net.minecraft.world.level.storage;

import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.timers.TimerQueue;

public class DerivedLevelData implements ServerLevelData {
    private final DimensionType dimensionType;
    private final WorldData worldData;
    private final ServerLevelData wrapped;

    public DerivedLevelData(DimensionType param0, WorldData param1, ServerLevelData param2) {
        this.dimensionType = param0;
        this.worldData = param1;
        this.wrapped = param2;
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
    public String getLevelName() {
        return this.worldData.getLevelName();
    }

    @Override
    public int getClearWeatherTime() {
        return this.wrapped.getClearWeatherTime();
    }

    @Override
    public void setClearWeatherTime(int param0) {
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
        return this.worldData.getGameType();
    }

    @Override
    public void setXSpawn(int param0) {
    }

    @Override
    public void setYSpawn(int param0) {
    }

    @Override
    public void setZSpawn(int param0) {
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
    public void setGameType(GameType param0) {
    }

    @Override
    public boolean isHardcore() {
        return this.worldData.isHardcore();
    }

    @Override
    public boolean getAllowCommands() {
        return this.worldData.getAllowCommands();
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
        return this.worldData.getGameRules();
    }

    @Override
    public WorldBorder.Settings getWorldBorder() {
        return this.wrapped.getWorldBorder();
    }

    @Override
    public void setWorldBorder(WorldBorder.Settings param0) {
    }

    @Override
    public Difficulty getDifficulty() {
        return this.worldData.getDifficulty();
    }

    @Override
    public boolean isDifficultyLocked() {
        return this.worldData.isDifficultyLocked();
    }

    @Override
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return this.wrapped.getScheduledEvents();
    }

    @Override
    public void setDimensionData(CompoundTag param0) {
        this.worldData.setDimensionData(this.dimensionType, param0);
    }

    @Override
    public CompoundTag getDimensionData() {
        return this.worldData.getDimensionData(this.dimensionType);
    }

    @Override
    public int getWanderingTraderSpawnDelay() {
        return 0;
    }

    @Override
    public void setWanderingTraderSpawnDelay(int param0) {
    }

    @Override
    public int getWanderingTraderSpawnChance() {
        return 0;
    }

    @Override
    public void setWanderingTraderSpawnChance(int param0) {
    }

    @Override
    public void setWanderingTraderId(UUID param0) {
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory param0) {
        param0.setDetail("Derived", true);
        this.wrapped.fillCrashReportCategory(param0);
    }
}
