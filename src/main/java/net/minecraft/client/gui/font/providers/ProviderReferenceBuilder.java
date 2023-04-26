package net.minecraft.client.gui.font.providers;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProviderReferenceBuilder implements GlyphProviderBuilder {
    public static final Codec<ProviderReferenceBuilder> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(ResourceLocation.CODEC.fieldOf("id").forGetter(param0x -> param0x.id)).apply(param0, ProviderReferenceBuilder::new)
    );
    private final ResourceLocation id;

    private ProviderReferenceBuilder(ResourceLocation param0) {
        this.id = param0;
    }

    public static GlyphProviderBuilder fromJson(JsonObject param0) {
        return CODEC.parse(JsonOps.INSTANCE, param0).getOrThrow(false, param0x -> {
        });
    }

    @Override
    public Either<GlyphProviderBuilder.Loader, GlyphProviderBuilder.Reference> build() {
        return Either.right(new GlyphProviderBuilder.Reference(this.id));
    }
}
