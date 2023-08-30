package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.slf4j.Logger;

public class ServerAdvancementManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private Map<ResourceLocation, AdvancementHolder> advancements = Map.of();
    private AdvancementTree tree = new AdvancementTree();
    private final LootDataManager lootData;

    public ServerAdvancementManager(LootDataManager param0) {
        super(GSON, "advancements");
        this.lootData = param0;
    }

    protected void apply(Map<ResourceLocation, JsonElement> param0, ResourceManager param1, ProfilerFiller param2) {
        Builder<ResourceLocation, AdvancementHolder> var0 = ImmutableMap.builder();
        param0.forEach((param1x, param2x) -> {
            try {
                JsonObject var0x = GsonHelper.convertToJsonObject(param2x, "advancement");
                Advancement var1x = Advancement.fromJson(var0x, new DeserializationContext(param1x, this.lootData));
                var0.put(param1x, new AdvancementHolder(param1x, var1x));
            } catch (Exception var6) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", param1x, var6.getMessage());
            }

        });
        this.advancements = var0.buildOrThrow();
        AdvancementTree var1 = new AdvancementTree();
        var1.addAll(this.advancements.values());

        for(AdvancementNode var2 : var1.roots()) {
            if (var2.holder().value().display().isPresent()) {
                TreeNodePosition.run(var2);
            }
        }

        this.tree = var1;
    }

    @Nullable
    public AdvancementHolder get(ResourceLocation param0) {
        return this.advancements.get(param0);
    }

    public AdvancementTree tree() {
        return this.tree;
    }

    public Collection<AdvancementHolder> getAllAdvancements() {
        return this.advancements.values();
    }
}
