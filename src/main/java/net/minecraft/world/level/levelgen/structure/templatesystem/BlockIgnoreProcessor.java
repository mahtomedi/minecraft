package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockIgnoreProcessor extends StructureProcessor {
    public static final BlockIgnoreProcessor STRUCTURE_BLOCK = new BlockIgnoreProcessor(ImmutableList.of(Blocks.STRUCTURE_BLOCK));
    public static final BlockIgnoreProcessor AIR = new BlockIgnoreProcessor(ImmutableList.of(Blocks.AIR));
    public static final BlockIgnoreProcessor STRUCTURE_AND_AIR = new BlockIgnoreProcessor(ImmutableList.of(Blocks.AIR, Blocks.STRUCTURE_BLOCK));
    private final ImmutableList<Block> toIgnore;

    public BlockIgnoreProcessor(List<Block> param0) {
        this.toIgnore = ImmutableList.copyOf(param0);
    }

    public BlockIgnoreProcessor(Dynamic<?> param0) {
        this(param0.get("blocks").asList(param0x -> BlockState.deserialize(param0x).getBlock()));
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader param0,
        BlockPos param1,
        StructureTemplate.StructureBlockInfo param2,
        StructureTemplate.StructureBlockInfo param3,
        StructurePlaceSettings param4
    ) {
        return this.toIgnore.contains(param3.state.getBlock()) ? null : param3;
    }

    @Override
    protected StructureProcessorType getType() {
        return StructureProcessorType.BLOCK_IGNORE;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("blocks"),
                    param0.createList(this.toIgnore.stream().map(param1 -> BlockState.serialize(param0, param1.defaultBlockState()).getValue()))
                )
            )
        );
    }
}
