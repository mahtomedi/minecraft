package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientLanguage extends Language {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, String> storage;
    private final boolean defaultRightToLeft;

    private ClientLanguage(Map<String, String> param0, boolean param1) {
        this.storage = param0;
        this.defaultRightToLeft = param1;
    }

    public static ClientLanguage loadFrom(ResourceManager param0, List<LanguageInfo> param1) {
        Map<String, String> var0 = Maps.newHashMap();
        boolean var1 = false;

        for(LanguageInfo var2 : param1) {
            var1 |= var2.isBidirectional();
            String var3 = String.format("lang/%s.json", var2.getCode());

            for(String var4 : param0.getNamespaces()) {
                try {
                    ResourceLocation var5 = new ResourceLocation(var4, var3);
                    appendFrom(param0.getResources(var5), var0);
                } catch (FileNotFoundException var10) {
                } catch (Exception var11) {
                    LOGGER.warn("Skipped language file: {}:{} ({})", var4, var3, var11.toString());
                }
            }
        }

        return new ClientLanguage(ImmutableMap.copyOf(var0), var1);
    }

    private static void appendFrom(List<Resource> param0, Map<String, String> param1) {
        for(Resource var0 : param0) {
            try (InputStream var1 = var0.getInputStream()) {
                Language.loadFromJson(var1, param1::put);
            } catch (IOException var9) {
                LOGGER.warn("Failed to load translations from {}", var0, var9);
            }
        }

    }

    @Override
    public String getOrDefault(String param0) {
        return this.storage.getOrDefault(param0, param0);
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
