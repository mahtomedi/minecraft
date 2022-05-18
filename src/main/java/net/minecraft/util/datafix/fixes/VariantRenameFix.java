package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class VariantRenameFix extends NamedEntityFix {
    private final Map<String, String> renames;

    public VariantRenameFix(Schema param0, String param1, TypeReference param2, String param3, Map<String, String> param4) {
        super(param0, false, param1, param2, param3);
        this.renames = param4;
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(
            DSL.remainderFinder(),
            param0x -> param0x.update(
                    "variant",
                    param0xx -> DataFixUtils.orElse(
                            param0xx.asString().map(param1 -> param0xx.createString(this.renames.getOrDefault(param1, param1))).result(), param0xx
                        )
                )
        );
    }
}
