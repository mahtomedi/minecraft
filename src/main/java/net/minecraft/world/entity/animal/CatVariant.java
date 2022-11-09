package net.minecraft.world.entity.animal;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record CatVariant(ResourceLocation texture) {
    public static final ResourceKey<CatVariant> TABBY = createKey("tabby");
    public static final ResourceKey<CatVariant> BLACK = createKey("black");
    public static final ResourceKey<CatVariant> RED = createKey("red");
    public static final ResourceKey<CatVariant> SIAMESE = createKey("siamese");
    public static final ResourceKey<CatVariant> BRITISH_SHORTHAIR = createKey("british_shorthair");
    public static final ResourceKey<CatVariant> CALICO = createKey("calico");
    public static final ResourceKey<CatVariant> PERSIAN = createKey("persian");
    public static final ResourceKey<CatVariant> RAGDOLL = createKey("ragdoll");
    public static final ResourceKey<CatVariant> WHITE = createKey("white");
    public static final ResourceKey<CatVariant> JELLIE = createKey("jellie");
    public static final ResourceKey<CatVariant> ALL_BLACK = createKey("all_black");

    private static ResourceKey<CatVariant> createKey(String param0) {
        return ResourceKey.create(Registries.CAT_VARIANT, new ResourceLocation(param0));
    }

    public static CatVariant bootstrap(Registry<CatVariant> param0) {
        register(param0, TABBY, "textures/entity/cat/tabby.png");
        register(param0, BLACK, "textures/entity/cat/black.png");
        register(param0, RED, "textures/entity/cat/red.png");
        register(param0, SIAMESE, "textures/entity/cat/siamese.png");
        register(param0, BRITISH_SHORTHAIR, "textures/entity/cat/british_shorthair.png");
        register(param0, CALICO, "textures/entity/cat/calico.png");
        register(param0, PERSIAN, "textures/entity/cat/persian.png");
        register(param0, RAGDOLL, "textures/entity/cat/ragdoll.png");
        register(param0, WHITE, "textures/entity/cat/white.png");
        register(param0, JELLIE, "textures/entity/cat/jellie.png");
        return register(param0, ALL_BLACK, "textures/entity/cat/all_black.png");
    }

    private static CatVariant register(Registry<CatVariant> param0, ResourceKey<CatVariant> param1, String param2) {
        return Registry.register(param0, param1, new CatVariant(new ResourceLocation(param2)));
    }
}
