package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class InfestedBlock extends Block {
    private final Block hostBlock;
    private static final Map<Block, Block> BLOCK_BY_HOST_BLOCK = Maps.newIdentityHashMap();

    public InfestedBlock(Block param0, Block.Properties param1) {
        super(param1);
        this.hostBlock = param0;
        BLOCK_BY_HOST_BLOCK.put(param0, this);
    }

    public Block getHostBlock() {
        return this.hostBlock;
    }

    public static boolean isCompatibleHostBlock(BlockState param0) {
        return BLOCK_BY_HOST_BLOCK.containsKey(param0.getBlock());
    }

    @Override
    public void spawnAfterBreak(BlockState param0, Level param1, BlockPos param2, ItemStack param3) {
        super.spawnAfterBreak(param0, param1, param2, param3);
        if (!param1.isClientSide
            && param1.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)
            && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, param3) == 0) {
            Silverfish var0 = EntityType.SILVERFISH.create(param1);
            var0.moveTo((double)param2.getX() + 0.5, (double)param2.getY(), (double)param2.getZ() + 0.5, 0.0F, 0.0F);
            param1.addFreshEntity(var0);
            var0.spawnAnim();
        }

    }

    public static BlockState stateByHostBlock(Block param0) {
        return BLOCK_BY_HOST_BLOCK.get(param0).defaultBlockState();
    }
}
