package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;

public class RemapChunkStatusFix extends DataFix {
    private final String name;
    private final UnaryOperator<String> mapper;

    public RemapChunkStatusFix(Schema param0, String param1, UnaryOperator<String> param2) {
        super(param0, false);
        this.name = param1;
        this.mapper = param2;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            this.name,
            this.getInputSchema().getType(References.CHUNK),
            param0 -> param0.update(
                    DSL.remainderFinder(),
                    param0x -> param0x.update("Status", this::fixStatus)
                            .update("below_zero_retrogen", param0xx -> param0xx.update("target_status", this::fixStatus))
                )
        );
    }

    private <T> Dynamic<T> fixStatus(Dynamic<T> param0) {
        return DataFixUtils.orElse(param0.asString().result().map(this.mapper).map(param0::createString), param0);
    }
}
