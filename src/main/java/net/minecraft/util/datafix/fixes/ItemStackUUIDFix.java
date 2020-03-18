package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;

public class ItemStackUUIDFix extends AbstractUUIDFix {
    public ItemStackUUIDFix(Schema param0) {
        super(param0, References.ITEM_STACK);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<Pair<String, String>> var0 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), DSL.namespacedString()));
        return this.fixTypeEverywhereTyped(
            "ItemStackUUIDFix",
            this.getInputSchema().getType(this.typeReference),
            param1 -> {
                if (param1.getOptional(var0).map(param0x -> "minecraft:player_head".equals(param0x.getSecond())).orElse(false)) {
                    OpticFinder<?> var0x = param1.getType().findField("tag");
                    return param1.updateTyped(
                        var0x,
                        param0x -> param0x.update(
                                DSL.remainderFinder(),
                                param0xx -> param0xx.update("SkullOwner", param0xxx -> replaceUUIDString(param0xxx, "Id", "Id").orElse(param0xxx))
                            )
                    );
                } else {
                    return param1;
                }
            }
        );
    }
}
