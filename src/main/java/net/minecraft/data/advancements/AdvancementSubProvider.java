package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;

public interface AdvancementSubProvider {
    void generate(HolderLookup.Provider var1, Consumer<AdvancementHolder> var2);
}
