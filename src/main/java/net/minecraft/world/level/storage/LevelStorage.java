package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LevelStorage implements PlayerIO {
    private static final Logger LOGGER = LogManager.getLogger();
    private final File worldDir;
    private final File playerDir;
    private final String levelId;
    private final StructureManager structureManager;
    protected final DataFixer fixerUpper;

    public LevelStorage(File param0, String param1, @Nullable MinecraftServer param2, DataFixer param3) {
        this.fixerUpper = param3;
        this.worldDir = new File(param0, param1);
        this.worldDir.mkdirs();
        this.playerDir = new File(this.worldDir, "playerdata");
        this.levelId = param1;
        if (param2 != null) {
            this.playerDir.mkdirs();
            this.structureManager = new StructureManager(param2, this.worldDir, param3);
        } else {
            this.structureManager = null;
        }

    }

    public void saveLevelData(LevelData param0, @Nullable CompoundTag param1) {
        param0.setVersion(19133);
        CompoundTag var0 = param0.createTag(param1);
        CompoundTag var1 = new CompoundTag();
        var1.put("Data", var0);

        try {
            File var2 = new File(this.worldDir, "level.dat_new");
            File var3 = new File(this.worldDir, "level.dat_old");
            File var4 = new File(this.worldDir, "level.dat");
            NbtIo.writeCompressed(var1, new FileOutputStream(var2));
            if (var3.exists()) {
                var3.delete();
            }

            var4.renameTo(var3);
            if (var4.exists()) {
                var4.delete();
            }

            var2.renameTo(var4);
            if (var2.exists()) {
                var2.delete();
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    public File getFolder() {
        return this.worldDir;
    }

    @Nullable
    public LevelData prepareLevel() {
        File var0 = new File(this.worldDir, "level.dat");
        if (var0.exists()) {
            LevelData var1 = LevelStorageSource.getLevelData(var0, this.fixerUpper);
            if (var1 != null) {
                return var1;
            }
        }

        var0 = new File(this.worldDir, "level.dat_old");
        return var0.exists() ? LevelStorageSource.getLevelData(var0, this.fixerUpper) : null;
    }

    public void saveLevelData(LevelData param0) {
        this.saveLevelData(param0, null);
    }

    @Override
    public void save(Player param0) {
        try {
            CompoundTag var0 = param0.saveWithoutId(new CompoundTag());
            File var1 = new File(this.playerDir, param0.getStringUUID() + ".dat.tmp");
            File var2 = new File(this.playerDir, param0.getStringUUID() + ".dat");
            NbtIo.writeCompressed(var0, new FileOutputStream(var1));
            if (var2.exists()) {
                var2.delete();
            }

            var1.renameTo(var2);
        } catch (Exception var5) {
            LOGGER.warn("Failed to save player data for {}", param0.getName().getString());
        }

    }

    @Nullable
    @Override
    public CompoundTag load(Player param0) {
        CompoundTag var0 = null;

        try {
            File var1 = new File(this.playerDir, param0.getStringUUID() + ".dat");
            if (var1.exists() && var1.isFile()) {
                var0 = NbtIo.readCompressed(new FileInputStream(var1));
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

    public StructureManager getStructureManager() {
        return this.structureManager;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }
}
