package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class ChunkDeleteIgnoredLightDataFix extends DataFix {
    public ChunkDeleteIgnoredLightDataFix(Schema param0) {
        super(param0, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> var1 = var0.findField("sections");
        return this.fixTypeEverywhereTyped(
            "ChunkDeleteIgnoredLightDataFix",
            var0,
            param1 -> {
                boolean var0x = param1.get(DSL.remainderFinder()).get("isLightOn").asBoolean(false);
                return !var0x
                    ? param1.updateTyped(var1, param0x -> param0x.update(DSL.remainderFinder(), param0xx -> param0xx.remove("BlockLight").remove("SkyLight")))
                    : param1;
            }
        );
    }
}
