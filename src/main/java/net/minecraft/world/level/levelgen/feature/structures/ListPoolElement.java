package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ListPoolElement extends StructurePoolElement {
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

    public ListPoolElement(Dynamic<?> param0) {
        super(param0);
        List<StructurePoolElement> var0 = param0.get("elements")
            .asList(param0x -> Deserializer.deserialize(param0x, Registry.STRUCTURE_POOL_ELEMENT, "element_type", EmptyPoolElement.INSTANCE));
        if (var0.isEmpty()) {
            throw new IllegalArgumentException("Elements are empty");
        } else {
            this.elements = var0;
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
        LevelAccessor param1,
        ChunkGenerator<?> param2,
        BlockPos param3,
        BlockPos param4,
        Rotation param5,
        BoundingBox param6,
        Random param7
    ) {
        for(StructurePoolElement var0 : this.elements) {
            if (!var0.place(param0, param1, param2, param3, param4, param5, param6, param7)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public StructurePoolElementType getType() {
        return StructurePoolElementType.LIST;
    }

    @Override
    public StructurePoolElement setProjection(StructureTemplatePool.Projection param0) {
        super.setProjection(param0);
        this.setProjectionOnEachElement(param0);
        return this;
    }

    @Override
    public <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        T var0 = param0.createList(this.elements.stream().map(param1 -> param1.serialize(param0).getValue()));
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("elements"), var0)));
    }

    @Override
    public String toString() {
        return "List[" + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    private void setProjectionOnEachElement(StructureTemplatePool.Projection param0) {
        this.elements.forEach(param1 -> param1.setProjection(param0));
    }
}
