package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResourcePack extends Pack {
    @Nullable
    private NativeImage icon;
    @Nullable
    private ResourceLocation iconLocation;

    public ResourcePack(
        String param0,
        boolean param1,
        Supplier<PackResources> param2,
        PackResources param3,
        PackMetadataSection param4,
        Pack.Position param5,
        PackSource param6
    ) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.icon = readIcon(param3);
    }

    public ResourcePack(
        String param0,
        boolean param1,
        Supplier<PackResources> param2,
        Component param3,
        Component param4,
        PackCompatibility param5,
        Pack.Position param6,
        boolean param7,
        PackSource param8,
        @Nullable NativeImage param9
    ) {
        super(param0, param1, param2, param3, param4, param5, param6, param7, param8);
        this.icon = param9;
    }

    @Nullable
    public static NativeImage readIcon(PackResources param0) {
        try (InputStream var0 = param0.getRootResource("pack.png")) {
            return NativeImage.read(var0);
        } catch (IllegalArgumentException | IOException var15) {
            return null;
        }
    }

    public void bindIcon(TextureManager param0) {
        if (this.iconLocation == null) {
            if (this.icon == null) {
                this.iconLocation = new ResourceLocation("textures/misc/unknown_pack.png");
            } else {
                this.iconLocation = param0.register("texturepackicon", new DynamicTexture(this.icon));
            }
        }

        param0.bind(this.iconLocation);
    }

    @Override
    public void close() {
        super.close();
        if (this.icon != null) {
            this.icon.close();
            this.icon = null;
        }

    }
}
