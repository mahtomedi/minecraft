package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
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
import net.minecraft.server.packs.resources.ResourceThunk;
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

    public static ClientLanguage loadFrom(ResourceManager param0, List<LanguageInfo> param1) {
        Map<String, String> var0 = Maps.newHashMap();
        boolean var1 = false;

        for(LanguageInfo var2 : param1) {
            var1 |= var2.isBidirectional();
            String var3 = var2.getCode();
            String var4 = String.format("lang/%s.json", var3);

            for(String var5 : param0.getNamespaces()) {
                try {
                    ResourceLocation var6 = new ResourceLocation(var5, var4);
                    appendFrom(var3, param0.getResourceStack(var6), var0);
                } catch (FileNotFoundException var11) {
                } catch (Exception var12) {
                    LOGGER.warn("Skipped language file: {}:{} ({})", var5, var4, var12.toString());
                }
            }
        }

        return new ClientLanguage(ImmutableMap.copyOf(var0), var1);
    }

    private static void appendFrom(String param0, List<ResourceThunk> param1, Map<String, String> param2) {
        for(ResourceThunk var0 : param1) {
            try (
                Resource var1 = var0.open();
                InputStream var2 = var1.getInputStream();
            ) {
                Language.loadFromJson(var2, param2::put);
            } catch (IOException var13) {
                LOGGER.warn("Failed to load translations for {} from pack {}", param0, var0.sourcePackId(), var13);
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
