package net.minecraft.client.resources.language;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record LanguageInfo(String region, String name, boolean bidirectional) {
    public static final Codec<LanguageInfo> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.NON_EMPTY_STRING.fieldOf("region").forGetter(LanguageInfo::region),
                    ExtraCodecs.NON_EMPTY_STRING.fieldOf("name").forGetter(LanguageInfo::name),
                    Codec.BOOL.optionalFieldOf("bidirectional", Boolean.valueOf(false)).forGetter(LanguageInfo::bidirectional)
                )
                .apply(param0, LanguageInfo::new)
    );

    public Component toComponent() {
        return Component.literal(this.name + " (" + this.region + ")");
    }
}
