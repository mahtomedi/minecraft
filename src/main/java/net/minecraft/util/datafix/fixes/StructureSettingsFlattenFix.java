package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

public class StructureSettingsFlattenFix extends DataFix {
    public StructureSettingsFlattenFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.WORLD_GEN_SETTINGS);
        OpticFinder<?> var1 = var0.findField("dimensions");
        return this.fixTypeEverywhereTyped("StructureSettingsFlatten", var0, param1 -> param1.updateTyped(var1, param1x -> {
                Dynamic<?> var0x = param1x.write().result().orElseThrow();
                Dynamic<?> var1x = var0x.updateMapValues(StructureSettingsFlattenFix::fixDimension);
                return var1.type().readTyped(var1x).result().orElseThrow().getFirst();
            }));
    }

    private static Pair<Dynamic<?>, Dynamic<?>> fixDimension(Pair<Dynamic<?>, Dynamic<?>> param0) {
        Dynamic<?> var0 = param0.getSecond();
        return Pair.of(
            param0.getFirst(),
            var0.update(
                "generator", param0x -> param0x.update("settings", param0xx -> param0xx.update("structures", StructureSettingsFlattenFix::fixStructures))
            )
        );
    }

    private static Dynamic<?> fixStructures(Dynamic<?> param0) {
        Dynamic<?> var0 = param0.get("structures")
            .result()
            .get()
            .updateMapValues(param1 -> param1.mapSecond(param1x -> param1x.set("type", param0.createString("minecraft:random_spread"))));
        Dynamic<?> var1 = param0.get("stronghold").result().get().set("type", param0.createString("minecraft:concentric_rings"));
        return var0.set("minecraft:stronghold", var1);
    }
}
