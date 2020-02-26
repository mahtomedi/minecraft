package net.minecraft.tags;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class EntityTypeTags {
    private static TagCollection<EntityType<?>> source = new TagCollection<>(param0 -> Optional.empty(), "", false, "");
    private static int resetCount;
    public static final Tag<EntityType<?>> SKELETONS = bind("skeletons");
    public static final Tag<EntityType<?>> RAIDERS = bind("raiders");
    public static final Tag<EntityType<?>> BEEHIVE_INHABITORS = bind("beehive_inhabitors");
    public static final Tag<EntityType<?>> ARROWS = bind("arrows");
    public static final Tag<EntityType<?>> IMPACT_PROJECTILES = bind("impact_projectiles");

    public static void reset(TagCollection<EntityType<?>> param0) {
        source = param0;
        ++resetCount;
    }

    public static TagCollection<EntityType<?>> getAllTags() {
        return source;
    }

    private static Tag<EntityType<?>> bind(String param0) {
        return new EntityTypeTags.Wrapper(new ResourceLocation(param0));
    }

    public static class Wrapper extends Tag<EntityType<?>> {
        private int check = -1;
        private Tag<EntityType<?>> actual;

        public Wrapper(ResourceLocation param0) {
            super(param0);
        }

        public boolean contains(EntityType<?> param0) {
            if (this.check != EntityTypeTags.resetCount) {
                this.actual = EntityTypeTags.source.getTagOrEmpty(this.getId());
                this.check = EntityTypeTags.resetCount;
            }

            return this.actual.contains(param0);
        }

        @Override
        public Collection<EntityType<?>> getValues() {
            if (this.check != EntityTypeTags.resetCount) {
                this.actual = EntityTypeTags.source.getTagOrEmpty(this.getId());
                this.check = EntityTypeTags.resetCount;
            }

            return this.actual.getValues();
        }

        @Override
        public Collection<Tag.Entry<EntityType<?>>> getSource() {
            if (this.check != EntityTypeTags.resetCount) {
                this.actual = EntityTypeTags.source.getTagOrEmpty(this.getId());
                this.check = EntityTypeTags.resetCount;
            }

            return this.actual.getSource();
        }
    }
}
