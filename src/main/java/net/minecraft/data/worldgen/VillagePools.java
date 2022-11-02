package net.minecraft.data.worldgen;

import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class VillagePools {
    public static void bootstrap(BootstapContext<StructureTemplatePool> param0) {
        PlainVillagePools.bootstrap(param0);
        SnowyVillagePools.bootstrap(param0);
        SavannaVillagePools.bootstrap(param0);
        DesertVillagePools.bootstrap(param0);
        TaigaVillagePools.bootstrap(param0);
    }
}
