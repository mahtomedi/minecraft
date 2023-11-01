package net.minecraft.client;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class HotbarManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int NUM_HOTBAR_GROUPS = 9;
    private final Path optionsFile;
    private final DataFixer fixerUpper;
    private final Hotbar[] hotbars = new Hotbar[9];
    private boolean loaded;

    public HotbarManager(Path param0, DataFixer param1) {
        this.optionsFile = param0.resolve("hotbar.nbt");
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

            int var1 = NbtUtils.getDataVersion(var0, 1343);
            var0 = DataFixTypes.HOTBAR.updateToCurrentVersion(this.fixerUpper, var0, var1);

            for(int var2 = 0; var2 < 9; ++var2) {
                this.hotbars[var2].fromTag(var0.getList(String.valueOf(var2), 10));
            }
        } catch (Exception var4) {
            LOGGER.error("Failed to load creative mode options", (Throwable)var4);
        }

    }

    public void save() {
        try {
            CompoundTag var0 = NbtUtils.addCurrentDataVersion(new CompoundTag());

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
