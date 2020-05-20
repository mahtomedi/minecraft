package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ListPoolElement extends StructurePoolElement {
    public static final Codec<ListPoolElement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(StructurePoolElement.CODEC.listOf().fieldOf("elements").forGetter(param0x -> param0x.elements), projectionCodec())
                .apply(param0, ListPoolElement::new)
    );
    private final List<StructurePoolElement> elements;

    @Deprecated
    public ListPoolElement(List<StructurePoolElement> param0) {
        this(param0, StructureTemplatePool.Projection.RIGID);
    }

    public ListPoolElement(List<StructurePoolElement> param0, StructureTemplatePool.Projection param1) {
        super(param1);
        if (param0.isEmpty()) {
            throw new IllegalArgumentException("Elements are empty");
        } else {
            this.elements = param0;
            this.setProjectionOnEachElement(param1);
        }
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager param0, BlockPos param1, Rotation param2, Random param3) {
        return this.elements.get(0).getShuffledJigsawBlocks(param0, param1, param2, param3);
    }

    @Override
    public BoundingBox getBoundingBox(StructureManager param0, BlockPos param1, Rotation param2) {
        BoundingBox var0 = BoundingBox.getUnknownBox();

        for(StructurePoolElement var1 : this.elements) {
            BoundingBox var2 = var1.getBoundingBox(param0, param1, param2);
            var0.expand(var2);
        }

        return var0;
    }

    @Override
    public boolean place(
        StructureManager param0,
        WorldGenLevel param1,
        StructureFeatureManager param2,
        ChunkGenerator param3,
        BlockPos param4,
        BlockPos param5,
        Rotation param6,
        BoundingBox param7,
        Random param8,
        boolean param9
    ) {
        for(StructurePoolElement var0 : this.elements) {
            if (!var0.place(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.LIST;
    }

    @Override
    public StructurePoolElement setProjection(StructureTemplatePool.Projection param0) {
        super.setProjection(param0);
        this.setProjectionOnEachElement(param0);
        return this;
    }

    @Override
    public String toString() {
        return "List[" + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    private void setProjectionOnEachElement(StructureTemplatePool.Projection param0) {
        this.elements.forEach(param1 -> param1.setProjection(param0));
    }
}
