package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;

public class FlatLevelGeneratorPresetTagsProvider extends TagsProvider<FlatLevelGeneratorPreset> {
    public FlatLevelGeneratorPresetTagsProvider(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        super(param0, Registries.FLAT_LEVEL_GENERATOR_PRESET, param1);
    }

    @Override
    protected void addTags(HolderLookup.Provider param0) {
        this.tag(FlatLevelGeneratorPresetTags.VISIBLE)
            .add(FlatLevelGeneratorPresets.CLASSIC_FLAT)
            .add(FlatLevelGeneratorPresets.TUNNELERS_DREAM)
            .add(FlatLevelGeneratorPresets.WATER_WORLD)
            .add(FlatLevelGeneratorPresets.OVERWORLD)
            .add(FlatLevelGeneratorPresets.SNOWY_KINGDOM)
            .add(FlatLevelGeneratorPresets.BOTTOMLESS_PIT)
            .add(FlatLevelGeneratorPresets.DESERT)
            .add(FlatLevelGeneratorPresets.REDSTONE_READY)
            .add(FlatLevelGeneratorPresets.THE_VOID);
    }
}
