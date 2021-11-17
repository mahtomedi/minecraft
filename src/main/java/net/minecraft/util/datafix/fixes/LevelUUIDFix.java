package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Supplier;

public class LevelUUIDFix extends AbstractUUIDFix {
    public LevelUUIDFix(Schema param0) {
        super(param0, References.LEVEL);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "LevelUUIDFix",
            this.getInputSchema().getType(this.typeReference),
            param0 -> param0.updateTyped(DSL.remainderFinder(), param0x -> param0x.update(DSL.remainderFinder(), param0xx -> {
                        param0xx = this.updateCustomBossEvents(param0xx);
                        param0xx = this.updateDragonFight(param0xx);
                        return this.updateWanderingTrader(param0xx);
                    }))
        );
    }

    private Dynamic<?> updateWanderingTrader(Dynamic<?> param0) {
        return replaceUUIDString(param0, "WanderingTraderId", "WanderingTraderId").orElse(param0);
    }

    private Dynamic<?> updateDragonFight(Dynamic<?> param0) {
        return param0.update(
            "DimensionData",
            param0x -> param0x.updateMapValues(
                    param0xx -> param0xx.mapSecond(
                            param0xxx -> param0xxx.update(
                                    "DragonFight", param0xxxx -> replaceUUIDLeastMost(param0xxxx, "DragonUUID", "Dragon").orElse(param0xxxx)
                                )
                        )
                )
        );
    }

    private Dynamic<?> updateCustomBossEvents(Dynamic<?> param0) {
        return param0.update(
            "CustomBossEvents",
            param0x -> param0x.updateMapValues(
                    param0xx -> param0xx.mapSecond(
                            param0xxx -> param0xxx.update(
                                    "Players",
                                    param1 -> param0xxx.createList(
                                            param1.asStream()
                                                .map(param0xxxxx -> createUUIDFromML(param0xxxxx).orElseGet((Supplier<? extends Dynamic<?>>)(() -> {
                                                        LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
                                                        return param0xxxxx;
                                                    })))
                                        )
                                )
                        )
                )
        );
    }
}
