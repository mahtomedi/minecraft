package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemWaterPotionFix extends DataFix {
    public ItemWaterPotionFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> var2 = var0.findField("tag");
        return this.fixTypeEverywhereTyped(
            "ItemWaterPotionFix",
            var0,
            param2 -> {
                Optional<Pair<String, String>> var0x = param2.getOptional(var1);
                if (var0x.isPresent()) {
                    String var1x = var0x.get().getSecond();
                    if ("minecraft:potion".equals(var1x)
                        || "minecraft:splash_potion".equals(var1x)
                        || "minecraft:lingering_potion".equals(var1x)
                        || "minecraft:tipped_arrow".equals(var1x)) {
                        Typed<?> var2x = param2.getOrCreateTyped(var2);
                        Dynamic<?> var3x = var2x.get(DSL.remainderFinder());
                        if (var3x.get("Potion").asString().result().isEmpty()) {
                            var3x = var3x.set("Potion", var3x.createString("minecraft:water"));
                        }
    
                        return param2.set(var2, var2x.set(DSL.remainderFinder(), var3x));
                    }
                }
    
                return param2;
            }
        );
    }
}
