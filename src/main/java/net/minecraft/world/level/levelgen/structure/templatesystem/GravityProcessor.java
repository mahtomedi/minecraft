package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;

public class GravityProcessor extends StructureProcessor {
    public static final Codec<GravityProcessor> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Heightmap.Types.CODEC.fieldOf("heightmap").withDefault(Heightmap.Types.WORLD_SURFACE_WG).forGetter(param0x -> param0x.heightmap),
                    Codec.INT.fieldOf("offset").withDefault(0).forGetter(param0x -> param0x.offset)
                )
                .apply(param0, GravityProcessor::new)
    );
    private final Heightmap.Types heightmap;
    private final int offset;

    public GravityProcessor(Heightmap.Types param0, int param1) {
        this.heightmap = param0;
        this.offset = param1;
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
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.GRAVITY;
    }
}
