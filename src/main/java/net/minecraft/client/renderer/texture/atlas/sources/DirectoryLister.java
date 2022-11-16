package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DirectoryLister implements SpriteSource {
    public static final Codec<DirectoryLister> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.STRING.fieldOf("source").forGetter(param0x -> param0x.sourcePath),
                    Codec.STRING.fieldOf("prefix").forGetter(param0x -> param0x.idPrefix)
                )
                .apply(param0, DirectoryLister::new)
    );
    private final String sourcePath;
    private final String idPrefix;

    public DirectoryLister(String param0, String param1) {
        this.sourcePath = param0;
        this.idPrefix = param1;
    }

    @Override
    public void run(ResourceManager param0, SpriteSource.Output param1) {
        FileToIdConverter var0 = new FileToIdConverter("textures/" + this.sourcePath, ".png");
        var0.listMatchingResources(param0).forEach((param2, param3) -> {
            ResourceLocation var0x = var0.fileToId(param2).withPrefix(this.idPrefix);
            param1.add(var0x, param3);
        });
    }

    @Override
    public SpriteSourceType type() {
        return SpriteSources.DIRECTORY;
    }
}
