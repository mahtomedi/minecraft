package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class ListPoolElement extends StructurePoolElement {
    public static final Codec<ListPoolElement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(StructurePoolElement.CODEC.listOf().fieldOf("elements").forGetter(param0x -> param0x.elements), projectionCodec())
                .apply(param0, ListPoolElement::new)
    );
    private final List<StructurePoolElement> elements;

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
    public Vec3i getSize(StructureTemplateManager param0, Rotation param1) {
        int var0 = 0;
        int var1 = 0;
        int var2 = 0;

        for(StructurePoolElement var3 : this.elements) {
            Vec3i var4 = var3.getSize(param0, param1);
            var0 = Math.max(var0, var4.getX());
            var1 = Math.max(var1, var4.getY());
            var2 = Math.max(var2, var4.getZ());
        }

        return new Vec3i(var0, var1, var2);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
        StructureTemplateManager param0, BlockPos param1, Rotation param2, RandomSource param3
    ) {
        return this.elements.get(0).getShuffledJigsawBlocks(param0, param1, param2, param3);
    }

    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager param0, BlockPos param1, Rotation param2) {
        Stream<BoundingBox> var0 = this.elements
            .stream()
            .filter(param0x -> param0x != EmptyPoolElement.INSTANCE)
            .map(param3 -> param3.getBoundingBox(param0, param1, param2));
        return BoundingBox.encapsulatingBoxes(var0::iterator)
            .orElseThrow(() -> new IllegalStateException("Unable to calculate boundingbox for ListPoolElement"));
    }

    @Override
    public boolean place(
        StructureTemplateManager param0,
        WorldGenLevel param1,
        StructureManager param2,
        ChunkGenerator param3,
        BlockPos param4,
        BlockPos param5,
        Rotation param6,
        BoundingBox param7,
        RandomSource param8,
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
