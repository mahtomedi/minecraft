package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlockNameFlatteningFix extends DataFix {
    public BlockNameFlatteningFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.BLOCK_NAME);
        Type<?> var1 = this.getOutputSchema().getType(References.BLOCK_NAME);
        Type<Pair<String, Either<Integer, String>>> var2 = DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), DSL.namespacedString()));
        Type<Pair<String, String>> var3 = DSL.named(References.BLOCK_NAME.typeName(), DSL.namespacedString());
        if (Objects.equals(var0, var2) && Objects.equals(var1, var3)) {
            return this.fixTypeEverywhere(
                "BlockNameFlatteningFix",
                var2,
                var3,
                param0 -> param0x -> param0x.mapSecond(
                            param0xx -> param0xx.map(
                                    BlockStateData::upgradeBlock, param0xxx -> BlockStateData.upgradeBlock(NamespacedSchema.ensureNamespaced(param0xxx))
                                )
                        )
            );
        } else {
            throw new IllegalStateException("Expected and actual types don't match.");
        }
    }
}
