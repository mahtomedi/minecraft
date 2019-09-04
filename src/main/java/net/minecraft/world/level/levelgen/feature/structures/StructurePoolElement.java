package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public abstract class StructurePoolElement {
    @Nullable
    private volatile StructureTemplatePool.Projection projection;

    protected StructurePoolElement(StructureTemplatePool.Projection param0) {
        this.projection = param0;
    }

    protected StructurePoolElement(Dynamic<?> param0) {
        this.projection = StructureTemplatePool.Projection.byName(param0.get("projection").asString(StructureTemplatePool.Projection.RIGID.getName()));
    }

    public abstract List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager var1, BlockPos var2, Rotation var3, Random var4);

    public abstract BoundingBox getBoundingBox(StructureManager var1, BlockPos var2, Rotation var3);

    public abstract boolean place(
        StructureManager var1, LevelAccessor var2, ChunkGenerator<?> var3, BlockPos var4, Rotation var5, BoundingBox var6, Random var7
    );

    public abstract StructurePoolElementType getType();

    public void handleDataMarker(
        LevelAccessor param0, StructureTemplate.StructureBlockInfo param1, BlockPos param2, Rotation param3, Random param4, BoundingBox param5
    ) {
    }

    public StructurePoolElement setProjection(StructureTemplatePool.Projection param0) {
        this.projection = param0;
        return this;
    }

    public StructureTemplatePool.Projection getProjection() {
        StructureTemplatePool.Projection var0 = this.projection;
        if (var0 == null) {
            throw new IllegalStateException();
        } else {
            return var0;
        }
    }

    protected abstract <T> Dynamic<T> getDynamic(DynamicOps<T> var1);

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        T var0 = this.getDynamic(param0).getValue();
        T var1 = param0.mergeInto(
            var0, param0.createString("element_type"), param0.createString(Registry.STRUCTURE_POOL_ELEMENT.getKey(this.getType()).toString())
        );
        return new Dynamic<>(param0, param0.mergeInto(var1, param0.createString("projection"), param0.createString(this.projection.getName())));
    }

    public int getGroundLevelDelta() {
        return 1;
    }
}
