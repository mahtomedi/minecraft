package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class BlockEntityBlockStateFix extends NamedEntityFix {
    public BlockEntityBlockStateFix(Schema param0, boolean param1) {
        super(param0, param1, "BlockEntityBlockStateFix", References.BLOCK_ENTITY, "minecraft:piston");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        Type<?> var0 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:piston");
        Type<?> var1 = var0.findFieldType("blockState");
        OpticFinder<?> var2 = DSL.fieldFinder("blockState", var1);
        Dynamic<?> var3 = param0.get(DSL.remainderFinder());
        int var4 = var3.get("blockId").asInt(0);
        var3 = var3.remove("blockId");
        int var5 = var3.get("blockData").asInt(0) & 15;
        var3 = var3.remove("blockData");
        Dynamic<?> var6 = BlockStateData.getTag(var4 << 4 | var5);
        Typed<?> var7 = var0.pointTyped(param0.getOps()).orElseThrow(() -> new IllegalStateException("Could not create new piston block entity."));
        return var7.set(DSL.remainderFinder(), var3)
            .set(var2, var1.readTyped(var6).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created block state tag.")).getFirst());
    }
}
