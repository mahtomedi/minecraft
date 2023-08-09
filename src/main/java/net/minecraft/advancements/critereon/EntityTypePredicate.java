package net.minecraft.advancements.critereon;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record EntityTypePredicate(HolderSet<EntityType<?>> types) {
    public static final Codec<EntityTypePredicate> CODEC = Codec.either(
            TagKey.hashedCodec(Registries.ENTITY_TYPE), BuiltInRegistries.ENTITY_TYPE.holderByNameCodec()
        )
        .flatComapMap(
            param0 -> param0.map(
                    param0x -> new EntityTypePredicate(BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(param0x)),
                    param0x -> new EntityTypePredicate(HolderSet.direct(param0x))
                ),
            param0 -> {
                HolderSet<EntityType<?>> var0 = param0.types();
                Optional<TagKey<EntityType<?>>> var1 = var0.unwrapKey();
                if (var1.isPresent()) {
                    return DataResult.success(Either.left(var1.get()));
                } else {
                    return var0.size() == 1
                        ? DataResult.success(Either.right(var0.get(0)))
                        : DataResult.error(() -> "Entity type set must have a single element, but got " + var0.size());
                }
            }
        );

    public static EntityTypePredicate of(EntityType<?> param0) {
        return new EntityTypePredicate(HolderSet.direct(param0.builtInRegistryHolder()));
    }

    public static EntityTypePredicate of(TagKey<EntityType<?>> param0) {
        return new EntityTypePredicate(BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(param0));
    }

    public boolean matches(EntityType<?> param0) {
        return param0.is(this.types);
    }
}
