package net.minecraft.data.advancements.packs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;

public class UpdateOneTwentyAdvancements extends VanillaHusbandryAdvancements {
    @Override
    public void generate(HolderLookup.Provider param0, Consumer<Advancement> param1) {
        Advancement var0 = this.createRoot(param1);
        Advancement var1 = this.createBreedAnAnimalAdvancement(var0, param1);
        this.createBreedAllAnimalsAdvancement(var1, param1);
    }

    @Override
    public EntityType<?>[] getBreedableAnimals() {
        EntityType<?>[] var0 = super.getBreedableAnimals();
        List<EntityType<?>> var1 = Arrays.stream(var0).collect(Collectors.toList());
        var1.add(EntityType.CAMEL);
        return var1.toArray(var0);
    }
}
