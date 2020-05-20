package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class StructureReferenceCountFix extends DataFix {
    public StructureReferenceCountFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
        return this.fixTypeEverywhereTyped(
            "Structure Reference Fix", var0, param0 -> param0.update(DSL.remainderFinder(), StructureReferenceCountFix::setCountToAtLeastOne)
        );
    }

    private static <T> Dynamic<T> setCountToAtLeastOne(Dynamic<T> param0) {
        return param0.update(
            "references", param0x -> param0x.createInt(param0x.asNumber().map(Number::intValue).result().filter(param0xx -> param0xx > 0).orElse(1))
        );
    }
}
