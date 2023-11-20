package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

public interface AdvancementSubProvider {
    void generate(HolderLookup.Provider var1, Consumer<AdvancementHolder> var2);

    static AdvancementHolder createPlaceholder(String param0) {
        return Advancement.Builder.advancement().build(new ResourceLocation(param0));
    }
}
