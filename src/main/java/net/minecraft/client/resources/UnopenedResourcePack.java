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
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.UnopenedPack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UnopenedResourcePack extends UnopenedPack {
    @Nullable
    private NativeImage icon;
    @Nullable
    private ResourceLocation iconLocation;

    public UnopenedResourcePack(String param0, boolean param1, Supplier<Pack> param2, Pack param3, PackMetadataSection param4, UnopenedPack.Position param5) {
        super(param0, param1, param2, param3, param4, param5);
        NativeImage var0 = null;

        try (InputStream var1 = param3.getRootResource("pack.png")) {
            var0 = NativeImage.read(var1);
        } catch (IllegalArgumentException | IOException var21) {
        }

        this.icon = var0;
    }

    public UnopenedResourcePack(
        String param0,
        boolean param1,
        Supplier<Pack> param2,
        Component param3,
        Component param4,
        PackCompatibility param5,
        UnopenedPack.Position param6,
        boolean param7,
        @Nullable NativeImage param8
    ) {
        super(param0, param1, param2, param3, param4, param5, param6, param7);
        this.icon = param8;
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
