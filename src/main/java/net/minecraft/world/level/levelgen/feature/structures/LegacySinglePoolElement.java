package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class LegacySinglePoolElement extends SinglePoolElement {
    public static final Codec<LegacySinglePoolElement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(templateCodec(), processorsCodec(), projectionCodec()).apply(param0, LegacySinglePoolElement::new)
    );

    protected LegacySinglePoolElement(
        Either<ResourceLocation, StructureTemplate> param0, Supplier<StructureProcessorList> param1, StructureTemplatePool.Projection param2
    ) {
        super(param0, param1, param2);
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation param0, BoundingBox param1, boolean param2) {
        StructurePlaceSettings var0 = super.getSettings(param0, param1, param2);
        var0.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        var0.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        return var0;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.LEGACY;
    }

    @Override
    public String toString() {
        return "LegacySingle[" + this.template + "]";
    }
}
