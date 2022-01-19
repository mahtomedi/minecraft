package net.minecraft.server;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.PredicateManager;
import org.slf4j.Logger;

public class ServerAdvancementManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private AdvancementList advancements = new AdvancementList();
    private final PredicateManager predicateManager;

    public ServerAdvancementManager(PredicateManager param0) {
        super(GSON, "advancements");
        this.predicateManager = param0;
    }

    protected void apply(Map<ResourceLocation, JsonElement> param0, ResourceManager param1, ProfilerFiller param2) {
        Map<ResourceLocation, Advancement.Builder> var0 = Maps.newHashMap();
        param0.forEach((param1x, param2x) -> {
            try {
                JsonObject var0x = GsonHelper.convertToJsonObject(param2x, "advancement");
                Advancement.Builder var1x = Advancement.Builder.fromJson(var0x, new DeserializationContext(param1x, this.predicateManager));
                var0.put(param1x, var1x);
            } catch (Exception var6) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", param1x, var6.getMessage());
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
