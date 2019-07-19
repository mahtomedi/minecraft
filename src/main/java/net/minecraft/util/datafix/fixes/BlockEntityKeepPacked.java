package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class BlockEntityKeepPacked extends NamedEntityFix {
    public BlockEntityKeepPacked(Schema param0, boolean param1) {
        super(param0, param1, "BlockEntityKeepPacked", References.BLOCK_ENTITY, "DUMMY");
    }

    private static Dynamic<?> fixTag(Dynamic<?> param0) {
        return param0.set("keepPacked", param0.createBoolean(true));
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), BlockEntityKeepPacked::fixTag);
    }
}
