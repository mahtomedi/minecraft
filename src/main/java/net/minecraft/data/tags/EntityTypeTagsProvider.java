package net.minecraft.data.tags;

import java.nio.file.Path;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

public class EntityTypeTagsProvider extends TagsProvider<EntityType<?>> {
    public EntityTypeTagsProvider(DataGenerator param0) {
        super(param0, Registry.ENTITY_TYPE);
    }

    @Override
    protected void addTags() {
        this.tag(EntityTypeTags.SKELETONS).add(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON);
        this.tag(EntityTypeTags.RAIDERS)
            .add(EntityType.EVOKER, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH);
        this.tag(EntityTypeTags.BEEHIVE_INHABITORS).add(EntityType.BEE);
        this.tag(EntityTypeTags.ARROWS).add(EntityType.ARROW, EntityType.SPECTRAL_ARROW);
        this.tag(EntityTypeTags.IMPACT_PROJECTILES)
            .addTag(EntityTypeTags.ARROWS)
            .add(
                EntityType.SNOWBALL,
                EntityType.FIREBALL,
                EntityType.SMALL_FIREBALL,
                EntityType.EGG,
                EntityType.TRIDENT,
                EntityType.DRAGON_FIREBALL,
                EntityType.WITHER_SKULL
            );
        this.tag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(EntityType.RABBIT, EntityType.ENDERMITE, EntityType.SILVERFISH);
    }

    @Override
    protected Path getPath(ResourceLocation param0) {
        return this.generator.getOutputFolder().resolve("data/" + param0.getNamespace() + "/tags/entity_types/" + param0.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Entity Type Tags";
    }
}
