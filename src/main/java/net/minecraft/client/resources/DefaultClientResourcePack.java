package net.minecraft.client.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DefaultClientResourcePack extends VanillaPack {
    private final AssetIndex assetIndex;

    public DefaultClientResourcePack(AssetIndex param0) {
        super("minecraft", "realms", "nothingtoseeheremovealong");
        this.assetIndex = param0;
    }

    @Nullable
    @Override
    protected InputStream getResourceAsStream(PackType param0, ResourceLocation param1) {
        if (param0 == PackType.CLIENT_RESOURCES) {
            File var0 = this.assetIndex.getFile(param1);
            if (var0 != null && var0.exists()) {
                try {
                    return new FileInputStream(var0);
                } catch (FileNotFoundException var5) {
                }
            }
        }

        return super.getResourceAsStream(param0, param1);
    }

    @Override
    public boolean hasResource(PackType param0, ResourceLocation param1) {
        if (param0 == PackType.CLIENT_RESOURCES) {
            File var0 = this.assetIndex.getFile(param1);
            if (var0 != null && var0.exists()) {
                return true;
            }
        }

        return super.hasResource(param0, param1);
    }

    @Nullable
    @Override
    protected InputStream getResourceAsStream(String param0) {
        File var0 = this.assetIndex.getRootFile(param0);
        if (var0 != null && var0.exists()) {
            try {
                return new FileInputStream(var0);
            } catch (FileNotFoundException var4) {
            }
        }

        return super.getResourceAsStream(param0);
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType param0, String param1, String param2, int param3, Predicate<String> param4) {
        Collection<ResourceLocation> var0 = super.getResources(param0, param1, param2, param3, param4);
        var0.addAll(this.assetIndex.getFiles(param2, param1, param3, param4));
        return var0;
    }
}
