package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class GossipUUIDFix extends NamedEntityFix {
    public GossipUUIDFix(Schema param0, String param1) {
        super(param0, false, "Gossip for for " + param1, References.ENTITY, param1);
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(
            DSL.remainderFinder(),
            param0x -> param0x.update(
                    "Gossips",
                    param0xx -> DataFixUtils.orElse(
                            param0xx.asStreamOpt()
                                .result()
                                .map(
                                    param0xxx -> param0xxx.map(
                                            param0xxxx -> AbstractUUIDFix.replaceUUIDLeastMost(param0xxxx, "Target", "Target").orElse(param0xxxx)
                                        )
                                )
                                .map(param0xx::createList),
                            param0xx
                        )
                )
        );
    }
}
