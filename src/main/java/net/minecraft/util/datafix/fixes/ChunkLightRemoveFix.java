package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class ChunkLightRemoveFix extends DataFix {
    public ChunkLightRemoveFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        Type<?> var1 = var0.findFieldType("Level");
        OpticFinder<?> var2 = DSL.fieldFinder("Level", var1);
        return this.fixTypeEverywhereTyped(
            "ChunkLightRemoveFix",
            var0,
            this.getOutputSchema().getType(References.CHUNK),
            param1 -> param1.updateTyped(var2, param0x -> param0x.update(DSL.remainderFinder(), param0xx -> param0xx.remove("isLightOn")))
        );
    }
}
