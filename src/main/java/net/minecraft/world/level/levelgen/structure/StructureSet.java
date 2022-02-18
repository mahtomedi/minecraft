package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public record StructureSet(List<StructureSet.StructureSelectionEntry> structures, StructurePlacement placement) {
    public static final Codec<StructureSet> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    StructureSet.StructureSelectionEntry.CODEC.listOf().fieldOf("structures").forGetter(StructureSet::structures),
                    StructurePlacement.CODEC.fieldOf("placement").forGetter(StructureSet::placement)
                )
                .apply(param0, StructureSet::new)
    );
    public static final Codec<Holder<StructureSet>> CODEC = RegistryFileCodec.create(Registry.STRUCTURE_SET_REGISTRY, DIRECT_CODEC);

    public StructureSet(Holder<ConfiguredStructureFeature<?, ?>> param0, StructurePlacement param1) {
        this(List.of(new StructureSet.StructureSelectionEntry(param0, 1)), param1);
    }

    public static StructureSet.StructureSelectionEntry entry(Holder<ConfiguredStructureFeature<?, ?>> param0, int param1) {
        return new StructureSet.StructureSelectionEntry(param0, param1);
    }

    public static StructureSet.StructureSelectionEntry entry(Holder<ConfiguredStructureFeature<?, ?>> param0) {
        return new StructureSet.StructureSelectionEntry(param0, 1);
    }

    public static record StructureSelectionEntry(Holder<ConfiguredStructureFeature<?, ?>> structure, int weight) {
        public static final Codec<StructureSet.StructureSelectionEntry> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ConfiguredStructureFeature.CODEC.fieldOf("structure").forGetter(StructureSet.StructureSelectionEntry::structure),
                        ExtraCodecs.POSITIVE_INT.fieldOf("weight").forGetter(StructureSet.StructureSelectionEntry::weight)
                    )
                    .apply(param0, StructureSet.StructureSelectionEntry::new)
        );

        public boolean generatesInMatchingBiome(Predicate<Holder<Biome>> param0) {
            HolderSet<Biome> var0 = this.structure().value().biomes();
            return var0.stream().anyMatch(param0);
        }
    }
}
