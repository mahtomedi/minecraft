package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;

public class LegacyDragonFightFix extends DataFix {
    public LegacyDragonFightFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "LegacyDragonFightFix", this.getInputSchema().getType(References.LEVEL), param0 -> param0.update(DSL.remainderFinder(), param0x -> {
                    OptionalDynamic<?> var0x = param0x.get("DragonFight");
                    if (var0x.result().isPresent()) {
                        return param0x;
                    } else {
                        Dynamic<?> var1 = param0x.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap();
                        return param0x.set("DragonFight", var1);
                    }
                })
        );
    }
}
