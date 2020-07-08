package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerDataStorage {
    private static final Logger LOGGER = LogManager.getLogger();
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
            File var1 = File.createTempFile(param0.getStringUUID() + "-", ".dat", this.playerDir);
            NbtIo.writeCompressed(var0, var1);
            File var2 = new File(this.playerDir, param0.getStringUUID() + ".dat");
            File var3 = new File(this.playerDir, param0.getStringUUID() + ".dat_old");
            Util.safeReplaceFile(var2, var1, var3);
        } catch (Exception var6) {
            LOGGER.warn("Failed to save player data for {}", param0.getName().getString());
        }

    }

    @Nullable
    public CompoundTag load(Player param0) {
        CompoundTag var0 = null;

        try {
            File var1 = new File(this.playerDir, param0.getStringUUID() + ".dat");
            if (var1.exists() && var1.isFile()) {
                var0 = NbtIo.readCompressed(var1);
            }
        } catch (Exception var4) {
            LOGGER.warn("Failed to load player data for {}", param0.getName().getString());
        }

        if (var0 != null) {
            int var3 = var0.contains("DataVersion", 3) ? var0.getInt("DataVersion") : -1;
            param0.load(NbtUtils.update(this.fixerUpper, DataFixTypes.PLAYER, var0, var3));
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
