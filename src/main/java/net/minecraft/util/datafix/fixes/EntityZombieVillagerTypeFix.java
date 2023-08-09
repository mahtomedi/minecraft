package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.RandomSource;

public class EntityZombieVillagerTypeFix extends NamedEntityFix {
    private static final int PROFESSION_MAX = 6;
    private static final RandomSource RANDOM = RandomSource.create();

    public EntityZombieVillagerTypeFix(Schema param0, boolean param1) {
        super(param0, param1, "EntityZombieVillagerTypeFix", References.ENTITY, "Zombie");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        if (param0.get("IsVillager").asBoolean(false)) {
            if (param0.get("ZombieType").result().isEmpty()) {
                int var0 = this.getVillagerProfession(param0.get("VillagerProfession").asInt(-1));
                if (var0 == -1) {
                    var0 = this.getVillagerProfession(RANDOM.nextInt(6));
                }

                param0 = param0.set("ZombieType", param0.createInt(var0));
            }

            param0 = param0.remove("IsVillager");
        }

        return param0;
    }

    private int getVillagerProfession(int param0) {
        return param0 >= 0 && param0 < 6 ? param0 : -1;
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }
}
