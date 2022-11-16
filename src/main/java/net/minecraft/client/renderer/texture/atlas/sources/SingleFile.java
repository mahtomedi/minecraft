package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SingleFile implements SpriteSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<SingleFile> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ResourceLocation.CODEC.fieldOf("resource").forGetter(param0x -> param0x.resourceId),
                    ResourceLocation.CODEC.optionalFieldOf("sprite").forGetter(param0x -> param0x.spriteId)
                )
                .apply(param0, SingleFile::new)
    );
    private final FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");
    private final ResourceLocation resourceId;
    private final Optional<ResourceLocation> spriteId;

    public SingleFile(ResourceLocation param0, Optional<ResourceLocation> param1) {
        this.resourceId = param0;
        this.spriteId = param1;
    }

    @Override
    public void run(ResourceManager param0, SpriteSource.Output param1) {
        ResourceLocation var0 = this.TEXTURE_ID_CONVERTER.idToFile(this.resourceId);
        Optional<Resource> var1 = param0.getResource(var0);
        if (var1.isPresent()) {
            param1.add(this.spriteId.orElse(this.resourceId), var1.get());
        } else {
            LOGGER.warn("Missing sprite: {}", var0);
        }

    }

    @Override
    public SpriteSourceType type() {
        return SpriteSources.SINGLE_FILE;
    }
}
