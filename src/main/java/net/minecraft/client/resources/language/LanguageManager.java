package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import javax.annotation.Nullable;
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
    private static final LanguageInfo DEFAULT_LANGUAGE = new LanguageInfo("US", "English", false);
    private Map<String, LanguageInfo> languages = ImmutableMap.of("en_us", DEFAULT_LANGUAGE);
    private String currentCode;

    public LanguageManager(String param0) {
        this.currentCode = param0;
    }

    private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> param0) {
        Map<String, LanguageInfo> var0 = Maps.newHashMap();
        param0.forEach(param1 -> {
            try {
                LanguageMetadataSection var1x = param1.getMetadataSection(LanguageMetadataSection.TYPE);
                if (var1x != null) {
                    var1x.languages().forEach(var0::putIfAbsent);
                }
            } catch (IOException | RuntimeException var3) {
                LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", param1.packId(), var3);
            }

        });
        return ImmutableMap.copyOf(var0);
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.languages = extractLanguages(param0.listPacks());
        List<String> var0 = new ArrayList<>(2);
        boolean var1 = DEFAULT_LANGUAGE.bidirectional();
        var0.add("en_us");
        if (!this.currentCode.equals("en_us")) {
            LanguageInfo var2 = this.languages.get(this.currentCode);
            if (var2 != null) {
                var0.add(this.currentCode);
                var1 = var2.bidirectional();
            }
        }

        ClientLanguage var3 = ClientLanguage.loadFrom(param0, var0, var1);
        I18n.setLanguage(var3);
        Language.inject(var3);
    }

    public void setSelected(String param0) {
        this.currentCode = param0;
    }

    public String getSelected() {
        return this.currentCode;
    }

    public SortedMap<String, LanguageInfo> getLanguages() {
        return new TreeMap<>(this.languages);
    }

    @Nullable
    public LanguageInfo getLanguage(String param0) {
        return this.languages.get(param0);
    }
}
