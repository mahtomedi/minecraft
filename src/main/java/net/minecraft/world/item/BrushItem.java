package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BrushItem extends Item {
    public static final int TICKS_BETWEEN_SWEEPS = 10;
    private static final int USE_DURATION = 225;

    public BrushItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Player var0 = param0.getPlayer();
        if (var0 != null) {
            var0.startUsingItem(param0.getHand());
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack param0) {
        return 225;
    }

    @Override
    public void onUseTick(Level param0, LivingEntity param1, ItemStack param2, int param3) {
        if (param3 >= 0 && param1 instanceof Player var0) {
            BlockHitResult var2 = Item.getPlayerPOVHitResult(param0, var0, ClipContext.Fluid.NONE);
            BlockPos var3 = var2.getBlockPos();
            if (var2.getType() == HitResult.Type.MISS) {
                param1.releaseUsingItem();
            } else {
                int var4 = this.getUseDuration(param2) - param3 + 1;
                if (var4 == 1 || var4 % 10 == 0) {
                    BlockState var5 = param0.getBlockState(var3);
                    this.spawnDustParticles(param0, var2, var5, param1.getViewVector(0.0F));
                    Block var10 = var5.getBlock();
                    SoundEvent var7;
                    if (var10 instanceof BrushableBlock var6) {
                        var7 = var6.getBrushSound();
                    } else {
                        var7 = SoundEvents.BRUSH_GENERIC;
                    }

                    param0.playSound(var0, var3, var7, SoundSource.PLAYERS);
                    if (!param0.isClientSide()) {
                        BlockEntity var14 = param0.getBlockEntity(var3);
                        if (var14 instanceof BrushableBlockEntity var9) {
                            boolean var10 = var9.brush(param0.getGameTime(), var0, var2.getDirection());
                            if (var10) {
                                param2.hurtAndBreak(1, param1, param0x -> param0x.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                            }
                        }
                    }
                }

            }
        } else {
            param1.releaseUsingItem();
        }
    }

    public void spawnDustParticles(Level param0, BlockHitResult param1, BlockState param2, Vec3 param3) {
        double var0 = 3.0;
        int var1 = param0.getRandom().nextInt(7, 12);
        BlockParticleOption var2 = new BlockParticleOption(ParticleTypes.BLOCK, param2);
        Direction var3 = param1.getDirection();
        BrushItem.DustParticlesDelta var4 = BrushItem.DustParticlesDelta.fromDirection(param3, var3);
        Vec3 var5 = param1.getLocation();

        for(int var6 = 0; var6 < var1; ++var6) {
            param0.addParticle(
                var2,
                var5.x - (double)(var3 == Direction.WEST ? 1.0E-6F : 0.0F),
                var5.y,
                var5.z - (double)(var3 == Direction.NORTH ? 1.0E-6F : 0.0F),
                var4.xd() * 3.0 * param0.getRandom().nextDouble(),
                0.0,
                var4.zd() * 3.0 * param0.getRandom().nextDouble()
            );
        }

    }

    static record DustParticlesDelta(double xd, double yd, double zd) {
        private static final double ALONG_SIDE_DELTA = 1.0;
        private static final double OUT_FROM_SIDE_DELTA = 0.1;

        public static BrushItem.DustParticlesDelta fromDirection(Vec3 param0, Direction param1) {
            double var0 = 0.0;

            return switch(param1) {
                case DOWN -> new BrushItem.DustParticlesDelta(-param0.x(), 0.0, param0.z());
                case UP -> new BrushItem.DustParticlesDelta(param0.z(), 0.0, -param0.x());
                case NORTH -> new BrushItem.DustParticlesDelta(1.0, 0.0, -0.1);
                case SOUTH -> new BrushItem.DustParticlesDelta(-1.0, 0.0, 0.1);
                case WEST -> new BrushItem.DustParticlesDelta(-0.1, 0.0, -1.0);
                case EAST -> new BrushItem.DustParticlesDelta(0.1, 0.0, 1.0);
            };
        }
    }
}
