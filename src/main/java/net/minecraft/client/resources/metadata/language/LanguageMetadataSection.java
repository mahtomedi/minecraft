package net.minecraft.client.resources.metadata.language;

import java.util.Collection;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageMetadataSection {
    public static final LanguageMetadataSectionSerializer SERIALIZER = new LanguageMetadataSectionSerializer();
    public static final boolean DEFAULT_BIDIRECTIONAL = false;
    private final Collection<LanguageInfo> languages;

    public LanguageMetadataSection(Collection<LanguageInfo> param0) {
        this.languages = param0;
    }

    public Collection<LanguageInfo> getLanguages() {
        return this.languages;
    }
}
