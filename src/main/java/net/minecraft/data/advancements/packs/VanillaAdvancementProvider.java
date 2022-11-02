package net.minecraft.data.advancements.packs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;

public class VanillaAdvancementProvider {
    public static AdvancementProvider create(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        return new AdvancementProvider(
            param0,
            param1,
            List.of(
                new VanillaTheEndAdvancements(),
                new VanillaHusbandryAdvancements(),
                new VanillaAdventureAdvancements(),
                new VanillaNetherAdvancements(),
                new VanillaStoryAdvancements()
            )
        );
    }
}
