package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsAccessibilityOnboardFix extends DataFix {
    public OptionsAccessibilityOnboardFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsAccessibilityOnboardFix",
            this.getInputSchema().getType(References.OPTIONS),
            param0 -> param0.update(DSL.remainderFinder(), param0x -> param0x.set("onboardAccessibility", param0x.createBoolean(false)))
        );
    }
}
