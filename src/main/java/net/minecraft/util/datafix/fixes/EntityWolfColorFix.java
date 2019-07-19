package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class EntityWolfColorFix extends NamedEntityFix {
    public EntityWolfColorFix(Schema param0, boolean param1) {
        super(param0, param1, "EntityWolfColorFix", References.ENTITY, "minecraft:wolf");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        return param0.update("CollarColor", param0x -> param0x.createByte((byte)(15 - param0x.asInt(0))));
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }
}
