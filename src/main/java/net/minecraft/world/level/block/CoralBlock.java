package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class CoralBlock extends Block {
    public static final MapCodec<Block> DEAD_CORAL_FIELD = BuiltInRegistries.BLOCK.byNameCodec().fieldOf("dead");
    public static final MapCodec<CoralBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(DEAD_CORAL_FIELD.forGetter(param0x -> param0x.deadBlock), propertiesCodec()).apply(param0, CoralBlock::new)
    );
    private final Block deadBlock;

    public CoralBlock(Block param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.deadBlock = param0;
    }

    @Override
    public MapCodec<CoralBlock> codec() {
        return CODEC;
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (!this.scanForWater(param1, param2)) {
            param1.setBlock(param2, this.deadBlock.defaultBlockState(), 2);
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (!this.scanForWater(param3, param4)) {
            param3.scheduleTick(param4, this, 60 + param3.getRandom().nextInt(40));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    protected boolean scanForWater(BlockGetter param0, BlockPos param1) {
        for(Direction var0 : Direction.values()) {
            FluidState var1 = param0.getFluidState(param1.relative(var0));
            if (var1.is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        if (!this.scanForWater(param0.getLevel(), param0.getClickedPos())) {
            param0.getLevel().scheduleTick(param0.getClickedPos(), this, 60 + param0.getLevel().getRandom().nextInt(40));
        }

        return this.defaultBlockState();
    }
}
