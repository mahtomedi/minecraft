package net.minecraft.client.resources.metadata.language;

import java.util.Collection;
import net.minecraft.client.resources.language.Language;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageMetadataSection {
    public static final LanguageMetadataSectionSerializer SERIALIZER = new LanguageMetadataSectionSerializer();
    private final Collection<Language> languages;

    public LanguageMetadataSection(Collection<Language> param0) {
        this.languages = param0;
    }

    public Collection<Language> getLanguages() {
        return this.languages;
    }
}
