package net.minecraft.client;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import net.minecraft.SharedConstants;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class HotbarManager {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int NUM_HOTBAR_GROUPS = 9;
    private final File optionsFile;
    private final DataFixer fixerUpper;
    private final Hotbar[] hotbars = new Hotbar[9];
    private boolean loaded;

    public HotbarManager(File param0, DataFixer param1) {
        this.optionsFile = new File(param0, "hotbar.nbt");
        this.fixerUpper = param1;

        for(int var0 = 0; var0 < 9; ++var0) {
            this.hotbars[var0] = new Hotbar();
        }

    }

    private void load() {
        try {
            CompoundTag var0 = NbtIo.read(this.optionsFile);
            if (var0 == null) {
                return;
            }

            if (!var0.contains("DataVersion", 99)) {
                var0.putInt("DataVersion", 1343);
            }

            var0 = NbtUtils.update(this.fixerUpper, DataFixTypes.HOTBAR, var0, var0.getInt("DataVersion"));

            for(int var1 = 0; var1 < 9; ++var1) {
                this.hotbars[var1].fromTag(var0.getList(String.valueOf(var1), 10));
            }
        } catch (Exception var3) {
            LOGGER.error("Failed to load creative mode options", (Throwable)var3);
        }

    }

    public void save() {
        try {
            CompoundTag var0 = new CompoundTag();
            var0.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

            for(int var1 = 0; var1 < 9; ++var1) {
                var0.put(String.valueOf(var1), this.get(var1).createTag());
            }

            NbtIo.write(var0, this.optionsFile);
        } catch (Exception var3) {
            LOGGER.error("Failed to save creative mode options", (Throwable)var3);
        }

    }

    public Hotbar get(int param0) {
        if (!this.loaded) {
            this.load();
            this.loaded = true;
        }

        return this.hotbars[param0];
    }
}
