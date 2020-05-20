package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityShulkerColorFix extends NamedEntityFix {
    public EntityShulkerColorFix(Schema param0, boolean param1) {
        super(param0, param1, "EntityShulkerColorFix", References.ENTITY, "minecraft:shulker");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        return !param0.get("Color").map(Dynamic::asNumber).result().isPresent() ? param0.set("Color", param0.createByte((byte)10)) : param0;
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }
}
