package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityHorseSaddleFix extends NamedEntityFix {
    public EntityHorseSaddleFix(Schema param0, boolean param1) {
        super(param0, param1, "EntityHorseSaddleFix", References.ENTITY, "EntityHorse");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        OpticFinder<Pair<String, String>> var0 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        Type<?> var1 = this.getInputSchema().getTypeRaw(References.ITEM_STACK);
        OpticFinder<?> var2 = DSL.fieldFinder("SaddleItem", var1);
        Optional<? extends Typed<?>> var3 = param0.getOptionalTyped(var2);
        Dynamic<?> var4 = param0.get(DSL.remainderFinder());
        if (var3.isEmpty() && var4.get("Saddle").asBoolean(false)) {
            Typed<?> var5 = var1.pointTyped(param0.getOps()).orElseThrow(IllegalStateException::new);
            var5 = var5.set(var0, Pair.of(References.ITEM_NAME.typeName(), "minecraft:saddle"));
            Dynamic<?> var6 = var4.emptyMap();
            var6 = var6.set("Count", var6.createByte((byte)1));
            var6 = var6.set("Damage", var6.createShort((short)0));
            var5 = var5.set(DSL.remainderFinder(), var6);
            var4.remove("Saddle");
            return param0.set(var2, var5).set(DSL.remainderFinder(), var4);
        } else {
            return param0;
        }
    }
}
