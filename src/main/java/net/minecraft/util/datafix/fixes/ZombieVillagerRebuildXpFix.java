package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;

public class ZombieVillagerRebuildXpFix extends NamedEntityFix {
    public ZombieVillagerRebuildXpFix(Schema param0, boolean param1) {
        super(param0, param1, "Zombie Villager XP rebuild", References.ENTITY, "minecraft:zombie_villager");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), param0x -> {
            Optional<Number> var0 = param0x.get("Xp").asNumber();
            if (!var0.isPresent()) {
                int var1x = param0x.get("VillagerData").get("level").asNumber().orElse(1).intValue();
                return param0x.set("Xp", param0x.createInt(VillagerRebuildLevelAndXpFix.getMinXpPerLevel(var1x)));
            } else {
                return param0x;
            }
        });
    }
}
