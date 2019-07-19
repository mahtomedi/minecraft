package net.minecraft.world.level.saveddata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SavedData {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String id;
    private boolean dirty;

    public SavedData(String param0) {
        this.id = param0;
    }

    public abstract void load(CompoundTag var1);

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

    public String getId() {
        return this.id;
    }

    public void save(File param0) {
        if (this.isDirty()) {
            CompoundTag var0 = new CompoundTag();
            var0.put("data", this.save(new CompoundTag()));
            var0.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

            try (FileOutputStream var1 = new FileOutputStream(param0)) {
                NbtIo.writeCompressed(var0, var1);
            } catch (IOException var16) {
                LOGGER.error("Could not save data {}", this, var16);
            }

            this.setDirty(false);
        }
    }
}
