package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsForceVBOFix extends DataFix {
    public OptionsForceVBOFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsForceVBOFix",
            this.getInputSchema().getType(References.OPTIONS),
            param0 -> param0.update(DSL.remainderFinder(), param0x -> param0x.set("useVbo", param0x.createString("true")))
        );
    }
}
