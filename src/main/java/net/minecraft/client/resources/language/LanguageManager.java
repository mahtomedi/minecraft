package net.minecraft.client.resources.language;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LanguageManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    protected static final Locale LOCALE = new Locale();
    private String currentCode;
    private final Map<String, Language> languages = Maps.newHashMap();

    public LanguageManager(String param0) {
        this.currentCode = param0;
        I18n.setLocale(LOCALE);
    }

    public void reload(List<Pack> param0) {
        this.languages.clear();

        for(Pack var0 : param0) {
            try {
                LanguageMetadataSection var1 = var0.getMetadataSection(LanguageMetadataSection.SERIALIZER);
                if (var1 != null) {
                    for(Language var2 : var1.getLanguages()) {
                        if (!this.languages.containsKey(var2.getCode())) {
                            this.languages.put(var2.getCode(), var2);
                        }
                    }
                }
            } catch (IOException | RuntimeException var7) {
                LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", var0.getName(), var7);
            }
        }

    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        List<String> var0 = Lists.newArrayList("en_us");
        if (!"en_us".equals(this.currentCode)) {
            var0.add(this.currentCode);
        }

        LOCALE.loadFrom(param0, var0);
        net.minecraft.locale.Language.forceData(LOCALE.storage);
    }

    public boolean isBidirectional() {
        return this.getSelected() != null && this.getSelected().isBidirectional();
    }

    public void setSelected(Language param0) {
        this.currentCode = param0.getCode();
    }

    public Language getSelected() {
        String var0 = this.languages.containsKey(this.currentCode) ? this.currentCode : "en_us";
        return this.languages.get(var0);
    }

    public SortedSet<Language> getLanguages() {
        return Sets.newTreeSet(this.languages.values());
    }

    public Language getLanguage(String param0) {
        return this.languages.get(param0);
    }
}
