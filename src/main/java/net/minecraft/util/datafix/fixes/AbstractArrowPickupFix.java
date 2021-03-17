package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;

public class AbstractArrowPickupFix extends DataFix {
    public AbstractArrowPickupFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema var0 = this.getInputSchema();
        return this.fixTypeEverywhereTyped("AbstractArrowPickupFix", var0.getType(References.ENTITY), this::updateProjectiles);
    }

    private Typed<?> updateProjectiles(Typed<?> param0) {
        param0 = this.updateEntity(param0, "minecraft:arrow", AbstractArrowPickupFix::updatePickup);
        param0 = this.updateEntity(param0, "minecraft:spectral_arrow", AbstractArrowPickupFix::updatePickup);
        return this.updateEntity(param0, "minecraft:trident", AbstractArrowPickupFix::updatePickup);
    }

    private static Dynamic<?> updatePickup(Dynamic<?> param0x) {
        if (param0x.get("pickup").result().isPresent()) {
            return param0x;
        } else {
            boolean var0x = param0x.get("player").asBoolean(true);
            return param0x.set("pickup", param0x.createByte((byte)(var0x ? 1 : 0))).remove("player");
        }
    }

    private Typed<?> updateEntity(Typed<?> param0, String param1, Function<Dynamic<?>, Dynamic<?>> param2) {
        Type<?> var0 = this.getInputSchema().getChoiceType(References.ENTITY, param1);
        Type<?> var1 = this.getOutputSchema().getChoiceType(References.ENTITY, param1);
        return param0.updateTyped(DSL.namedChoice(param1, var0), var1, param1x -> param1x.update(DSL.remainderFinder(), param2));
    }
}
