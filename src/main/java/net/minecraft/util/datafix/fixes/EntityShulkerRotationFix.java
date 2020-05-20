package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;

public class EntityShulkerRotationFix extends NamedEntityFix {
    public EntityShulkerRotationFix(Schema param0) {
        super(param0, false, "EntityShulkerRotationFix", References.ENTITY, "minecraft:shulker");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        List<Double> var0 = param0.get("Rotation").asList(param0x -> param0x.asDouble(180.0));
        if (!var0.isEmpty()) {
            var0.set(0, var0.get(0) - 180.0);
            return param0.set("Rotation", param0.createList(var0.stream().map(param0::createDouble)));
        } else {
            return param0;
        }
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }
}
