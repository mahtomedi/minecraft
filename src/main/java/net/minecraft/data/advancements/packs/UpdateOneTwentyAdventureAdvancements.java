package net.minecraft.data.advancements.packs;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public class UpdateOneTwentyAdventureAdvancements implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider param0, Consumer<Advancement> param1) {
        Advancement var0 = AdvancementSubProvider.createPlaceholder("adventure/sleep_in_bed");
        VanillaAdventureAdvancements.createAdventuringTime(param1, var0, MultiNoiseBiomeSource.Preset.OVERWORLD_UPDATE_1_20);
    }
}
