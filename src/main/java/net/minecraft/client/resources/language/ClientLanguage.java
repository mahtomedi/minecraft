package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientLanguage extends Language {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z])");
    private final Map<String, String> storage;
    private final boolean requiresReordering;

    private ClientLanguage(Map<String, String> param0, boolean param1) {
        this.storage = param0;
        this.requiresReordering = param1;
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
            } catch (IOException var17) {
                LOGGER.warn("Failed to load translations from {}", var0, var17);
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
    public boolean requiresReordering() {
        return this.requiresReordering;
    }

    @Override
    public String reorder(String param0, boolean param1) {
        if (!this.requiresReordering) {
            return param0;
        } else {
            if (param1 && param0.indexOf(37) != -1) {
                param0 = wrapFormatCodes(param0);
            }

            return this.reorder(param0);
        }
    }

    public static String wrapFormatCodes(String param0) {
        Matcher var0 = FORMAT_PATTERN.matcher(param0);
        StringBuffer var1 = new StringBuffer();
        int var2 = 1;

        while(var0.find()) {
            String var3 = var0.group(1);
            String var4 = var3 != null ? var3 : Integer.toString(var2++);
            String var5 = var0.group(2);
            String var6 = Matcher.quoteReplacement("\u2066%" + var4 + "$" + var5 + "\u2069");
            var0.appendReplacement(var1, var6);
        }

        var0.appendTail(var1);
        return var1.toString();
    }

    private String reorder(String param0) {
        try {
            Bidi var0 = new Bidi(new ArabicShaping(8).shape(param0), 127);
            var0.setReorderingMode(0);
            return var0.writeReordered(10);
        } catch (ArabicShapingException var3) {
            return param0;
        }
    }
}
