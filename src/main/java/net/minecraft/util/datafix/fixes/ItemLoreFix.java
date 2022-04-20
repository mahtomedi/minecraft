package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

public class ItemLoreFix extends DataFix {
    public ItemLoreFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> var1 = var0.findField("tag");
        return this.fixTypeEverywhereTyped(
            "Item Lore componentize",
            var0,
            param1 -> param1.updateTyped(
                    var1,
                    param0x -> param0x.update(
                            DSL.remainderFinder(),
                            param0xx -> param0xx.update(
                                    "display",
                                    param0xxx -> param0xxx.update(
                                            "Lore",
                                            param0xxxx -> DataFixUtils.orElse(
                                                    param0xxxx.asStreamOpt().map(ItemLoreFix::fixLoreList).map(param0xxxx::createList).result(), param0xxxx
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static <T> Stream<Dynamic<T>> fixLoreList(Stream<Dynamic<T>> param0) {
        return param0.map(param0x -> DataFixUtils.orElse(param0x.asString().map(ItemLoreFix::fixLoreEntry).map(param0x::createString).result(), param0x));
    }

    private static String fixLoreEntry(String param0) {
        return Component.Serializer.toJson(Component.literal(param0));
    }
}
