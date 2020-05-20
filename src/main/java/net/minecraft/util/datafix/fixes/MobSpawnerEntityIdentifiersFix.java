package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;

public class MobSpawnerEntityIdentifiersFix extends DataFix {
    public MobSpawnerEntityIdentifiersFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    private Dynamic<?> fix(Dynamic<?> param0) {
        if (!"MobSpawner".equals(param0.get("id").asString(""))) {
            return param0;
        } else {
            Optional<String> var0 = param0.get("EntityId").asString().result();
            if (var0.isPresent()) {
                Dynamic<?> var1 = DataFixUtils.orElse(param0.get("SpawnData").result(), param0.emptyMap());
                var1 = var1.set("id", var1.createString(var0.get().isEmpty() ? "Pig" : var0.get()));
                param0 = param0.set("SpawnData", var1);
                param0 = param0.remove("EntityId");
            }

            Optional<? extends Stream<? extends Dynamic<?>>> var2 = param0.get("SpawnPotentials").asStreamOpt().result();
            if (var2.isPresent()) {
                param0 = param0.set(
                    "SpawnPotentials",
                    param0.createList(
                        var2.get()
                            .map(
                                param0x -> {
                                    Optional<String> var0x = param0x.get("Type").asString().result();
                                    if (var0x.isPresent()) {
                                        Dynamic<?> var1x = DataFixUtils.orElse(param0x.get("Properties").result(), param0x.emptyMap())
                                            .set("id", param0x.createString(var0x.get()));
                                        return param0x.set("Entity", var1x).remove("Type").remove("Properties");
                                    } else {
                                        return param0x;
                                    }
                                }
                            )
                    )
                );
            }

            return param0;
        }
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.UNTAGGED_SPAWNER);
        return this.fixTypeEverywhereTyped("MobSpawnerEntityIdentifiersFix", this.getInputSchema().getType(References.UNTAGGED_SPAWNER), var0, param1 -> {
            Dynamic<?> var0x = param1.get(DSL.remainderFinder());
            var0x = var0x.set("id", var0x.createString("MobSpawner"));
            DataResult<? extends Pair<? extends Typed<?>, ?>> var1x = var0.readTyped(this.fix(var0x));
            return !var1x.result().isPresent() ? param1 : (Typed)((Pair)var1x.result().get()).getFirst();
        });
    }
}
