package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsAddTextBackgroundFix extends DataFix {
    public OptionsAddTextBackgroundFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsAddTextBackgroundFix",
            this.getInputSchema().getType(References.OPTIONS),
            param0 -> param0.update(
                    DSL.remainderFinder(),
                    param0x -> DataFixUtils.orElse(
                            param0x.get("chatOpacity")
                                .asString()
                                .map(param1 -> param0x.set("textBackgroundOpacity", param0x.createDouble(this.calculateBackground(param1)))),
                            param0x
                        )
                )
        );
    }

    private double calculateBackground(String param0) {
        try {
            double var0 = 0.9 * Double.parseDouble(param0) + 0.1;
            return var0 / 2.0;
        } catch (NumberFormatException var4) {
            return 0.5;
        }
    }
}
