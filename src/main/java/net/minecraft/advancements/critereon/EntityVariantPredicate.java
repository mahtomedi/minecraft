package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityVariantPredicate<V> {
    private static final String VARIANT_KEY = "variant";
    final Codec<V> variantCodec;
    final Function<Entity, Optional<V>> getter;
    final EntitySubPredicate.Type type;

    public static <V> EntityVariantPredicate<V> create(Registry<V> param0, Function<Entity, Optional<V>> param1) {
        return new EntityVariantPredicate<>(param0.byNameCodec(), param1);
    }

    public static <V> EntityVariantPredicate<V> create(Codec<V> param0, Function<Entity, Optional<V>> param1) {
        return new EntityVariantPredicate<>(param0, param1);
    }

    private EntityVariantPredicate(Codec<V> param0, Function<Entity, Optional<V>> param1) {
        this.variantCodec = param0;
        this.getter = param1;
        this.type = param1x -> {
            JsonElement var0 = param1x.get("variant");
            if (var0 == null) {
                throw new JsonParseException("Missing variant field");
            } else {
                V var1x = Util.getOrThrow(param0.decode(new Dynamic<>(JsonOps.INSTANCE, var0)), JsonParseException::new).getFirst();
                return this.createPredicate((V)var1x);
            }
        };
    }

    public EntitySubPredicate.Type type() {
        return this.type;
    }

    public EntitySubPredicate createPredicate(final V param0) {
        return new EntitySubPredicate() {
            @Override
            public boolean matches(Entity param0x, ServerLevel param1, @Nullable Vec3 param2) {
                return EntityVariantPredicate.this.getter.apply(param0).filter(param1x -> param1x.equals(param0)).isPresent();
            }

            @Override
            public JsonObject serializeCustomData() {
                JsonObject var0 = new JsonObject();
                var0.add(
                    "variant",
                    Util.getOrThrow(
                        EntityVariantPredicate.this.variantCodec.encodeStart(JsonOps.INSTANCE, param0),
                        param1 -> new JsonParseException("Can't serialize variant " + param0 + ", message " + param1)
                    )
                );
                return var0;
            }

            @Override
            public EntitySubPredicate.Type type() {
                return EntityVariantPredicate.this.type;
            }
        };
    }
}
