package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BedItemColorFix extends DataFix {
    public BedItemColorFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<Pair<String, String>> var0 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        return this.fixTypeEverywhereTyped("BedItemColorFix", this.getInputSchema().getType(References.ITEM_STACK), param1 -> {
            Optional<Pair<String, String>> var0x = param1.getOptional(var0);
            if (var0x.isPresent() && Objects.equals(var0x.get().getSecond(), "minecraft:bed")) {
                Dynamic<?> var1x = param1.get(DSL.remainderFinder());
                if (var1x.get("Damage").asInt(0) == 0) {
                    return param1.set(DSL.remainderFinder(), var1x.set("Damage", var1x.createShort((short)14)));
                }
            }

            return param1;
        });
    }
}
