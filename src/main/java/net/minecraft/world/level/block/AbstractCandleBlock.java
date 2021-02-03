package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractCandleBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected AbstractCandleBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract Iterable<Vec3> getParticleOffsets(BlockState var1);

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        if (!param0.isClientSide && param3.isOnFire() && !param1.getValue(LIT)) {
            setLit(param0, param1, param2.getBlockPos(), true);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param0.getValue(LIT)) {
            this.getParticleOffsets(param0)
                .forEach(param3x -> addParticlesAndSound(param1, param3x.add((double)param2.getX(), (double)param2.getY(), (double)param2.getZ()), param3));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void addParticlesAndSound(Level param0, Vec3 param1, Random param2) {
        float var0 = param2.nextFloat();
        if (var0 < 0.3F) {
            param0.addParticle(ParticleTypes.SMOKE, param1.x, param1.y, param1.z, 0.0, 0.0, 0.0);
            if (var0 < 0.17F) {
                param0.playLocalSound(
                    param1.x + 0.5,
                    param1.y + 0.5,
                    param1.z + 0.5,
                    SoundEvents.CANDLE_AMBIENT,
                    SoundSource.BLOCKS,
                    1.0F + param2.nextFloat(),
                    param2.nextFloat() * 0.7F + 0.3F,
                    false
                );
            }
        }

        param0.addParticle(ParticleTypes.SMALL_FLAME, param1.x, param1.y, param1.z, 0.0, 0.0, 0.0);
    }

    protected static void extinguish(@Nullable Player param0, BlockState param1, LevelAccessor param2, BlockPos param3) {
        setLit(param2, param1, param3, false);
        param2.addParticle(ParticleTypes.SMOKE, (double)param3.getX(), (double)param3.getY(), (double)param3.getZ(), 0.0, 0.1F, 0.0);
        param2.playSound(null, param3, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        param2.gameEvent(param0, GameEvent.BLOCK_CHANGE, param3);
    }

    private static void setLit(LevelAccessor param0, BlockState param1, BlockPos param2, boolean param3) {
        param0.setBlock(param2, param1.setValue(LIT, Boolean.valueOf(param3)), 11);
    }
}
