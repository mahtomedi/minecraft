package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class DimensionDataStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
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

    public <T extends SavedData> T computeIfAbsent(SavedData.Factory<T> param0, String param1) {
        T var0 = this.get(param0, param1);
        if (var0 != null) {
            return var0;
        } else {
            T var1 = param0.constructor().get();
            this.set(param1, var1);
            return var1;
        }
    }

    @Nullable
    public <T extends SavedData> T get(SavedData.Factory param0, String param1) {
        SavedData var0 = this.cache.get(param1);
        if (var0 == null && !this.cache.containsKey(param1)) {
            var0 = this.readSavedData(param0.deserializer(), param0.type(), param1);
            this.cache.put(param1, var0);
        }

        return (T)var0;
    }

    @Nullable
    private <T extends SavedData> T readSavedData(Function<CompoundTag, T> param0, DataFixTypes param1, String param2) {
        try {
            File var0 = this.getDataFile(param2);
            if (var0.exists()) {
                CompoundTag var1 = this.readTagFromDisk(param2, param1, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                return param0.apply(var1.getCompound("data"));
            }
        } catch (Exception var6) {
            LOGGER.error("Error loading saved data: {}", param2, var6);
        }

        return null;
    }

    public void set(String param0, SavedData param1) {
        this.cache.put(param0, param1);
    }

    public CompoundTag readTagFromDisk(String param0, DataFixTypes param1, int param2) throws IOException {
        File var0 = this.getDataFile(param0);

        CompoundTag var9;
        try (
            FileInputStream var1 = new FileInputStream(var0);
            PushbackInputStream var2 = new PushbackInputStream(var1, 2);
        ) {
            CompoundTag var3;
            if (this.isGzip(var2)) {
                var3 = NbtIo.readCompressed(var2);
            } else {
                try (DataInputStream var4 = new DataInputStream(var2)) {
                    var3 = NbtIo.read(var4);
                }
            }

            int var7 = NbtUtils.getDataVersion(var3, 1343);
            var9 = param1.update(this.fixerUpper, var3, var7, param2);
        }

        return var9;
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
        this.cache.forEach((param0, param1) -> {
            if (param1 != null) {
                param1.save(this.getDataFile(param0));
            }

        });
    }
}
