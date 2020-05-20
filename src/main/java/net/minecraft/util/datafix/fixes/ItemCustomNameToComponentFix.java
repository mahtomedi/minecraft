package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ItemCustomNameToComponentFix extends DataFix {
    public ItemCustomNameToComponentFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    private Dynamic<?> fixTag(Dynamic<?> param0) {
        Optional<? extends Dynamic<?>> var0 = param0.get("display").result();
        if (var0.isPresent()) {
            Dynamic<?> var1 = var0.get();
            Optional<String> var2 = var1.get("Name").asString().result();
            if (var2.isPresent()) {
                var1 = var1.set("Name", var1.createString(Component.Serializer.toJson(new TextComponent(var2.get()))));
            } else {
                Optional<String> var3 = var1.get("LocName").asString().result();
                if (var3.isPresent()) {
                    var1 = var1.set("Name", var1.createString(Component.Serializer.toJson(new TranslatableComponent(var3.get()))));
                    var1 = var1.remove("LocName");
                }
            }

            return param0.set("display", var1);
        } else {
            return param0;
        }
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> var1 = var0.findField("tag");
        return this.fixTypeEverywhereTyped(
            "ItemCustomNameToComponentFix", var0, param1 -> param1.updateTyped(var1, param0x -> param0x.update(DSL.remainderFinder(), this::fixTag))
        );
    }
}
