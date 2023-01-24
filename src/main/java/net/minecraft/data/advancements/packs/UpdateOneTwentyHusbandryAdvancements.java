package net.minecraft.data.advancements.packs;

import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.world.entity.EntityType;

public class UpdateOneTwentyHusbandryAdvancements implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider param0, Consumer<Advancement> param1) {
        Advancement var0 = AdvancementSubProvider.createPlaceholder("husbandry/breed_an_animal");
        Stream<EntityType<?>> var1 = Stream.concat(VanillaHusbandryAdvancements.BREEDABLE_ANIMALS.stream(), Stream.of(EntityType.CAMEL));
        VanillaHusbandryAdvancements.createBreedAllAnimalsAdvancement(var0, param1, var1, VanillaHusbandryAdvancements.INDIRECTLY_BREEDABLE_ANIMALS.stream());
    }
}
