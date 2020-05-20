package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class StriderGravityFix extends NamedEntityFix {
    public StriderGravityFix(Schema param0, boolean param1) {
        super(param0, param1, "StriderGravityFix", References.ENTITY, "minecraft:strider");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        return param0.get("NoGravity").asBoolean(false) ? param0.set("NoGravity", param0.createBoolean(false)) : param0;
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }
}
