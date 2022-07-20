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
import java.util.Set;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemRemoveBlockEntityTagFix extends DataFix {
    private final Set<String> items;

    public ItemRemoveBlockEntityTagFix(Schema param0, boolean param1, Set<String> param2) {
        super(param0, param1);
        this.items = param2;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> var2 = var0.findField("tag");
        OpticFinder<?> var3 = var2.type().findField("BlockEntityTag");
        return this.fixTypeEverywhereTyped("ItemRemoveBlockEntityTagFix", var0, param3 -> {
            Optional<Pair<String, String>> var0x = param3.getOptional(var1);
            if (var0x.isPresent() && this.items.contains(var0x.get().getSecond())) {
                Optional<? extends Typed<?>> var1x = param3.getOptionalTyped(var2);
                if (var1x.isPresent()) {
                    Typed<?> var2x = var1x.get();
                    Optional<? extends Typed<?>> var3x = var2x.getOptionalTyped(var3);
                    if (var3x.isPresent()) {
                        Optional<? extends Dynamic<?>> var4x = var2x.write().result();
                        Dynamic<?> var5 = var4x.isPresent() ? (Dynamic)var4x.get() : var2x.get(DSL.remainderFinder());
                        Dynamic<?> var6 = var5.remove("BlockEntityTag");
                        Optional<? extends Pair<? extends Typed<?>, ?>> var7 = var2.type().readTyped(var6).result();
                        if (var7.isEmpty()) {
                            return param3;
                        }

                        return param3.set(var2, var7.get().getFirst());
                    }
                }
            }

            return param3;
        });
    }
}
