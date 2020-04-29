package net.minecraft.server.level;

import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;

public class DerivedServerLevel extends ServerLevel {
    public DerivedServerLevel(
        ServerLevel param0,
        ServerLevelData param1,
        MinecraftServer param2,
        Executor param3,
        LevelStorageSource.LevelStorageAccess param4,
        DimensionType param5,
        ChunkProgressListener param6
    ) {
        super(param2, param3, param4, new DerivedLevelData(param5, param2.getWorldData(), param1), param5, param6);
        param0.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(this.getWorldBorder()));
    }

    @Override
    protected void tickTime() {
    }
}
