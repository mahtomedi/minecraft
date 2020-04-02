package net.minecraft.tags;

import net.minecraft.world.entity.EntityType;

public class EntityTypeTags {
    private static final StaticTagHelper<EntityType<?>> HELPER = new StaticTagHelper<>();
    public static final Tag.Named<EntityType<?>> SKELETONS = bind("skeletons");
    public static final Tag.Named<EntityType<?>> RAIDERS = bind("raiders");
    public static final Tag.Named<EntityType<?>> BEEHIVE_INHABITORS = bind("beehive_inhabitors");
    public static final Tag.Named<EntityType<?>> ARROWS = bind("arrows");
    public static final Tag.Named<EntityType<?>> IMPACT_PROJECTILES = bind("impact_projectiles");

    private static Tag.Named<EntityType<?>> bind(String param0) {
        return HELPER.bind(param0);
    }

    public static void reset(TagCollection<EntityType<?>> param0) {
        HELPER.reset(param0);
    }

    public static TagCollection<EntityType<?>> getAllTags() {
        return HELPER.getAllTags();
    }
}
