package net.minecraft.tags;

import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagManager implements PreparableReloadListener {
    private final SynchronizableTagCollection<Block> blocks = new SynchronizableTagCollection<>(Registry.BLOCK, "tags/blocks", "block");
    private final SynchronizableTagCollection<Item> items = new SynchronizableTagCollection<>(Registry.ITEM, "tags/items", "item");
    private final SynchronizableTagCollection<Fluid> fluids = new SynchronizableTagCollection<>(Registry.FLUID, "tags/fluids", "fluid");
    private final SynchronizableTagCollection<EntityType<?>> entityTypes = new SynchronizableTagCollection<>(
        Registry.ENTITY_TYPE, "tags/entity_types", "entity_type"
    );

    public SynchronizableTagCollection<Block> getBlocks() {
        return this.blocks;
    }

    public SynchronizableTagCollection<Item> getItems() {
        return this.items;
    }

    public SynchronizableTagCollection<Fluid> getFluids() {
        return this.fluids;
    }

    public SynchronizableTagCollection<EntityType<?>> getEntityTypes() {
        return this.entityTypes;
    }

    public void serializeToNetwork(FriendlyByteBuf param0) {
        this.blocks.serializeToNetwork(param0);
        this.items.serializeToNetwork(param0);
        this.fluids.serializeToNetwork(param0);
        this.entityTypes.serializeToNetwork(param0);
    }

    public static TagManager deserializeFromNetwork(FriendlyByteBuf param0) {
        TagManager var0 = new TagManager();
        var0.getBlocks().loadFromNetwork(param0);
        var0.getItems().loadFromNetwork(param0);
        var0.getFluids().loadFromNetwork(param0);
        var0.getEntityTypes().loadFromNetwork(param0);
        return var0;
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
        CompletableFuture<Map<ResourceLocation, Tag.Builder<Block>>> var0 = this.blocks.prepare(param1, param4);
        CompletableFuture<Map<ResourceLocation, Tag.Builder<Item>>> var1 = this.items.prepare(param1, param4);
        CompletableFuture<Map<ResourceLocation, Tag.Builder<Fluid>>> var2 = this.fluids.prepare(param1, param4);
        CompletableFuture<Map<ResourceLocation, Tag.Builder<EntityType<?>>>> var3 = this.entityTypes.prepare(param1, param4);
        return var0.thenCombine(var1, Pair::of)
            .thenCombine(
                var2.thenCombine(var3, Pair::of),
                (param0x, param1x) -> new TagManager.Preparations(param0x.getFirst(), param0x.getSecond(), param1x.getFirst(), param1x.getSecond())
            )
            .thenCompose(param0::wait)
            .thenAcceptAsync(param0x -> {
                this.blocks.load(param0x.blocks);
                this.items.load(param0x.items);
                this.fluids.load(param0x.fluids);
                this.entityTypes.load(param0x.entityTypes);
                BlockTags.reset(this.blocks);
                ItemTags.reset(this.items);
                FluidTags.reset(this.fluids);
                EntityTypeTags.reset(this.entityTypes);
            }, param5);
    }

    public static class Preparations {
        final Map<ResourceLocation, Tag.Builder<Block>> blocks;
        final Map<ResourceLocation, Tag.Builder<Item>> items;
        final Map<ResourceLocation, Tag.Builder<Fluid>> fluids;
        final Map<ResourceLocation, Tag.Builder<EntityType<?>>> entityTypes;

        public Preparations(
            Map<ResourceLocation, Tag.Builder<Block>> param0,
            Map<ResourceLocation, Tag.Builder<Item>> param1,
            Map<ResourceLocation, Tag.Builder<Fluid>> param2,
            Map<ResourceLocation, Tag.Builder<EntityType<?>>> param3
        ) {
            this.blocks = param0;
            this.items = param1;
            this.fluids = param2;
            this.entityTypes = param3;
        }
    }
}
