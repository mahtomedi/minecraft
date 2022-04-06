package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;

public class CriteriaRenameFix extends DataFix {
    private final String name;
    private final String advancementId;
    private final UnaryOperator<String> conversions;

    public CriteriaRenameFix(Schema param0, String param1, String param2, UnaryOperator<String> param3) {
        super(param0, false);
        this.name = param1;
        this.advancementId = param2;
        this.conversions = param3;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            this.name, this.getInputSchema().getType(References.ADVANCEMENTS), param0 -> param0.update(DSL.remainderFinder(), this::fixAdvancements)
        );
    }

    private Dynamic<?> fixAdvancements(Dynamic<?> param0) {
        return param0.update(
            this.advancementId,
            param0x -> param0x.update(
                    "criteria",
                    param0xx -> param0xx.updateMapValues(
                            param0xxx -> param0xxx.mapFirst(
                                    param0xxxx -> DataFixUtils.orElse(
                                            param0xxxx.asString().map(param1 -> param0xxxx.createString(this.conversions.apply(param1))).result(), param0xxxx
                                        )
                                )
                        )
                )
        );
    }
}
