package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Stream;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LanguageManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_LANGUAGE_CODE = "en_us";
    private static final LanguageInfo DEFAULT_LANGUAGE = new LanguageInfo("en_us", "US", "English", false);
    private Map<String, LanguageInfo> languages = ImmutableMap.of("en_us", DEFAULT_LANGUAGE);
    private String currentCode;
    private LanguageInfo currentLanguage = DEFAULT_LANGUAGE;

    public LanguageManager(String param0) {
        this.currentCode = param0;
    }

    private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> param0) {
        Map<String, LanguageInfo> var0 = Maps.newHashMap();
        param0.forEach(param1 -> {
            try {
                LanguageMetadataSection var0x = param1.getMetadataSection(LanguageMetadataSection.SERIALIZER);
                if (var0x != null) {
                    for(LanguageInfo var1x : var0x.getLanguages()) {
                        var0.putIfAbsent(var1x.getCode(), var1x);
                    }
                }
            } catch (IOException | RuntimeException var5) {
                LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", param1.getName(), var5);
            }

        });
        return ImmutableMap.copyOf(var0);
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.languages = extractLanguages(param0.listPacks());
        LanguageInfo var0 = this.languages.getOrDefault("en_us", DEFAULT_LANGUAGE);
        this.currentLanguage = this.languages.getOrDefault(this.currentCode, var0);
        List<LanguageInfo> var1 = Lists.newArrayList(var0);
        if (this.currentLanguage != var0) {
            var1.add(this.currentLanguage);
        }

        ClientLanguage var2 = ClientLanguage.loadFrom(param0, var1);
        I18n.setLanguage(var2);
        Language.inject(var2);
    }

    public void setSelected(LanguageInfo param0) {
        this.currentCode = param0.getCode();
        this.currentLanguage = param0;
    }

    public LanguageInfo getSelected() {
        return this.currentLanguage;
    }

    public SortedSet<LanguageInfo> getLanguages() {
        return Sets.newTreeSet(this.languages.values());
    }

    public LanguageInfo getLanguage(String param0) {
        return this.languages.get(param0);
    }
}
