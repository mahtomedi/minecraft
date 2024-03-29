package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientLanguage extends Language {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, String> storage;
    private final boolean defaultRightToLeft;

    private ClientLanguage(Map<String, String> param0, boolean param1) {
        this.storage = param0;
        this.defaultRightToLeft = param1;
    }

    public static ClientLanguage loadFrom(ResourceManager param0, List<String> param1, boolean param2) {
        Map<String, String> var0 = Maps.newHashMap();

        for(String var1 : param1) {
            String var2 = String.format(Locale.ROOT, "lang/%s.json", var1);

            for(String var3 : param0.getNamespaces()) {
                try {
                    ResourceLocation var4 = new ResourceLocation(var3, var2);
                    appendFrom(var1, param0.getResourceStack(var4), var0);
                } catch (Exception var10) {
                    LOGGER.warn("Skipped language file: {}:{} ({})", var3, var2, var10.toString());
                }
            }
        }

        return new ClientLanguage(ImmutableMap.copyOf(var0), param2);
    }

    private static void appendFrom(String param0, List<Resource> param1, Map<String, String> param2) {
        for(Resource var0 : param1) {
            try (InputStream var1 = var0.open()) {
                Language.loadFromJson(var1, param2::put);
            } catch (IOException var10) {
                LOGGER.warn("Failed to load translations for {} from pack {}", param0, var0.sourcePackId(), var10);
            }
        }

    }

    @Override
    public String getOrDefault(String param0, String param1) {
        return this.storage.getOrDefault(param0, param1);
    }

    @Override
    public boolean has(String param0) {
        return this.storage.containsKey(param0);
    }

    @Override
    public boolean isDefaultRightToLeft() {
        return this.defaultRightToLeft;
    }

    @Override
    public FormattedCharSequence getVisualOrder(FormattedText param0) {
        return FormattedBidiReorder.reorder(param0, this.defaultRightToLeft);
    }
}
