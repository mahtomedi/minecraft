package net.minecraft.tags;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class SerializationTags {
    private static volatile SerializationTags instance = new SerializationTags(
        BlockTags.getAllTags(), ItemTags.getAllTags(), FluidTags.getAllTags(), EntityTypeTags.getAllTags()
    );
    private final TagCollection<Block> blocks;
    private final TagCollection<Item> items;
    private final TagCollection<Fluid> fluids;
    private final TagCollection<EntityType<?>> entityTypes;

    private SerializationTags(TagCollection<Block> param0, TagCollection<Item> param1, TagCollection<Fluid> param2, TagCollection<EntityType<?>> param3) {
        this.blocks = param0;
        this.items = param1;
        this.fluids = param2;
        this.entityTypes = param3;
    }

    public TagCollection<Block> getBlocks() {
        return this.blocks;
    }

    public TagCollection<Item> getItems() {
        return this.items;
    }

    public TagCollection<Fluid> getFluids() {
        return this.fluids;
    }

    public TagCollection<EntityType<?>> getEntityTypes() {
        return this.entityTypes;
    }

    public static SerializationTags getInstance() {
        return instance;
    }

    public static void bind(TagCollection<Block> param0, TagCollection<Item> param1, TagCollection<Fluid> param2, TagCollection<EntityType<?>> param3) {
        instance = new SerializationTags(param0, param1, param2, param3);
    }
}
