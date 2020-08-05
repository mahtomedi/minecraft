package net.minecraft.tags;

import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class SerializationTags {
    private static volatile TagContainer instance = TagContainer.of(
        TagCollection.of(
            BlockTags.getWrappers()
                .stream()
                .collect(Collectors.toMap(Tag.Named::getName, (Function<? super Tag.Named<Block>, ? extends Tag.Named<Block>>)(param0 -> param0)))
        ),
        TagCollection.of(
            ItemTags.getWrappers()
                .stream()
                .collect(Collectors.toMap(Tag.Named::getName, (Function<? super Tag.Named<Item>, ? extends Tag.Named<Item>>)(param0 -> param0)))
        ),
        TagCollection.of(
            FluidTags.getWrappers()
                .stream()
                .collect(Collectors.toMap(Tag.Named::getName, (Function<? super Tag.Named<Fluid>, ? extends Tag.Named<Fluid>>)(param0 -> param0)))
        ),
        TagCollection.of(
            EntityTypeTags.getWrappers()
                .stream()
                .collect(
                    Collectors.toMap(Tag.Named::getName, (Function<? super Tag.Named<EntityType<?>>, ? extends Tag.Named<EntityType<?>>>)(param0 -> param0))
                )
        )
    );

    public static TagContainer getInstance() {
        return instance;
    }

    public static void bind(TagContainer param0) {
        instance = param0;
    }
}
