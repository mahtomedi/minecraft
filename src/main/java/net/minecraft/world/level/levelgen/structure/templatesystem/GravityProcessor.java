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
                    Heightmap.Types.CODEC.fieldOf("heightmap").orElse(Heightmap.Types.WORLD_SURFACE_WG).forGetter(param0x -> param0x.heightmap),
                    Codec.INT.fieldOf("offset").orElse(0).forGetter(param0x -> param0x.offset)
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

        BlockPos var4 = param4.pos();
        int var5 = param0.getHeight(var0, var4.getX(), var4.getZ()) + this.offset;
        int var6 = param3.pos().getY();
        return new StructureTemplate.StructureBlockInfo(new BlockPos(var4.getX(), var5 + var6, var4.getZ()), param4.state(), param4.nbt());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.GRAVITY;
    }
}
