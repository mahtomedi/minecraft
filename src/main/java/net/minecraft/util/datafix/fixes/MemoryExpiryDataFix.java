package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;

public class MemoryExpiryDataFix extends NamedEntityFix {
    public MemoryExpiryDataFix(Schema param0, String param1) {
        super(param0, false, "Memory expiry data fix (" + param1 + ")", References.ENTITY, param1);
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }

    public Dynamic<?> fixTag(Dynamic<?> param0x) {
        return param0x.update("Brain", this::updateBrain);
    }

    private Dynamic<?> updateBrain(Dynamic<?> param0x) {
        return param0x.update("memories", this::updateMemories);
    }

    private Dynamic<?> updateMemories(Dynamic<?> param0x) {
        return param0x.updateMapValues(this::updateMemoryEntry);
    }

    private Pair<Dynamic<?>, Dynamic<?>> updateMemoryEntry(Pair<Dynamic<?>, Dynamic<?>> param0x) {
        return param0x.mapSecond(this::wrapMemoryValue);
    }

    private Dynamic<?> wrapMemoryValue(Dynamic<?> param0x) {
        return param0x.createMap(ImmutableMap.of(param0x.createString("value"), param0x));
    }
}
