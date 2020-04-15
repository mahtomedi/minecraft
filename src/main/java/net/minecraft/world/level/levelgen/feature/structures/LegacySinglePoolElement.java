package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class LegacySinglePoolElement extends SinglePoolElement {
    @Deprecated
    public LegacySinglePoolElement(String param0, List<StructureProcessor> param1) {
        super(param0, param1, StructureTemplatePool.Projection.RIGID);
    }

    @Deprecated
    public LegacySinglePoolElement(String param0) {
        super(param0, ImmutableList.of());
    }

    public LegacySinglePoolElement(Dynamic<?> param0) {
        super(param0);
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation param0, BoundingBox param1, boolean param2) {
        StructurePlaceSettings var0 = super.getSettings(param0, param1, param2);
        var0.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        var0.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        return var0;
    }

    @Override
    public StructurePoolElementType getType() {
        return StructurePoolElementType.LEGACY;
    }

    @Override
    public String toString() {
        return "LegacySingle[" + this.template + "]";
    }
}
