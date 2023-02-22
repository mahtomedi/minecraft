package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class IglooMetadataRemovalFix extends DataFix {
    public IglooMetadataRemovalFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
        return this.fixTypeEverywhereTyped("IglooMetadataRemovalFix", var0, param0 -> param0.update(DSL.remainderFinder(), IglooMetadataRemovalFix::fixTag));
    }

    private static <T> Dynamic<T> fixTag(Dynamic<T> param0) {
        boolean var0 = param0.get("Children").asStreamOpt().map(param0x -> param0x.allMatch(IglooMetadataRemovalFix::isIglooPiece)).result().orElse(false);
        return var0 ? param0.set("id", param0.createString("Igloo")).remove("Children") : param0.update("Children", IglooMetadataRemovalFix::removeIglooPieces);
    }

    private static <T> Dynamic<T> removeIglooPieces(Dynamic<T> param0x) {
        return param0x.asStreamOpt().map(param0xx -> param0xx.filter(param0xxx -> !isIglooPiece(param0xxx))).map(param0x::createList).result().orElse(param0x);
    }

    private static boolean isIglooPiece(Dynamic<?> param0) {
        return param0.get("id").asString("").equals("Iglu");
    }
}
