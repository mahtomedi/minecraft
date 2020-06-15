package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;

public class EntityRedundantChanceTagsFix extends DataFix {
    private static final Codec<List<Float>> FLOAT_LIST_CODEC = Codec.FLOAT.listOf();

    public EntityRedundantChanceTagsFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "EntityRedundantChanceTagsFix", this.getInputSchema().getType(References.ENTITY), param0 -> param0.update(DSL.remainderFinder(), param0x -> {
                    if (isZeroList(param0x.get("HandDropChances"), 2)) {
                        param0x = param0x.remove("HandDropChances");
                    }
    
                    if (isZeroList(param0x.get("ArmorDropChances"), 4)) {
                        param0x = param0x.remove("ArmorDropChances");
                    }
    
                    return param0x;
                })
        );
    }

    private static boolean isZeroList(OptionalDynamic<?> param0, int param1) {
        return param0.flatMap(FLOAT_LIST_CODEC::parse)
            .map(param1x -> param1x.size() == param1 && param1x.stream().allMatch(param0x -> param0x == 0.0F))
            .result()
            .orElse(false);
    }
}
