package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import java.util.stream.Collectors;

public class OptionsKeyTranslationFix extends DataFix {
    public OptionsKeyTranslationFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsKeyTranslationFix",
            this.getInputSchema().getType(References.OPTIONS),
            param0 -> param0.update(
                    DSL.remainderFinder(), param0x -> param0x.getMapValues().map(param1 -> param0x.createMap(param1.entrySet().stream().map(param1x -> {
                                if (param1x.getKey().asString("").startsWith("key_")) {
                                    String var0x = param1x.getValue().asString("");
                                    if (!var0x.startsWith("key.mouse") && !var0x.startsWith("scancode.")) {
                                        return Pair.of(param1x.getKey(), param0x.createString("key.keyboard." + var0x.substring("key.".length())));
                                    }
                                }
        
                                return Pair.of(param1x.getKey(), param1x.getValue());
                            }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))).result().orElse(param0x)
                )
        );
    }
}
