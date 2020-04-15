package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
        Heightmap.Types var0;
        if (param0 instanceof ServerLevel) {
            if (this.heightmap == Heightmap.Types.WORLD_SURFACE_WG) {
                var0 = Heightmap.Types.WORLD_SURFACE;
            } else if (this.heightmap == Heightmap.Types.OCEAN_FLOOR_WG) {
                var0 = Heightmap.Types.OCEAN_FLOOR;
            } else {
                var0 = this.heightmap;
            }
        } else {
            var0 = this.heightmap;
        }

        int var4 = param0.getHeight(var0, param4.pos.getX(), param4.pos.getZ()) + this.offset;
        int var5 = param3.pos.getY();
        return new StructureTemplate.StructureBlockInfo(new BlockPos(param4.pos.getX(), var4 + var5, param4.pos.getZ()), param4.state, param4.nbt);
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
