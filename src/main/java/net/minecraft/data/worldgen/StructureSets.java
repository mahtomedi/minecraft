package net.minecraft.data.worldgen;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public interface StructureSets {
    static void bootstrap(BootstapContext<StructureSet> param0) {
        HolderGetter<Structure> var0 = param0.lookup(Registries.STRUCTURE);
        HolderGetter<Biome> var1 = param0.lookup(Registries.BIOME);
        Holder.Reference<StructureSet> var2 = param0.register(
            BuiltinStructureSets.VILLAGES,
            new StructureSet(
                List.of(
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.VILLAGE_PLAINS)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.VILLAGE_DESERT)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.VILLAGE_SAVANNA)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.VILLAGE_SNOWY)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.VILLAGE_TAIGA))
                ),
                new RandomSpreadStructurePlacement(34, 8, RandomSpreadType.LINEAR, 10387312)
            )
        );
        param0.register(
            BuiltinStructureSets.DESERT_PYRAMIDS,
            new StructureSet(var0.getOrThrow(BuiltinStructures.DESERT_PYRAMID), new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357617))
        );
        param0.register(
            BuiltinStructureSets.IGLOOS,
            new StructureSet(var0.getOrThrow(BuiltinStructures.IGLOO), new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357618))
        );
        param0.register(
            BuiltinStructureSets.JUNGLE_TEMPLES,
            new StructureSet(var0.getOrThrow(BuiltinStructures.JUNGLE_TEMPLE), new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357619))
        );
        param0.register(
            BuiltinStructureSets.SWAMP_HUTS,
            new StructureSet(var0.getOrThrow(BuiltinStructures.SWAMP_HUT), new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357620))
        );
        param0.register(
            BuiltinStructureSets.PILLAGER_OUTPOSTS,
            new StructureSet(
                var0.getOrThrow(BuiltinStructures.PILLAGER_OUTPOST),
                new RandomSpreadStructurePlacement(
                    Vec3i.ZERO,
                    StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_1,
                    0.2F,
                    165745296,
                    Optional.of(new StructurePlacement.ExclusionZone(var2, 10)),
                    32,
                    8,
                    RandomSpreadType.LINEAR
                )
            )
        );
        param0.register(
            BuiltinStructureSets.ANCIENT_CITIES,
            new StructureSet(var0.getOrThrow(BuiltinStructures.ANCIENT_CITY), new RandomSpreadStructurePlacement(24, 8, RandomSpreadType.LINEAR, 20083232))
        );
        param0.register(
            BuiltinStructureSets.OCEAN_MONUMENTS,
            new StructureSet(
                var0.getOrThrow(BuiltinStructures.OCEAN_MONUMENT), new RandomSpreadStructurePlacement(32, 5, RandomSpreadType.TRIANGULAR, 10387313)
            )
        );
        param0.register(
            BuiltinStructureSets.WOODLAND_MANSIONS,
            new StructureSet(
                var0.getOrThrow(BuiltinStructures.WOODLAND_MANSION), new RandomSpreadStructurePlacement(80, 20, RandomSpreadType.TRIANGULAR, 10387319)
            )
        );
        param0.register(
            BuiltinStructureSets.BURIED_TREASURES,
            new StructureSet(
                var0.getOrThrow(BuiltinStructures.BURIED_TREASURE),
                new RandomSpreadStructurePlacement(
                    new Vec3i(9, 0, 9), StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_2, 0.01F, 0, Optional.empty(), 1, 0, RandomSpreadType.LINEAR
                )
            )
        );
        param0.register(
            BuiltinStructureSets.MINESHAFTS,
            new StructureSet(
                List.of(StructureSet.entry(var0.getOrThrow(BuiltinStructures.MINESHAFT)), StructureSet.entry(var0.getOrThrow(BuiltinStructures.MINESHAFT_MESA))),
                new RandomSpreadStructurePlacement(
                    Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_3, 0.004F, 0, Optional.empty(), 1, 0, RandomSpreadType.LINEAR
                )
            )
        );
        param0.register(
            BuiltinStructureSets.RUINED_PORTALS,
            new StructureSet(
                List.of(
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.RUINED_PORTAL_STANDARD)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.RUINED_PORTAL_DESERT)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.RUINED_PORTAL_JUNGLE)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.RUINED_PORTAL_SWAMP)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.RUINED_PORTAL_MOUNTAIN)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.RUINED_PORTAL_OCEAN)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.RUINED_PORTAL_NETHER))
                ),
                new RandomSpreadStructurePlacement(40, 15, RandomSpreadType.LINEAR, 34222645)
            )
        );
        param0.register(
            BuiltinStructureSets.SHIPWRECKS,
            new StructureSet(
                List.of(
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.SHIPWRECK)), StructureSet.entry(var0.getOrThrow(BuiltinStructures.SHIPWRECK_BEACHED))
                ),
                new RandomSpreadStructurePlacement(24, 4, RandomSpreadType.LINEAR, 165745295)
            )
        );
        param0.register(
            BuiltinStructureSets.OCEAN_RUINS,
            new StructureSet(
                List.of(
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.OCEAN_RUIN_COLD)),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.OCEAN_RUIN_WARM))
                ),
                new RandomSpreadStructurePlacement(20, 8, RandomSpreadType.LINEAR, 14357621)
            )
        );
        param0.register(
            BuiltinStructureSets.NETHER_COMPLEXES,
            new StructureSet(
                List.of(
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.FORTRESS), 2),
                    StructureSet.entry(var0.getOrThrow(BuiltinStructures.BASTION_REMNANT), 3)
                ),
                new RandomSpreadStructurePlacement(27, 4, RandomSpreadType.LINEAR, 30084232)
            )
        );
        param0.register(
            BuiltinStructureSets.NETHER_FOSSILS,
            new StructureSet(var0.getOrThrow(BuiltinStructures.NETHER_FOSSIL), new RandomSpreadStructurePlacement(2, 1, RandomSpreadType.LINEAR, 14357921))
        );
        param0.register(
            BuiltinStructureSets.END_CITIES,
            new StructureSet(var0.getOrThrow(BuiltinStructures.END_CITY), new RandomSpreadStructurePlacement(20, 11, RandomSpreadType.TRIANGULAR, 10387313))
        );
        param0.register(
            BuiltinStructureSets.STRONGHOLDS,
            new StructureSet(
                var0.getOrThrow(BuiltinStructures.STRONGHOLD),
                new ConcentricRingsStructurePlacement(32, 3, 128, var1.getOrThrow(BiomeTags.STRONGHOLD_BIASED_TO))
            )
        );
    }
}
