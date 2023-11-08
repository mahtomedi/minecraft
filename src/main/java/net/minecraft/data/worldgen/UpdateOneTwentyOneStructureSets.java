package net.minecraft.data.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public interface UpdateOneTwentyOneStructureSets {
    static void bootstrap(BootstapContext<StructureSet> param0) {
        HolderGetter<Structure> var0 = param0.lookup(Registries.STRUCTURE);
        param0.register(
            BuiltinStructureSets.TRIAL_CHAMBERS,
            new StructureSet(var0.getOrThrow(BuiltinStructures.TRIAL_CHAMBERS), new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 94251327))
        );
    }
}
