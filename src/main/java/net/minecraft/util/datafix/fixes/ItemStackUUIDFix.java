package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
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
        return this.fixTypeEverywhereTyped("ItemStackUUIDFix", this.getInputSchema().getType(this.typeReference), param1 -> {
            OpticFinder<?> var0x = param1.getType().findField("tag");
            return param1.updateTyped(var0x, param2 -> param2.update(DSL.remainderFinder(), param2x -> {
                    param2x = this.updateAttributeModifiers(param2x);
                    if (param1.getOptional(var0).map(param0x -> "minecraft:player_head".equals(param0x.getSecond())).orElse(false)) {
                        param2x = this.updateSkullOwner(param2x);
                    }

                    return param2x;
                }));
        });
    }

    private Dynamic<?> updateAttributeModifiers(Dynamic<?> param0) {
        return param0.update(
            "AttributeModifiers", param1 -> param0.createList(param1.asStream().map(param0x -> replaceUUIDLeastMost(param0x, "UUID", "UUID").orElse(param0x)))
        );
    }

    private Dynamic<?> updateSkullOwner(Dynamic<?> param0) {
        return param0.update("SkullOwner", param0x -> replaceUUIDString(param0x, "Id", "Id").orElse(param0x));
    }
}
