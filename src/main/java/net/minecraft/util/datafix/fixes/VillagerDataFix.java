package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class VillagerDataFix extends NamedEntityFix {
    public VillagerDataFix(Schema param0, String param1) {
        super(param0, false, "Villager profession data fix (" + param1 + ")", References.ENTITY, param1);
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        Dynamic<?> var0 = param0.get(DSL.remainderFinder());
        return param0.set(
            DSL.remainderFinder(),
            var0.remove("Profession")
                .remove("Career")
                .remove("CareerLevel")
                .set(
                    "VillagerData",
                    var0.createMap(
                        ImmutableMap.of(
                            var0.createString("type"),
                            var0.createString("minecraft:plains"),
                            var0.createString("profession"),
                            var0.createString(upgradeData(var0.get("Profession").asInt(0), var0.get("Career").asInt(0))),
                            var0.createString("level"),
                            DataFixUtils.orElse(var0.get("CareerLevel").result(), var0.createInt(1))
                        )
                    )
                )
        );
    }

    private static String upgradeData(int param0, int param1) {
        if (param0 == 0) {
            if (param1 == 2) {
                return "minecraft:fisherman";
            } else if (param1 == 3) {
                return "minecraft:shepherd";
            } else {
                return param1 == 4 ? "minecraft:fletcher" : "minecraft:farmer";
            }
        } else if (param0 == 1) {
            return param1 == 2 ? "minecraft:cartographer" : "minecraft:librarian";
        } else if (param0 == 2) {
            return "minecraft:cleric";
        } else if (param0 == 3) {
            if (param1 == 2) {
                return "minecraft:weaponsmith";
            } else {
                return param1 == 3 ? "minecraft:toolsmith" : "minecraft:armorer";
            }
        } else if (param0 == 4) {
            return param1 == 2 ? "minecraft:leatherworker" : "minecraft:butcher";
        } else {
            return param0 == 5 ? "minecraft:nitwit" : "minecraft:none";
        }
    }
}
