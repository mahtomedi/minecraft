package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class RandomSequenceSettingsFix extends DataFix {
    public RandomSequenceSettingsFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "RandomSequenceSettingsFix",
            this.getInputSchema().getType(References.SAVED_DATA_RANDOM_SEQUENCES),
            param0 -> param0.update(DSL.remainderFinder(), param0x -> param0x.update("data", param0xx -> param0xx.emptyMap().set("sequences", param0xx)))
        );
    }
}
