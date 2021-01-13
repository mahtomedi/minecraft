package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldTemplate extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public String id = "";
    public String name = "";
    public String version = "";
    public String author = "";
    public String link = "";
    @Nullable
    public String image;
    public String trailer = "";
    public String recommendedPlayers = "";
    public WorldTemplate.WorldTemplateType type = WorldTemplate.WorldTemplateType.WORLD_TEMPLATE;

    public static WorldTemplate parse(JsonObject param0) {
        WorldTemplate var0 = new WorldTemplate();

        try {
            var0.id = JsonUtils.getStringOr("id", param0, "");
            var0.name = JsonUtils.getStringOr("name", param0, "");
            var0.version = JsonUtils.getStringOr("version", param0, "");
            var0.author = JsonUtils.getStringOr("author", param0, "");
            var0.link = JsonUtils.getStringOr("link", param0, "");
            var0.image = JsonUtils.getStringOr("image", param0, null);
            var0.trailer = JsonUtils.getStringOr("trailer", param0, "");
            var0.recommendedPlayers = JsonUtils.getStringOr("recommendedPlayers", param0, "");
            var0.type = WorldTemplate.WorldTemplateType.valueOf(JsonUtils.getStringOr("type", param0, WorldTemplate.WorldTemplateType.WORLD_TEMPLATE.name()));
        } catch (Exception var3) {
            LOGGER.error("Could not parse WorldTemplate: " + var3.getMessage());
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum WorldTemplateType {
        WORLD_TEMPLATE,
        MINIGAME,
        ADVENTUREMAP,
        EXPERIENCE,
        INSPIRATION;
    }
}
