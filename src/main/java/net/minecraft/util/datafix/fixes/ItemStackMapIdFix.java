package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;

public class ItemStackMapIdFix extends DataFix {
    public ItemStackMapIdFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), DSL.namespacedString()));
        OpticFinder<?> var2 = var0.findField("tag");
        return this.fixTypeEverywhereTyped("ItemInstanceMapIdFix", var0, param2 -> {
            Optional<Pair<String, String>> var0x = param2.getOptional(var1);
            if (var0x.isPresent() && Objects.equals(var0x.get().getSecond(), "minecraft:filled_map")) {
                Dynamic<?> var1x = param2.get(DSL.remainderFinder());
                Typed<?> var2x = param2.getOrCreateTyped(var2);
                Dynamic<?> var3x = var2x.get(DSL.remainderFinder());
                var3x = var3x.set("map", var3x.createInt(var1x.get("Damage").asInt(0)));
                return param2.set(var2, var2x.set(DSL.remainderFinder(), var3x));
            } else {
                return param2;
            }
        });
    }
}
