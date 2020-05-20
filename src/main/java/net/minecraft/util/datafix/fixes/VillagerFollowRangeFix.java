package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class VillagerFollowRangeFix extends NamedEntityFix {
    public VillagerFollowRangeFix(Schema param0) {
        super(param0, false, "Villager Follow Range Fix", References.ENTITY, "minecraft:villager");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), VillagerFollowRangeFix::fixValue);
    }

    private static Dynamic<?> fixValue(Dynamic<?> param0x) {
        return param0x.update(
            "Attributes",
            param1 -> param0x.createList(
                    param1.asStream()
                        .map(
                            param0xxx -> param0xxx.get("Name").asString("").equals("generic.follow_range") && param0xxx.get("Base").asDouble(0.0) == 16.0
                                    ? param0xxx.set("Base", param0xxx.createDouble(48.0))
                                    : param0xxx
                        )
                )
        );
    }
}
