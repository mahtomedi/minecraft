package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class BrushableBlock extends BaseEntityBlock implements Fallable {
    public static final MapCodec<BrushableBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("turns_into").forGetter(BrushableBlock::getTurnsInto),
                    BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_sound").forGetter(BrushableBlock::getBrushSound),
                    BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_comleted_sound").forGetter(BrushableBlock::getBrushCompletedSound),
                    propertiesCodec()
                )
                .apply(param0, BrushableBlock::new)
    );
    private static final IntegerProperty DUSTED = BlockStateProperties.DUSTED;
    public static final int TICK_DELAY = 2;
    private final Block turnsInto;
    private final SoundEvent brushSound;
    private final SoundEvent brushCompletedSound;

    @Override
    public MapCodec<BrushableBlock> codec() {
        return CODEC;
    }

    public BrushableBlock(Block param0, SoundEvent param1, SoundEvent param2, BlockBehaviour.Properties param3) {
        super(param3);
        this.turnsInto = param0;
        this.brushSound = param1;
        this.brushCompletedSound = param2;
        this.registerDefaultState(this.stateDefinition.any().setValue(DUSTED, Integer.valueOf(0)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(DUSTED);
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        param1.scheduleTick(param2, this, 2);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        param3.scheduleTick(param4, this, 2);
        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        BlockEntity var6 = param1.getBlockEntity(param2);
        if (var6 instanceof BrushableBlockEntity var0) {
            var0.checkReset();
        }

        if (FallingBlock.isFree(param1.getBlockState(param2.below())) && param2.getY() >= param1.getMinBuildHeight()) {
            FallingBlockEntity var1 = FallingBlockEntity.fall(param1, param2, param0);
            var1.disableDrop();
        }
    }

    @Override
    public void onBrokenAfterFall(Level param0, BlockPos param1, FallingBlockEntity param2) {
        Vec3 var0 = param2.getBoundingBox().getCenter();
        param0.levelEvent(2001, BlockPos.containing(var0), Block.getId(param2.getBlockState()));
        param0.gameEvent(param2, GameEvent.BLOCK_DESTROY, var0);
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        if (param3.nextInt(16) == 0) {
            BlockPos var0 = param2.below();
            if (FallingBlock.isFree(param1.getBlockState(var0))) {
                double var1 = (double)param2.getX() + param3.nextDouble();
                double var2 = (double)param2.getY() - 0.05;
                double var3 = (double)param2.getZ() + param3.nextDouble();
                param1.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, param0), var1, var2, var3, 0.0, 0.0, 0.0);
            }
        }

    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new BrushableBlockEntity(param0, param1);
    }

    public Block getTurnsInto() {
        return this.turnsInto;
    }

    public SoundEvent getBrushSound() {
        return this.brushSound;
    }

    public SoundEvent getBrushCompletedSound() {
        return this.brushCompletedSound;
    }
}
