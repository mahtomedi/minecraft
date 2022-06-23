package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;

public class BlendingDataRemoveFromNetherEndFix extends DataFix {
    public BlendingDataRemoveFromNetherEndFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.CHUNK);
        return this.fixTypeEverywhereTyped(
            "BlendingDataRemoveFromNetherEndFix",
            var0,
            param0 -> param0.update(DSL.remainderFinder(), param0x -> updateChunkTag(param0x, param0x.get("__context")))
        );
    }

    private static Dynamic<?> updateChunkTag(Dynamic<?> param0, OptionalDynamic<?> param1) {
        boolean var0 = "minecraft:overworld".equals(param1.get("dimension").asString().result().orElse(""));
        return var0 ? param0 : param0.remove("blending_data");
    }
}
