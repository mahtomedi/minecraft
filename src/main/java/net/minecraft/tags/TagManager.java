package net.minecraft.tags;

import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagManager implements PreparableReloadListener {
    private final TagLoader<Block> blocks = new TagLoader<>(Registry.BLOCK::getOptional, "tags/blocks", "block");
    private final TagLoader<Item> items = new TagLoader<>(Registry.ITEM::getOptional, "tags/items", "item");
    private final TagLoader<Fluid> fluids = new TagLoader<>(Registry.FLUID::getOptional, "tags/fluids", "fluid");
    private final TagLoader<EntityType<?>> entityTypes = new TagLoader<>(Registry.ENTITY_TYPE::getOptional, "tags/entity_types", "entity_type");
    private TagContainer tags = TagContainer.EMPTY;

    public TagContainer getTags() {
        return this.tags;
    }

    @Override
    public CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier param0,
        ResourceManager param1,
        ProfilerFiller param2,
        ProfilerFiller param3,
        Executor param4,
        Executor param5
    ) {
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> var0 = this.blocks.prepare(param1, param4);
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> var1 = this.items.prepare(param1, param4);
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> var2 = this.fluids.prepare(param1, param4);
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> var3 = this.entityTypes.prepare(param1, param4);
        return CompletableFuture.allOf(var0, var1, var2, var3)
            .thenCompose(param0::wait)
            .thenAcceptAsync(
                param4x -> {
                    TagCollection<Block> var0x = this.blocks.load(var0.join());
                    TagCollection<Item> var1x = this.items.load(var1.join());
                    TagCollection<Fluid> var2x = this.fluids.load(var2.join());
                    TagCollection<EntityType<?>> var3x = this.entityTypes.load(var3.join());
                    TagContainer var4x = TagContainer.of(var0x, var1x, var2x, var3x);
                    Multimap<ResourceLocation, ResourceLocation> var5x = StaticTags.getAllMissingTags(var4x);
                    if (!var5x.isEmpty()) {
                        throw new IllegalStateException(
                            "Missing required tags: "
                                + (String)var5x.entries()
                                    .stream()
                                    .map(param0x -> param0x.getKey() + ":" + param0x.getValue())
                                    .sorted()
                                    .collect(Collectors.joining(","))
                        );
                    } else {
                        SerializationTags.bind(var4x);
                        this.tags = var4x;
                    }
                },
                param5
            );
    }
}
