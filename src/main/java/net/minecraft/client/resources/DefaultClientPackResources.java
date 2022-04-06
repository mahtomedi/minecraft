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
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DefaultClientPackResources extends VanillaPackResources {
    private final AssetIndex assetIndex;

    public DefaultClientPackResources(PackMetadataSection param0, AssetIndex param1) {
        super(param0, "minecraft", "realms");
        this.assetIndex = param1;
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
    public Collection<ResourceLocation> getResources(PackType param0, String param1, String param2, Predicate<ResourceLocation> param3) {
        Collection<ResourceLocation> var0 = super.getResources(param0, param1, param2, param3);
        var0.addAll(this.assetIndex.getFiles(param2, param1, param3));
        return var0;
    }
}
