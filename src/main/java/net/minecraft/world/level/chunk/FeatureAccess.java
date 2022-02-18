package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface FeatureAccess {
    @Nullable
    StructureStart getStartForFeature(ConfiguredStructureFeature<?, ?> var1);

    void setStartForFeature(ConfiguredStructureFeature<?, ?> var1, StructureStart var2);

    LongSet getReferencesForFeature(ConfiguredStructureFeature<?, ?> var1);

    void addReferenceForFeature(ConfiguredStructureFeature<?, ?> var1, long var2);

    Map<ConfiguredStructureFeature<?, ?>, LongSet> getAllReferences();

    void setAllReferences(Map<ConfiguredStructureFeature<?, ?>, LongSet> var1);
}
