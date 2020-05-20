package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityHorseSplitFix extends EntityRenameFix {
    public EntityHorseSplitFix(Schema param0, boolean param1) {
        super("EntityHorseSplitFix", param0, param1);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String param0, Typed<?> param1) {
        Dynamic<?> var0 = param1.get(DSL.remainderFinder());
        if (Objects.equals("EntityHorse", param0)) {
            int var1 = var0.get("Type").asInt(0);
            String var2;
            switch(var1) {
                case 0:
                default:
                    var2 = "Horse";
                    break;
                case 1:
                    var2 = "Donkey";
                    break;
                case 2:
                    var2 = "Mule";
                    break;
                case 3:
                    var2 = "ZombieHorse";
                    break;
                case 4:
                    var2 = "SkeletonHorse";
            }

            var0.remove("Type");
            Type<?> var7 = this.getOutputSchema().findChoiceType(References.ENTITY).types().get(var2);
            return Pair.of(
                var2,
                (Typed<?>)((Pair)param1.write().flatMap(var7::readTyped).result().orElseThrow(() -> new IllegalStateException("Could not parse the new horse")))
                    .getFirst()
            );
        } else {
            return Pair.of(param0, param1);
        }
    }
}
