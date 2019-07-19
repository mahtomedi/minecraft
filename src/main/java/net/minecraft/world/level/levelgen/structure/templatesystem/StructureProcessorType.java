package net.minecraft.world.level.levelgen.structure.templatesystem;

import net.minecraft.core.Registry;
import net.minecraft.util.Deserializer;

public interface StructureProcessorType extends Deserializer<StructureProcessor> {
    StructureProcessorType BLOCK_IGNORE = register("block_ignore", BlockIgnoreProcessor::new);
    StructureProcessorType BLOCK_ROT = register("block_rot", BlockRotProcessor::new);
    StructureProcessorType GRAVITY = register("gravity", GravityProcessor::new);
    StructureProcessorType JIGSAW_REPLACEMENT = register("jigsaw_replacement", param0 -> JigsawReplacementProcessor.INSTANCE);
    StructureProcessorType RULE = register("rule", RuleProcessor::new);
    StructureProcessorType NOP = register("nop", param0 -> NopProcessor.INSTANCE);

    static StructureProcessorType register(String param0, StructureProcessorType param1) {
        return Registry.register(Registry.STRUCTURE_PROCESSOR, param0, param1);
    }
}
