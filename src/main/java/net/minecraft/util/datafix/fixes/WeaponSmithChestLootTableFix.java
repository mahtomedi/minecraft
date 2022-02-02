package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class WeaponSmithChestLootTableFix extends NamedEntityFix {
    public WeaponSmithChestLootTableFix(Schema param0, boolean param1) {
        super(param0, param1, "WeaponSmithChestLootTableFix", References.BLOCK_ENTITY, "minecraft:chest");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(
            DSL.remainderFinder(),
            param0x -> {
                String var0 = param0x.get("LootTable").asString("");
                return var0.equals("minecraft:chests/village_blacksmith")
                    ? param0x.set("LootTable", param0x.createString("minecraft:chests/village/village_weaponsmith"))
                    : param0x;
            }
        );
    }
}
