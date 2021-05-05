package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class InfestedBlock extends Block {
    private final Block hostBlock;
    private static final Map<Block, Block> BLOCK_BY_HOST_BLOCK = Maps.newIdentityHashMap();
    private static final Map<BlockState, BlockState> HOST_TO_INFESTED_STATES = Maps.newIdentityHashMap();
    private static final Map<BlockState, BlockState> INFESTED_TO_HOST_STATES = Maps.newIdentityHashMap();

    public InfestedBlock(Block param0, BlockBehaviour.Properties param1) {
        super(param1.destroyTime(param0.defaultDestroyTime() / 2.0F).explosionResistance(0.75F));
        this.hostBlock = param0;
        BLOCK_BY_HOST_BLOCK.put(param0, this);
    }

    public Block getHostBlock() {
        return this.hostBlock;
    }

    public static boolean isCompatibleHostBlock(BlockState param0) {
        return BLOCK_BY_HOST_BLOCK.containsKey(param0.getBlock());
    }

    private void spawnInfestation(ServerLevel param0, BlockPos param1) {
        Silverfish var0 = EntityType.SILVERFISH.create(param0);
        var0.moveTo((double)param1.getX() + 0.5, (double)param1.getY(), (double)param1.getZ() + 0.5, 0.0F, 0.0F);
        param0.addFreshEntity(var0);
        var0.spawnAnim();
    }

    @Override
    public void spawnAfterBreak(BlockState param0, ServerLevel param1, BlockPos param2, ItemStack param3) {
        super.spawnAfterBreak(param0, param1, param2, param3);
        if (param1.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, param3) == 0) {
            this.spawnInfestation(param1, param2);
        }

    }

    @Override
    public void wasExploded(Level param0, BlockPos param1, Explosion param2) {
        if (param0 instanceof ServerLevel) {
            this.spawnInfestation((ServerLevel)param0, param1);
        }

    }

    public static BlockState infestedStateByHost(BlockState param0) {
        return getNewStateWithProperties(HOST_TO_INFESTED_STATES, param0, () -> BLOCK_BY_HOST_BLOCK.get(param0.getBlock()).defaultBlockState());
    }

    public BlockState hostStateByInfested(BlockState param0) {
        return getNewStateWithProperties(INFESTED_TO_HOST_STATES, param0, () -> this.getHostBlock().defaultBlockState());
    }

    private static BlockState getNewStateWithProperties(Map<BlockState, BlockState> param0, BlockState param1, Supplier<BlockState> param2) {
        return param0.computeIfAbsent(param1, param1x -> {
            BlockState var0x = param2.get();

            for(Property var1x : param1x.getProperties()) {
                var0x = var0x.hasProperty(var1x) ? var0x.setValue(var1x, param1x.getValue(var1x)) : var0x;
            }

            return var0x;
        });
    }
}
