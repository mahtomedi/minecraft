package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class PlayerUUIDFix extends AbstractUUIDFix {
    public PlayerUUIDFix(Schema param0) {
        super(param0, References.PLAYER);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "PlayerUUIDFix",
            this.getInputSchema().getType(this.typeReference),
            param0 -> {
                OpticFinder<?> var0 = param0.getType().findField("RootVehicle");
                return param0.updateTyped(
                        var0,
                        var0.type(),
                        param0x -> param0x.update(DSL.remainderFinder(), param0xx -> replaceUUIDLeastMost(param0xx, "Attach", "Attach").orElse(param0xx))
                    )
                    .update(DSL.remainderFinder(), param0x -> EntityUUIDFix.updateEntityUUID(EntityUUIDFix.updateLivingEntity(param0x)));
            }
        );
    }
}
