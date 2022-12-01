package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsAmbientOcclusionFix extends DataFix {
    public OptionsAmbientOcclusionFix(Schema param0) {
        super(param0, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsAmbientOcclusionFix",
            this.getInputSchema().getType(References.OPTIONS),
            param0 -> param0.update(
                    DSL.remainderFinder(),
                    param0x -> DataFixUtils.orElse(
                            param0x.get("ao").asString().map(param1 -> param0x.set("ao", param0x.createString(updateValue(param1)))).result(), param0x
                        )
                )
        );
    }

    private static String updateValue(String param0) {
        return switch(param0) {
            case "0" -> "false";
            case "1", "2" -> "true";
            default -> param0;
        };
    }
}
