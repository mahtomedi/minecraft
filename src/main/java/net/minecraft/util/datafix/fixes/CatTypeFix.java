package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class CatTypeFix extends NamedEntityFix {
    public CatTypeFix(Schema param0, boolean param1) {
        super(param0, param1, "CatTypeFix", References.ENTITY, "minecraft:cat");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        return param0.get("CatType").asInt(0) == 9 ? param0.set("CatType", param0.createInt(10)) : param0;
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }
}
