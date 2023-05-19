package com.mojang.blaze3d.platform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

@OnlyIn(Dist.CLIENT)
public enum IconSet {
    RELEASE("icons"),
    SNAPSHOT("icons", "snapshot");

    private final String[] path;

    private IconSet(String... param0) {
        this.path = param0;
    }

    public List<IoSupplier<InputStream>> getStandardIcons(PackResources param0) throws IOException {
        return List.of(
            this.getFile(param0, "icon_16x16.png"),
            this.getFile(param0, "icon_32x32.png"),
            this.getFile(param0, "icon_48x48.png"),
            this.getFile(param0, "icon_128x128.png"),
            this.getFile(param0, "icon_256x256.png")
        );
    }

    public IoSupplier<InputStream> getMacIcon(PackResources param0) throws IOException {
        return this.getFile(param0, "minecraft.icns");
    }

    private IoSupplier<InputStream> getFile(PackResources param0, String param1) throws IOException {
        String[] var0 = ArrayUtils.add(this.path, param1);
        IoSupplier<InputStream> var1 = param0.getRootResource(var0);
        if (var1 == null) {
            throw new FileNotFoundException(String.join("/", var0));
        } else {
            return var1;
        }
    }
}
