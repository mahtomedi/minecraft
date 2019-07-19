package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DimensionDataStorage {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, SavedData> cache = Maps.newHashMap();
    private final DataFixer fixerUpper;
    private final File dataFolder;

    public DimensionDataStorage(File param0, DataFixer param1) {
        this.fixerUpper = param1;
        this.dataFolder = param0;
    }

    private File getDataFile(String param0) {
        return new File(this.dataFolder, param0 + ".dat");
    }

    public <T extends SavedData> T computeIfAbsent(Supplier<T> param0, String param1) {
        T var0 = this.get(param0, param1);
        if (var0 != null) {
            return var0;
        } else {
            T var1 = param0.get();
            this.set(var1);
            return var1;
        }
    }

    @Nullable
    public <T extends SavedData> T get(Supplier<T> param0, String param1) {
        SavedData var0 = this.cache.get(param1);
        if (var0 == null && !this.cache.containsKey(param1)) {
            var0 = this.readSavedData(param0, param1);
            this.cache.put(param1, var0);
        }

        return (T)var0;
    }

    @Nullable
    private <T extends SavedData> T readSavedData(Supplier<T> param0, String param1) {
        try {
            File var0 = this.getDataFile(param1);
            if (var0.exists()) {
                T var1 = param0.get();
                CompoundTag var2 = this.readTagFromDisk(param1, SharedConstants.getCurrentVersion().getWorldVersion());
                var1.load(var2.getCompound("data"));
                return var1;
            }
        } catch (Exception var6) {
            LOGGER.error("Error loading saved data: {}", param1, var6);
        }

        return null;
    }

    public void set(SavedData param0) {
        this.cache.put(param0.getId(), param0);
    }

    public CompoundTag readTagFromDisk(String param0, int param1) throws IOException {
        File var0 = this.getDataFile(param0);

        CompoundTag var36;
        try (PushbackInputStream var1 = new PushbackInputStream(new FileInputStream(var0), 2)) {
            CompoundTag var2;
            if (this.isGzip(var1)) {
                var2 = NbtIo.readCompressed(var1);
            } else {
                try (DataInputStream var3 = new DataInputStream(var1)) {
                    var2 = NbtIo.read(var3);
                }
            }

            int var6 = var2.contains("DataVersion", 99) ? var2.getInt("DataVersion") : 1343;
            var36 = NbtUtils.update(this.fixerUpper, DataFixTypes.SAVED_DATA, var2, var6, param1);
        }

        return var36;
    }

    private boolean isGzip(PushbackInputStream param0) throws IOException {
        byte[] var0 = new byte[2];
        boolean var1 = false;
        int var2 = param0.read(var0, 0, 2);
        if (var2 == 2) {
            int var3 = (var0[1] & 255) << 8 | var0[0] & 255;
            if (var3 == 35615) {
                var1 = true;
            }
        }

        if (var2 != 0) {
            param0.unread(var0, 0, var2);
        }

        return var1;
    }

    public void save() {
        for(SavedData var0 : this.cache.values()) {
            if (var0 != null) {
                var0.save(this.getDataFile(var0.getId()));
            }
        }

    }
}
