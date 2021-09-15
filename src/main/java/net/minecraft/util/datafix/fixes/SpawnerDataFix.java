package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;

public class SpawnerDataFix extends DataFix {
    public SpawnerDataFix(Schema param0) {
        super(param0, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.UNTAGGED_SPAWNER);
        Type<?> var1 = this.getOutputSchema().getType(References.UNTAGGED_SPAWNER);
        OpticFinder<?> var2 = var0.findField("SpawnData");
        Type<?> var3 = var1.findField("SpawnData").type();
        OpticFinder<?> var4 = var0.findField("SpawnPotentials");
        Type<?> var5 = var1.findField("SpawnPotentials").type();
        return this.fixTypeEverywhereTyped(
            "Fix mob spawner data structure",
            var0,
            var1,
            param4 -> param4.updateTyped(var2, var3, param1x -> this.wrapEntityToSpawnData(var3, param1x))
                    .updateTyped(var4, var5, param1x -> this.wrapSpawnPotentialsToWeightedEntries(var5, param1x))
        );
    }

    private <T> Typed<T> wrapEntityToSpawnData(Type<T> param0, Typed<?> param1) {
        DynamicOps<?> var0 = param1.getOps();
        return new Typed<>(param0, var0, (T)Pair.<Object, Dynamic<?>>of(param1.getValue(), new Dynamic<>(var0)));
    }

    private <T> Typed<T> wrapSpawnPotentialsToWeightedEntries(Type<T> param0, Typed<?> param1) {
        DynamicOps<?> var0 = param1.getOps();
        List<?> var1 = (List)param1.getValue();
        List<?> var2 = var1.stream().map(param1x -> {
            Pair<Object, Dynamic<?>> var0x = (Pair)param1x;
            int var1x = var0x.getSecond().get("Weight").asNumber().result().orElse(1).intValue();
            Dynamic<?> var2x = new Dynamic<>(var0);
            var2x = var2x.set("weight", var2x.createInt(var1x));
            Dynamic<?> var3x = var0x.getSecond().remove("Weight").remove("Entity");
            return Pair.of(Pair.of(var0x.getFirst(), var3x), var2x);
        }).toList();
        return new Typed<>(param0, var0, (T)var2);
    }
}
