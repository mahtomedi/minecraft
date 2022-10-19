package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class OptionsProgrammerArtFix extends DataFix {
    public OptionsProgrammerArtFix(Schema param0) {
        super(param0, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsProgrammerArtFix",
            this.getInputSchema().getType(References.OPTIONS),
            param0 -> param0.update(
                    DSL.remainderFinder(), param0x -> param0x.update("resourcePacks", this::fixList).update("incompatibleResourcePacks", this::fixList)
                )
        );
    }

    private <T> Dynamic<T> fixList(Dynamic<T> param0) {
        return param0.asString().result().map(param1 -> param0.createString(param1.replace("\"programer_art\"", "\"programmer_art\""))).orElse(param0);
    }
}
