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
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class OminousBannerRenameFix extends DataFix {
    public OminousBannerRenameFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    private Dynamic<?> fixTag(Dynamic<?> param0) {
        Optional<? extends Dynamic<?>> var0 = param0.get("display").result();
        if (var0.isPresent()) {
            Dynamic<?> var1 = var0.get();
            Optional<String> var2 = var1.get("Name").asString().result();
            if (var2.isPresent()) {
                String var3 = var2.get();
                var3 = var3.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
                var1 = var1.set("Name", var1.createString(var3));
            }

            return param0.set("display", var1);
        } else {
            return param0;
        }
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> var2 = var0.findField("tag");
        return this.fixTypeEverywhereTyped("OminousBannerRenameFix", var0, param2 -> {
            Optional<Pair<String, String>> var0x = param2.getOptional(var1);
            if (var0x.isPresent() && Objects.equals(var0x.get().getSecond(), "minecraft:white_banner")) {
                Optional<? extends Typed<?>> var1x = param2.getOptionalTyped(var2);
                if (var1x.isPresent()) {
                    Typed<?> var2x = var1x.get();
                    Dynamic<?> var3x = var2x.get(DSL.remainderFinder());
                    return param2.set(var2, var2x.set(DSL.remainderFinder(), this.fixTag(var3x)));
                }
            }

            return param2;
        });
    }
}
