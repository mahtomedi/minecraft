package net.minecraft.world.level.levelgen.feature.structures;

import net.minecraft.core.Registry;
import net.minecraft.util.Deserializer;

public interface StructurePoolElementType extends Deserializer<StructurePoolElement> {
    StructurePoolElementType SINGLE = register("single_pool_element", SinglePoolElement::new);
    StructurePoolElementType LIST = register("list_pool_element", ListPoolElement::new);
    StructurePoolElementType FEATURE = register("feature_pool_element", FeaturePoolElement::new);
    StructurePoolElementType EMPTY = register("empty_pool_element", param0 -> EmptyPoolElement.INSTANCE);

    static StructurePoolElementType register(String param0, StructurePoolElementType param1) {
        return Registry.register(Registry.STRUCTURE_POOL_ELEMENT, param0, param1);
    }
}
