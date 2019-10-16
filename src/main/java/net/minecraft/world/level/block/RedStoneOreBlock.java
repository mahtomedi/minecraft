package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedStoneOreBlock extends Block {
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public RedStoneOreBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(false)));
    }

    @Override
    public int getLightEmission(BlockState param0) {
        return param0.getValue(LIT) ? super.getLightEmission(param0) : 0;
    }

    @Override
    public void attack(BlockState param0, Level param1, BlockPos param2, Player param3) {
        interact(param0, param1, param2);
        super.attack(param0, param1, param2, param3);
    }

    @Override
    public void stepOn(Level param0, BlockPos param1, Entity param2) {
        interact(param0.getBlockState(param1), param0, param1);
        super.stepOn(param0, param1, param2);
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            interact(param0, param1, param2);
            return InteractionResult.PASS;
        }
    }

    private static void interact(BlockState param0, Level param1, BlockPos param2) {
        spawnParticles(param1, param2);
        if (!param0.getValue(LIT)) {
            param1.setBlock(param2, param0.setValue(LIT, Boolean.valueOf(true)), 3);
        }

    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param0.getValue(LIT)) {
            param1.setBlock(param2, param0.setValue(LIT, Boolean.valueOf(false)), 3);
        }

    }

    @Override
    public void spawnAfterBreak(BlockState param0, Level param1, BlockPos param2, ItemStack param3) {
        super.spawnAfterBreak(param0, param1, param2, param3);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, param3) == 0) {
            int var0 = 1 + param1.random.nextInt(5);
            this.popExperience(param1, param2, var0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param0.getValue(LIT)) {
            spawnParticles(param1, param2);
        }

    }

    private static void spawnParticles(Level param0, BlockPos param1) {
        double var0 = 0.5625;
        Random var1 = param0.random;

        for(Direction var2 : Direction.values()) {
            BlockPos var3 = param1.relative(var2);
            if (!param0.getBlockState(var3).isSolidRender(param0, var3)) {
                Direction.Axis var4 = var2.getAxis();
                double var5 = var4 == Direction.Axis.X ? 0.5 + 0.5625 * (double)var2.getStepX() : (double)var1.nextFloat();
                double var6 = var4 == Direction.Axis.Y ? 0.5 + 0.5625 * (double)var2.getStepY() : (double)var1.nextFloat();
                double var7 = var4 == Direction.Axis.Z ? 0.5 + 0.5625 * (double)var2.getStepZ() : (double)var1.nextFloat();
                param0.addParticle(
                    DustParticleOptions.REDSTONE, (double)param1.getX() + var5, (double)param1.getY() + var6, (double)param1.getZ() + var7, 0.0, 0.0, 0.0
                );
            }
        }

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(LIT);
    }
}
