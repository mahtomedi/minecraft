package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class BlockStateStructureTemplateFix extends DataFix {
    public BlockStateStructureTemplateFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "BlockStateStructureTemplateFix",
            this.getInputSchema().getType(References.BLOCK_STATE),
            param0 -> param0.update(DSL.remainderFinder(), BlockStateData::upgradeBlockStateTag)
        );
    }
}
