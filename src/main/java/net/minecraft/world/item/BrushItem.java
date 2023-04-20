package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.context.UseOnContext;
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
    public static final int ANIMATION_DURATION = 10;
    private static final int USE_DURATION = 200;
    private static final double MAX_BRUSH_DISTANCE = Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) - 1.0;

    public BrushItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Player var0 = param0.getPlayer();
        if (var0 != null && this.calculateHitResult(var0).getType() == HitResult.Type.BLOCK) {
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
        return 200;
    }

    @Override
    public void onUseTick(Level param0, LivingEntity param1, ItemStack param2, int param3) {
        if (param3 >= 0 && param1 instanceof Player var0) {
            HitResult var2 = this.calculateHitResult(param1);
            if (var2 instanceof BlockHitResult var3 && var2.getType() == HitResult.Type.BLOCK) {
                int var5 = this.getUseDuration(param2) - param3 + 1;
                boolean var6 = var5 % 10 == 5;
                if (var6) {
                    BlockPos var7 = var3.getBlockPos();
                    BlockState var8 = param0.getBlockState(var7);
                    this.spawnDustParticles(param0, var3, var8, param1.getViewVector(0.0F), param1.getMainHandItem().equals(param2));
                    Block var13 = var8.getBlock();
                    SoundEvent var10;
                    if (var13 instanceof BrushableBlock var9) {
                        var10 = var9.getBrushSound();
                    } else {
                        var10 = SoundEvents.BRUSH_GENERIC;
                    }

                    param0.playSound(var0, var7, var10, SoundSource.BLOCKS);
                    if (!param0.isClientSide()) {
                        BlockEntity var17 = param0.getBlockEntity(var7);
                        if (var17 instanceof BrushableBlockEntity var12) {
                            boolean var13 = var12.brush(param0.getGameTime(), var0, var3.getDirection());
                            if (var13) {
                                EquipmentSlot var14 = param2.equals(var0.getItemBySlot(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                                param2.hurtAndBreak(1, param1, param1x -> param1x.broadcastBreakEvent(var14));
                            }
                        }
                    }
                }

                return;
            }

            param1.releaseUsingItem();
        } else {
            param1.releaseUsingItem();
        }
    }

    private HitResult calculateHitResult(LivingEntity param0) {
        return ProjectileUtil.getHitResultOnViewVector(param0, param0x -> !param0x.isSpectator() && param0x.isPickable(), MAX_BRUSH_DISTANCE);
    }

    public void spawnDustParticles(Level param0, BlockHitResult param1, BlockState param2, Vec3 param3, boolean param4) {
        double var0 = 3.0;
        int var1 = param4 ? 1 : -1;
        int var2 = param0.getRandom().nextInt(7, 12);
        BlockParticleOption var3 = new BlockParticleOption(ParticleTypes.BLOCK, param2);
        Direction var4 = param1.getDirection();
        BrushItem.DustParticlesDelta var5 = BrushItem.DustParticlesDelta.fromDirection(param3, var4);
        Vec3 var6 = param1.getLocation();

        for(int var7 = 0; var7 < var2; ++var7) {
            param0.addParticle(
                var3,
                var6.x - (double)(var4 == Direction.WEST ? 1.0E-6F : 0.0F),
                var6.y,
                var6.z - (double)(var4 == Direction.NORTH ? 1.0E-6F : 0.0F),
                var5.xd() * (double)var1 * 3.0 * param0.getRandom().nextDouble(),
                0.0,
                var5.zd() * (double)var1 * 3.0 * param0.getRandom().nextDouble()
            );
        }

    }

    static record DustParticlesDelta(double xd, double yd, double zd) {
        private static final double ALONG_SIDE_DELTA = 1.0;
        private static final double OUT_FROM_SIDE_DELTA = 0.1;

        public static BrushItem.DustParticlesDelta fromDirection(Vec3 param0, Direction param1) {
            double var0 = 0.0;

            return switch(param1) {
                case DOWN, UP -> new BrushItem.DustParticlesDelta(param0.z(), 0.0, -param0.x());
                case NORTH -> new BrushItem.DustParticlesDelta(1.0, 0.0, -0.1);
                case SOUTH -> new BrushItem.DustParticlesDelta(-1.0, 0.0, 0.1);
                case WEST -> new BrushItem.DustParticlesDelta(-0.1, 0.0, -1.0);
                case EAST -> new BrushItem.DustParticlesDelta(0.1, 0.0, 1.0);
            };
        }
    }
}
