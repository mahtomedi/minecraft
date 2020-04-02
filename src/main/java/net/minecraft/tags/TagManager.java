package net.minecraft.tags;

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
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> var0 = this.blocks.prepare(param1, param4);
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> var1 = this.items.prepare(param1, param4);
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> var2 = this.fluids.prepare(param1, param4);
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> var3 = this.entityTypes.prepare(param1, param4);
        return CompletableFuture.allOf(var0, var1, var2, var3).thenCompose(param0::wait).thenAcceptAsync(param4x -> {
            this.blocks.load(var0.join());
            this.items.load(var1.join());
            this.fluids.load(var2.join());
            this.entityTypes.load(var3.join());
            BlockTags.reset(this.blocks);
            ItemTags.reset(this.items);
            FluidTags.reset(this.fluids);
            EntityTypeTags.reset(this.entityTypes);
        }, param5);
    }
}
