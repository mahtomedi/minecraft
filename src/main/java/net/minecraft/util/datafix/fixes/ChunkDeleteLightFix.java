package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class ChunkDeleteLightFix extends DataFix {
    public ChunkDeleteLightFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> var1 = var0.findField("sections");
        return this.fixTypeEverywhereTyped("ChunkDeleteLightFix for " + this.getOutputSchema().getVersionKey(), var0, param1 -> {
            param1 = param1.update(DSL.remainderFinder(), param0x -> param0x.remove("isLightOn"));
            return param1.updateTyped(var1, param0x -> param0x.update(DSL.remainderFinder(), param0xx -> param0xx.remove("BlockLight").remove("SkyLight")));
        });
    }
}
