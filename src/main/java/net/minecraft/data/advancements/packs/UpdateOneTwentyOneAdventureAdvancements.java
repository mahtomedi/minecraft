package net.minecraft.data.advancements.packs;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.world.entity.EntityType;

public class UpdateOneTwentyOneAdventureAdvancements implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider param0, Consumer<AdvancementHolder> param1) {
        AdvancementHolder var0 = AdvancementSubProvider.createPlaceholder("adventure/root");
        VanillaAdventureAdvancements.createMonsterHunterAdvancement(
            var0, param1, Stream.concat(VanillaAdventureAdvancements.MOBS_TO_KILL.stream(), Stream.of(EntityType.BREEZE)).collect(Collectors.toList())
        );
    }
}
