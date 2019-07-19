package net.minecraft.server.level;

import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorage;

public class DerivedServerLevel extends ServerLevel {
    public DerivedServerLevel(
        ServerLevel param0,
        MinecraftServer param1,
        Executor param2,
        LevelStorage param3,
        DimensionType param4,
        ProfilerFiller param5,
        ChunkProgressListener param6
    ) {
        super(param1, param2, param3, new DerivedLevelData(param0.getLevelData()), param4, param5, param6);
        param0.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(this.getWorldBorder()));
    }

    @Override
    protected void tickTime() {
    }
}
