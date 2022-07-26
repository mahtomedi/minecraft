package net.minecraft.world.level.saveddata;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
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
            var0.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

            try {
                NbtIo.writeCompressed(var0, param0);
            } catch (IOException var4) {
                LOGGER.error("Could not save data {}", this, var4);
            }

            this.setDirty(false);
        }
    }
}
