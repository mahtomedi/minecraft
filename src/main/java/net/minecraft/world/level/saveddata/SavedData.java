package net.minecraft.world.level.saveddata;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import org.slf4j.Logger;

public abstract class SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean dirty;

    public abstract CompoundTag save(CompoundTag var1);

    public void setDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean param0) {
        this.dirty = param0;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void save(File param0) {
        if (this.isDirty()) {
            CompoundTag var0 = new CompoundTag();
            var0.put("data", this.save(new CompoundTag()));
            NbtUtils.addCurrentDataVersion(var0);

            try {
                NbtIo.writeCompressed(var0, param0.toPath());
            } catch (IOException var4) {
                LOGGER.error("Could not save data {}", this, var4);
            }

            this.setDirty(false);
        }
    }

    public static record Factory<T extends SavedData>(Supplier<T> constructor, Function<CompoundTag, T> deserializer, DataFixTypes type) {
    }
}
