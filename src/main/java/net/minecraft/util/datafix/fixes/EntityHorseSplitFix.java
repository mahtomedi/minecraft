package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.Util;

public class EntityHorseSplitFix extends EntityRenameFix {
    public EntityHorseSplitFix(Schema param0, boolean param1) {
        super("EntityHorseSplitFix", param0, param1);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String param0, Typed<?> param1) {
        Dynamic<?> var0 = param1.get(DSL.remainderFinder());
        if (Objects.equals("EntityHorse", param0)) {
            int var1 = var0.get("Type").asInt(0);

            String var2 = switch(var1) {
                case 1 -> "Donkey";
                case 2 -> "Mule";
                case 3 -> "ZombieHorse";
                case 4 -> "SkeletonHorse";
                default -> "Horse";
            };
            var0.remove("Type");
            Type<?> var3 = this.getOutputSchema().findChoiceType(References.ENTITY).types().get(var2);
            return Pair.of(var2, Util.writeAndReadTypedOrThrow(param1, var3, param0x -> param0x));
        } else {
            return Pair.of(param0, param1);
        }
    }
}
