package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class BlockEntityJukeboxFix extends NamedEntityFix {
    public BlockEntityJukeboxFix(Schema param0, boolean param1) {
        super(param0, param1, "BlockEntityJukeboxFix", References.BLOCK_ENTITY, "minecraft:jukebox");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        Type<?> var0 = this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:jukebox");
        Type<?> var1 = var0.findFieldType("RecordItem");
        OpticFinder<?> var2 = DSL.fieldFinder("RecordItem", var1);
        Dynamic<?> var3 = param0.get(DSL.remainderFinder());
        int var4 = var3.get("Record").asInt(0);
        if (var4 > 0) {
            var3.remove("Record");
            String var5 = ItemStackTheFlatteningFix.updateItem(ItemIdFix.getItem(var4), 0);
            if (var5 != null) {
                Dynamic<?> var6 = var3.emptyMap();
                var6 = var6.set("id", var6.createString(var5));
                var6 = var6.set("Count", var6.createByte((byte)1));
                return param0.set(var2, var1.readTyped(var6).getSecond().orElseThrow(() -> new IllegalStateException("Could not create record item stack.")))
                    .set(DSL.remainderFinder(), var3);
            }
        }

        return param0;
    }
}
