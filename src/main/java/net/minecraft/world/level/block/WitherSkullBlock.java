package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

public class WitherSkullBlock extends SkullBlock {
    @Nullable
    private static BlockPattern witherPatternFull;
    @Nullable
    private static BlockPattern witherPatternBase;

    protected WitherSkullBlock(BlockBehaviour.Properties param0) {
        super(SkullBlock.Types.WITHER_SKELETON, param0);
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
        super.setPlacedBy(param0, param1, param2, param3, param4);
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (var0 instanceof SkullBlockEntity) {
            checkSpawn(param0, param1, (SkullBlockEntity)var0);
        }

    }

    public static void checkSpawn(Level param0, BlockPos param1, SkullBlockEntity param2) {
        if (!param0.isClientSide) {
            BlockState var0 = param2.getBlockState();
            boolean var1 = var0.is(Blocks.WITHER_SKELETON_SKULL) || var0.is(Blocks.WITHER_SKELETON_WALL_SKULL);
            if (var1 && param1.getY() >= param0.getMinBuildHeight() && param0.getDifficulty() != Difficulty.PEACEFUL) {
                BlockPattern.BlockPatternMatch var2 = getOrCreateWitherFull().find(param0, param1);
                if (var2 != null) {
                    WitherBoss var3 = EntityType.WITHER.create(param0);
                    if (var3 != null) {
                        CarvedPumpkinBlock.clearPatternBlocks(param0, var2);
                        BlockPos var4 = var2.getBlock(1, 2, 0).getPos();
                        var3.moveTo(
                            (double)var4.getX() + 0.5,
                            (double)var4.getY() + 0.55,
                            (double)var4.getZ() + 0.5,
                            var2.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F,
                            0.0F
                        );
                        var3.yBodyRot = var2.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
                        var3.makeInvulnerable();

                        for(ServerPlayer var5 : param0.getEntitiesOfClass(ServerPlayer.class, var3.getBoundingBox().inflate(50.0))) {
                            CriteriaTriggers.SUMMONED_ENTITY.trigger(var5, var3);
                        }

                        param0.addFreshEntity(var3);
                        CarvedPumpkinBlock.updatePatternBlocks(param0, var2);
                    }

                }
            }
        }
    }

    public static boolean canSpawnMob(Level param0, BlockPos param1, ItemStack param2) {
        if (param2.is(Items.WITHER_SKELETON_SKULL)
            && param1.getY() >= param0.getMinBuildHeight() + 2
            && param0.getDifficulty() != Difficulty.PEACEFUL
            && !param0.isClientSide) {
            return getOrCreateWitherBase().find(param0, param1) != null;
        } else {
            return false;
        }
    }

    private static BlockPattern getOrCreateWitherFull() {
        if (witherPatternFull == null) {
            witherPatternFull = BlockPatternBuilder.start()
                .aisle("^^^", "###", "~#~")
                .where('#', param0 -> param0.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
                .where(
                    '^',
                    BlockInWorld.hasState(
                        BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL))
                    )
                )
                .where('~', param0 -> param0.getState().isAir())
                .build();
        }

        return witherPatternFull;
    }

    private static BlockPattern getOrCreateWitherBase() {
        if (witherPatternBase == null) {
            witherPatternBase = BlockPatternBuilder.start()
                .aisle("   ", "###", "~#~")
                .where('#', param0 -> param0.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
                .where('~', param0 -> param0.getState().isAir())
                .build();
        }

        return witherPatternBase;
    }
}
