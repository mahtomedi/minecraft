package net.minecraft.client.resources.metadata.language;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.resources.language.Language;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageMetadataSectionSerializer implements MetadataSectionSerializer<LanguageMetadataSection> {
    public LanguageMetadataSection fromJson(JsonObject param0) {
        Set<Language> var0 = Sets.newHashSet();

        for(Entry<String, JsonElement> var1 : param0.entrySet()) {
            String var2 = var1.getKey();
            if (var2.length() > 16) {
                throw new JsonParseException("Invalid language->'" + var2 + "': language code must not be more than " + 16 + " characters long");
            }

            JsonObject var3 = GsonHelper.convertToJsonObject(var1.getValue(), "language");
            String var4 = GsonHelper.getAsString(var3, "region");
            String var5 = GsonHelper.getAsString(var3, "name");
            boolean var6 = GsonHelper.getAsBoolean(var3, "bidirectional", false);
            if (var4.isEmpty()) {
                throw new JsonParseException("Invalid language->'" + var2 + "'->region: empty value");
            }

            if (var5.isEmpty()) {
                throw new JsonParseException("Invalid language->'" + var2 + "'->name: empty value");
            }

            if (!var0.add(new Language(var2, var4, var5, var6))) {
                throw new JsonParseException("Duplicate language->'" + var2 + "' defined");
            }
        }

        return new LanguageMetadataSection(var0);
    }

    @Override
    public String getMetadataSectionName() {
        return "language";
    }
}
