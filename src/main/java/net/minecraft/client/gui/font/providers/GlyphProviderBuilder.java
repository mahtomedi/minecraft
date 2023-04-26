package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GlyphProviderBuilder {
    Either<GlyphProviderBuilder.Loader, GlyphProviderBuilder.Reference> build();

    @OnlyIn(Dist.CLIENT)
    public interface Loader {
        GlyphProvider load(ResourceManager var1) throws IOException;
    }

    @OnlyIn(Dist.CLIENT)
    public static record Reference(ResourceLocation id) {
    }
}
