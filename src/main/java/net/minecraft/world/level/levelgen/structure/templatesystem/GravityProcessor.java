package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;

public class GravityProcessor extends StructureProcessor {
    private final Heightmap.Types heightmap;
    private final int offset;

    public GravityProcessor(Heightmap.Types param0, int param1) {
        this.heightmap = param0;
        this.offset = param1;
    }

    public GravityProcessor(Dynamic<?> param0) {
        this(
            Heightmap.Types.getFromKey(param0.get("heightmap").asString(Heightmap.Types.WORLD_SURFACE_WG.getSerializationKey())), param0.get("offset").asInt(0)
        );
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader param0,
        BlockPos param1,
        BlockPos param2,
        StructureTemplate.StructureBlockInfo param3,
        StructureTemplate.StructureBlockInfo param4,
        StructurePlaceSettings param5
    ) {
        int var0 = param0.getHeight(this.heightmap, param4.pos.getX(), param4.pos.getZ()) + this.offset;
        int var1 = param3.pos.getY();
        return new StructureTemplate.StructureBlockInfo(new BlockPos(param4.pos.getX(), var0 + var1, param4.pos.getZ()), param4.state, param4.nbt);
    }

    @Override
    protected StructureProcessorType getType() {
        return StructureProcessorType.GRAVITY;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("heightmap"),
                    param0.createString(this.heightmap.getSerializationKey()),
                    param0.createString("offset"),
                    param0.createInt(this.offset)
                )
            )
        );
    }
}
