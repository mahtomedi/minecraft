package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class LayeredCauldronBlock extends AbstractCauldronBlock {
    public static final MapCodec<LayeredCauldronBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter(param0x -> param0x.precipitationType),
                    CauldronInteraction.CODEC.fieldOf("interactions").forGetter(param0x -> param0x.interactions),
                    propertiesCodec()
                )
                .apply(param0, LayeredCauldronBlock::new)
    );
    public static final int MIN_FILL_LEVEL = 1;
    public static final int MAX_FILL_LEVEL = 3;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
    private static final int BASE_CONTENT_HEIGHT = 6;
    private static final double HEIGHT_PER_LEVEL = 3.0;
    private final Biome.Precipitation precipitationType;

    @Override
    public MapCodec<LayeredCauldronBlock> codec() {
        return CODEC;
    }

    public LayeredCauldronBlock(Biome.Precipitation param0, CauldronInteraction.InteractionMap param1, BlockBehaviour.Properties param2) {
        super(param2, param1);
        this.precipitationType = param0;
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(1)));
    }

    @Override
    public boolean isFull(BlockState param0) {
        return param0.getValue(LEVEL) == 3;
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid param0) {
        return param0 == Fluids.WATER && this.precipitationType == Biome.Precipitation.RAIN;
    }

    @Override
    protected double getContentHeight(BlockState param0) {
        return (6.0 + (double)param0.getValue(LEVEL).intValue() * 3.0) / 16.0;
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide && param3.isOnFire() && this.isEntityInsideContent(param0, param2, param3)) {
            param3.clearFire();
            if (param3.mayInteract(param1, param2)) {
                this.handleEntityOnFireInside(param0, param1, param2);
            }
        }

    }

    private void handleEntityOnFireInside(BlockState param0, Level param1, BlockPos param2) {
        if (this.precipitationType == Biome.Precipitation.SNOW) {
            lowerFillLevel(Blocks.WATER_CAULDRON.defaultBlockState().setValue(LEVEL, param0.getValue(LEVEL)), param1, param2);
        } else {
            lowerFillLevel(param0, param1, param2);
        }

    }

    public static void lowerFillLevel(BlockState param0, Level param1, BlockPos param2) {
        int var0 = param0.getValue(LEVEL) - 1;
        BlockState var1 = var0 == 0 ? Blocks.CAULDRON.defaultBlockState() : param0.setValue(LEVEL, Integer.valueOf(var0));
        param1.setBlockAndUpdate(param2, var1);
        param1.gameEvent(GameEvent.BLOCK_CHANGE, param2, GameEvent.Context.of(var1));
    }

    @Override
    public void handlePrecipitation(BlockState param0, Level param1, BlockPos param2, Biome.Precipitation param3) {
        if (CauldronBlock.shouldHandlePrecipitation(param1, param3) && param0.getValue(LEVEL) != 3 && param3 == this.precipitationType) {
            BlockState var0 = param0.cycle(LEVEL);
            param1.setBlockAndUpdate(param2, var0);
            param1.gameEvent(GameEvent.BLOCK_CHANGE, param2, GameEvent.Context.of(var0));
        }
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return param0.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(LEVEL);
    }

    @Override
    protected void receiveStalactiteDrip(BlockState param0, Level param1, BlockPos param2, Fluid param3) {
        if (!this.isFull(param0)) {
            BlockState var0 = param0.setValue(LEVEL, Integer.valueOf(param0.getValue(LEVEL) + 1));
            param1.setBlockAndUpdate(param2, var0);
            param1.gameEvent(GameEvent.BLOCK_CHANGE, param2, GameEvent.Context.of(var0));
            param1.levelEvent(1047, param2, 0);
        }
    }
}
