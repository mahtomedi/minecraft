package net.minecraft.server;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerAdvancementManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder()
        .registerTypeHierarchyAdapter(Advancement.Builder.class, (JsonDeserializer<Advancement.Builder>)(param0, param1, param2) -> {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "advancement");
            return Advancement.Builder.fromJson(var0, param2);
        })
        .registerTypeAdapter(AdvancementRewards.class, new AdvancementRewards.Deserializer())
        .registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
        .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
        .registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
        .create();
    private AdvancementList advancements = new AdvancementList();

    public ServerAdvancementManager() {
        super(GSON, "advancements");
    }

    protected void apply(Map<ResourceLocation, JsonObject> param0, ResourceManager param1, ProfilerFiller param2) {
        Map<ResourceLocation, Advancement.Builder> var0 = Maps.newHashMap();
        param0.forEach((param1x, param2x) -> {
            try {
                Advancement.Builder var0x = GSON.fromJson(param2x, Advancement.Builder.class);
                var0.put(param1x, var0x);
            } catch (IllegalArgumentException | JsonParseException var4x) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", param1x, var4x.getMessage());
            }

        });
        AdvancementList var1 = new AdvancementList();
        var1.add(var0);

        for(Advancement var2 : var1.getRoots()) {
            if (var2.getDisplay() != null) {
                TreeNodePosition.run(var2);
            }
        }

        this.advancements = var1;
    }

    @Nullable
    public Advancement getAdvancement(ResourceLocation param0) {
        return this.advancements.get(param0);
    }

    public Collection<Advancement> getAllAdvancements() {
        return this.advancements.getAllAdvancements();
    }
}
