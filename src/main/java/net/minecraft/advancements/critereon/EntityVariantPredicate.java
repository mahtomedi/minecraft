package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityVariantPredicate<V> {
    private final Function<Entity, Optional<V>> getter;
    private final EntitySubPredicate.Type type;

    public static <V> EntityVariantPredicate<V> create(Registry<V> param0, Function<Entity, Optional<V>> param1) {
        return new EntityVariantPredicate<>(param0.byNameCodec(), param1);
    }

    public static <V> EntityVariantPredicate<V> create(Codec<V> param0, Function<Entity, Optional<V>> param1) {
        return new EntityVariantPredicate<>(param0, param1);
    }

    private EntityVariantPredicate(Codec<V> param0, Function<Entity, Optional<V>> param1) {
        this.getter = param1;
        MapCodec<EntityVariantPredicate.SubPredicate<V>> var0 = RecordCodecBuilder.mapCodec(
            param1x -> param1x.group(param0.fieldOf("variant").forGetter(EntityVariantPredicate.SubPredicate::variant)).apply(param1x, this::createPredicate)
        );
        this.type = new EntitySubPredicate.Type(var0);
    }

    public EntitySubPredicate.Type type() {
        return this.type;
    }

    public EntityVariantPredicate.SubPredicate<V> createPredicate(V param0) {
        return new EntityVariantPredicate.SubPredicate<>(this.type, this.getter, param0);
    }

    public static record SubPredicate<V>(EntitySubPredicate.Type type, Function<Entity, Optional<V>> getter, V variant) implements EntitySubPredicate {
        @Override
        public boolean matches(Entity param0, ServerLevel param1, @Nullable Vec3 param2) {
            return this.getter.apply(param0).filter(param0x -> param0x.equals(this.variant)).isPresent();
        }
    }
}
