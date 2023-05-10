package net.minecraft.client.gui.font.providers;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ProviderReferenceDefinition(ResourceLocation id) implements GlyphProviderDefinition {
    public static final MapCodec<ProviderReferenceDefinition> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(ResourceLocation.CODEC.fieldOf("id").forGetter(ProviderReferenceDefinition::id)).apply(param0, ProviderReferenceDefinition::new)
    );

    @Override
    public GlyphProviderType type() {
        return GlyphProviderType.REFERENCE;
    }

    @Override
    public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
        return Either.right(new GlyphProviderDefinition.Reference(this.id));
    }
}
