package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldTemplatePaginatedList extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public List<WorldTemplate> templates;
    public int page;
    public int size;
    public int total;

    public WorldTemplatePaginatedList() {
    }

    public WorldTemplatePaginatedList(int param0) {
        this.templates = Collections.emptyList();
        this.page = 0;
        this.size = param0;
        this.total = -1;
    }

    public boolean isLastPage() {
        return this.page * this.size >= this.total && this.page > 0 && this.total > 0 && this.size > 0;
    }

    public static WorldTemplatePaginatedList parse(String param0) {
        WorldTemplatePaginatedList var0 = new WorldTemplatePaginatedList();
        var0.templates = Lists.newArrayList();

        try {
            JsonParser var1 = new JsonParser();
            JsonObject var2 = var1.parse(param0).getAsJsonObject();
            if (var2.get("templates").isJsonArray()) {
                Iterator<JsonElement> var3 = var2.get("templates").getAsJsonArray().iterator();

                while(var3.hasNext()) {
                    var0.templates.add(WorldTemplate.parse(var3.next().getAsJsonObject()));
                }
            }

            var0.page = JsonUtils.getIntOr("page", var2, 0);
            var0.size = JsonUtils.getIntOr("size", var2, 0);
            var0.total = JsonUtils.getIntOr("total", var2, 0);
        } catch (Exception var5) {
            LOGGER.error("Could not parse WorldTemplatePaginatedList: {}", var5.getMessage());
        }

        return var0;
    }
}
