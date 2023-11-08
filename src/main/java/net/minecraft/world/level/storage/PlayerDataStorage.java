package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class PlayerDataStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final File playerDir;
    protected final DataFixer fixerUpper;

    public PlayerDataStorage(LevelStorageSource.LevelStorageAccess param0, DataFixer param1) {
        this.fixerUpper = param1;
        this.playerDir = param0.getLevelPath(LevelResource.PLAYER_DATA_DIR).toFile();
        this.playerDir.mkdirs();
    }

    public void save(Player param0) {
        try {
            CompoundTag var0 = param0.saveWithoutId(new CompoundTag());
            Path var1 = this.playerDir.toPath();
            Path var2 = Files.createTempFile(var1, param0.getStringUUID() + "-", ".dat");
            NbtIo.writeCompressed(var0, var2);
            Path var3 = var1.resolve(param0.getStringUUID() + ".dat");
            Path var4 = var1.resolve(param0.getStringUUID() + ".dat_old");
            Util.safeReplaceFile(var3, var2, var4);
        } catch (Exception var7) {
            LOGGER.warn("Failed to save player data for {}", param0.getName().getString());
        }

    }

    @Nullable
    public CompoundTag load(Player param0) {
        CompoundTag var0 = null;

        try {
            File var1 = new File(this.playerDir, param0.getStringUUID() + ".dat");
            if (var1.exists() && var1.isFile()) {
                var0 = NbtIo.readCompressed(var1.toPath(), NbtAccounter.unlimitedHeap());
            }
        } catch (Exception var4) {
            LOGGER.warn("Failed to load player data for {}", param0.getName().getString());
        }

        if (var0 != null) {
            int var3 = NbtUtils.getDataVersion(var0, -1);
            var0 = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, var0, var3);
            param0.load(var0);
        }

        return var0;
    }

    public String[] getSeenPlayers() {
        String[] var0 = this.playerDir.list();
        if (var0 == null) {
            var0 = new String[0];
        }

        for(int var1 = 0; var1 < var0.length; ++var1) {
            if (var0[var1].endsWith(".dat")) {
                var0[var1] = var0[var1].substring(0, var0[var1].length() - 4);
            }
        }

        return var0;
    }
}
