package net.minecraft.client.renderer.texture.atlas;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SpriteSource {
    FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");

    void run(ResourceManager var1, SpriteSource.Output var2);

    SpriteSourceType type();

    @OnlyIn(Dist.CLIENT)
    public interface Output {
        default void add(ResourceLocation param0, Resource param1) {
            this.add(param0, () -> SpriteLoader.loadSprite(param0, param1));
        }

        void add(ResourceLocation var1, SpriteSource.SpriteSupplier var2);

        void removeAll(Predicate<ResourceLocation> var1);
    }

    @OnlyIn(Dist.CLIENT)
    public interface SpriteSupplier extends Supplier<SpriteContents> {
        default void discard() {
        }
    }
}
