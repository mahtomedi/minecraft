package net.minecraft.data.advancements.packs;

import java.util.List;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;

public class VanillaAdvancementProvider {
    public static AdvancementProvider create(PackOutput param0) {
        return new AdvancementProvider(
            "Vanilla Advancements",
            param0,
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
