package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;

public class EntityZombieSplitFix extends SimpleEntityRenameFix {
    public EntityZombieSplitFix(Schema param0, boolean param1) {
        super("EntityZombieSplitFix", param0, param1);
    }

    @Override
    protected Pair<String, Dynamic<?>> getNewNameAndTag(String param0, Dynamic<?> param1) {
        if (Objects.equals("Zombie", param0)) {
            String var0 = "Zombie";
            int var1 = param1.get("ZombieType").asInt(0);
            switch(var1) {
                case 0:
                default:
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    var0 = "ZombieVillager";
                    param1 = param1.set("Profession", param1.createInt(var1 - 1));
                    break;
                case 6:
                    var0 = "Husk";
            }

            param1 = param1.remove("ZombieType");
            return Pair.of(var0, param1);
        } else {
            return Pair.of(param0, param1);
        }
    }
}
