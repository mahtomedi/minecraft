package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class ChunkStatusFix extends DataFix {
    public ChunkStatusFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        Type<?> var1 = var0.findFieldType("Level");
        OpticFinder<?> var2 = DSL.fieldFinder("Level", var1);
        return this.fixTypeEverywhereTyped(
            "ChunkStatusFix", var0, this.getOutputSchema().getType(References.CHUNK), param1 -> param1.updateTyped(var2, param0x -> {
                    Dynamic<?> var0x = param0x.get(DSL.remainderFinder());
                    String var1x = var0x.get("Status").asString("empty");
                    if (Objects.equals(var1x, "postprocessed")) {
                        var0x = var0x.set("Status", var0x.createString("fullchunk"));
                    }
    
                    return param0x.set(DSL.remainderFinder(), var0x);
                })
        );
    }
}
