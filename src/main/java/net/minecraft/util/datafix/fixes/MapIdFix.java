package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class MapIdFix extends DataFix {
    public MapIdFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "Map id fix",
            this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA),
            param0 -> param0.update(DSL.remainderFinder(), param0x -> param0x.createMap(ImmutableMap.of(param0x.createString("data"), param0x)))
        );
    }
}
