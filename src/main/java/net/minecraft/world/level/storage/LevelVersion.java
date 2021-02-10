package net.minecraft.world.level.storage;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.SharedConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LevelVersion {
    private final int levelDataVersion;
    private final long lastPlayed;
    private final String minecraftVersionName;
    private final int minecraftVersion;
    private final boolean snapshot;

    public LevelVersion(int param0, long param1, String param2, int param3, boolean param4) {
        this.levelDataVersion = param0;
        this.lastPlayed = param1;
        this.minecraftVersionName = param2;
        this.minecraftVersion = param3;
        this.snapshot = param4;
    }

    public static LevelVersion parse(Dynamic<?> param0) {
        int var0 = param0.get("version").asInt(0);
        long var1 = param0.get("LastPlayed").asLong(0L);
        OptionalDynamic<?> var2 = param0.get("Version");
        return var2.result().isPresent()
            ? new LevelVersion(
                var0,
                var1,
                var2.get("Name").asString(SharedConstants.getCurrentVersion().getName()),
                var2.get("Id").asInt(SharedConstants.getCurrentVersion().getWorldVersion()),
                var2.get("Snapshot").asBoolean(!SharedConstants.getCurrentVersion().isStable())
            )
            : new LevelVersion(var0, var1, "", 0, false);
    }

    public int levelDataVersion() {
        return this.levelDataVersion;
    }

    public long lastPlayed() {
        return this.lastPlayed;
    }

    @OnlyIn(Dist.CLIENT)
    public String minecraftVersionName() {
        return this.minecraftVersionName;
    }

    public int minecraftVersion() {
        return this.minecraftVersion;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean snapshot() {
        return this.snapshot;
    }
}
