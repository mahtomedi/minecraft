package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityVariantPredicate<V> {
    private static final String VARIANT_KEY = "variant";
    final Registry<V> registry;
    final Function<Entity, Optional<V>> getter;
    final EntitySubPredicate.Type type;

    public static <V> EntityVariantPredicate<V> create(Registry<V> param0, Function<Entity, Optional<V>> param1) {
        return new EntityVariantPredicate<>(param0, param1);
    }

    private EntityVariantPredicate(Registry<V> param0, Function<Entity, Optional<V>> param1) {
        this.registry = param0;
        this.getter = param1;
        this.type = param1x -> {
            String var0 = GsonHelper.getAsString(param1x, "variant");
            V var1x = param0.get(ResourceLocation.tryParse(var0));
            if (var1x == null) {
                throw new JsonSyntaxException("Unknown variant: " + var0);
            } else {
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
                var0.addProperty("variant", EntityVariantPredicate.this.registry.getKey(param0).toString());
                return var0;
            }

            @Override
            public EntitySubPredicate.Type type() {
                return EntityVariantPredicate.this.type;
            }
        };
    }
}
