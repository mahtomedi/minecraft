package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractCandleBlock extends Block {
    public static final int LIGHT_PER_CANDLE = 3;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    @Override
    protected abstract MapCodec<? extends AbstractCandleBlock> codec();

    protected AbstractCandleBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    protected abstract Iterable<Vec3> getParticleOffsets(BlockState var1);

    public static boolean isLit(BlockState param0) {
        return param0.hasProperty(LIT) && (param0.is(BlockTags.CANDLES) || param0.is(BlockTags.CANDLE_CAKES)) && param0.getValue(LIT);
    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        if (!param0.isClientSide && param3.isOnFire() && this.canBeLit(param1)) {
            setLit(param0, param1, param2.getBlockPos(), true);
        }

    }

    protected boolean canBeLit(BlockState param0) {
        return !param0.getValue(LIT);
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        if (param0.getValue(LIT)) {
            this.getParticleOffsets(param0)
                .forEach(param3x -> addParticlesAndSound(param1, param3x.add((double)param2.getX(), (double)param2.getY(), (double)param2.getZ()), param3));
        }
    }

    private static void addParticlesAndSound(Level param0, Vec3 param1, RandomSource param2) {
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

    public static void extinguish(@Nullable Player param0, BlockState param1, LevelAccessor param2, BlockPos param3) {
        setLit(param2, param1, param3, false);
        if (param1.getBlock() instanceof AbstractCandleBlock) {
            ((AbstractCandleBlock)param1.getBlock())
                .getParticleOffsets(param1)
                .forEach(
                    param2x -> param2.addParticle(
                            ParticleTypes.SMOKE,
                            (double)param3.getX() + param2x.x(),
                            (double)param3.getY() + param2x.y(),
                            (double)param3.getZ() + param2x.z(),
                            0.0,
                            0.1F,
                            0.0
                        )
                );
        }

        param2.playSound(null, param3, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        param2.gameEvent(param0, GameEvent.BLOCK_CHANGE, param3);
    }

    private static void setLit(LevelAccessor param0, BlockState param1, BlockPos param2, boolean param3) {
        param0.setBlock(param2, param1.setValue(LIT, Boolean.valueOf(param3)), 11);
    }

    @Override
    public void onExplosionHit(BlockState param0, Level param1, BlockPos param2, Explosion param3, BiConsumer<ItemStack, BlockPos> param4) {
        if (param3.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK && !param1.isClientSide() && param0.getValue(LIT)) {
            extinguish(null, param0, param1, param2);
        }

        super.onExplosionHit(param0, param1, param2, param3, param4);
    }
}
