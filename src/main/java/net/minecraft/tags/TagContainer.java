package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public interface TagContainer {
    TagContainer EMPTY = of(TagCollection.empty(), TagCollection.empty(), TagCollection.empty(), TagCollection.empty(), TagCollection.empty());

    TagCollection<Block> getBlocks();

    TagCollection<Item> getItems();

    TagCollection<Fluid> getFluids();

    TagCollection<EntityType<?>> getEntityTypes();

    TagCollection<GameEvent> getGameEvents();

    default void bindToGlobal() {
        StaticTags.resetAll(this);
        Blocks.rebuildCache();
    }

    default void serializeToNetwork(FriendlyByteBuf param0) {
        this.getBlocks().serializeToNetwork(param0, Registry.BLOCK);
        this.getItems().serializeToNetwork(param0, Registry.ITEM);
        this.getFluids().serializeToNetwork(param0, Registry.FLUID);
        this.getEntityTypes().serializeToNetwork(param0, Registry.ENTITY_TYPE);
        this.getGameEvents().serializeToNetwork(param0, Registry.GAME_EVENT);
    }

    static TagContainer deserializeFromNetwork(FriendlyByteBuf param0) {
        TagCollection<Block> var0 = TagCollection.loadFromNetwork(param0, Registry.BLOCK);
        TagCollection<Item> var1 = TagCollection.loadFromNetwork(param0, Registry.ITEM);
        TagCollection<Fluid> var2 = TagCollection.loadFromNetwork(param0, Registry.FLUID);
        TagCollection<EntityType<?>> var3 = TagCollection.loadFromNetwork(param0, Registry.ENTITY_TYPE);
        TagCollection<GameEvent> var4 = TagCollection.loadFromNetwork(param0, Registry.GAME_EVENT);
        return of(var0, var1, var2, var3, var4);
    }

    static TagContainer of(
        final TagCollection<Block> param0,
        final TagCollection<Item> param1,
        final TagCollection<Fluid> param2,
        final TagCollection<EntityType<?>> param3,
        final TagCollection<GameEvent> param4
    ) {
        return new TagContainer() {
            @Override
            public TagCollection<Block> getBlocks() {
                return param0;
            }

            @Override
            public TagCollection<Item> getItems() {
                return param1;
            }

            @Override
            public TagCollection<Fluid> getFluids() {
                return param2;
            }

            @Override
            public TagCollection<EntityType<?>> getEntityTypes() {
                return param3;
            }

            @Override
            public TagCollection<GameEvent> getGameEvents() {
                return param4;
            }
        };
    }
}
