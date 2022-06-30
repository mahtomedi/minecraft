package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class DebugStickItem extends Item {
    public DebugStickItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState param0, Level param1, BlockPos param2, Player param3) {
        if (!param1.isClientSide) {
            this.handleInteraction(param3, param0, param1, param2, false, param3.getItemInHand(InteractionHand.MAIN_HAND));
        }

        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Player var0 = param0.getPlayer();
        Level var1 = param0.getLevel();
        if (!var1.isClientSide && var0 != null) {
            BlockPos var2 = param0.getClickedPos();
            if (!this.handleInteraction(var0, var1.getBlockState(var2), var1, var2, true, param0.getItemInHand())) {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.sidedSuccess(var1.isClientSide);
    }

    private boolean handleInteraction(Player param0, BlockState param1, LevelAccessor param2, BlockPos param3, boolean param4, ItemStack param5) {
        if (!param0.canUseGameMasterBlocks()) {
            return false;
        } else {
            Block var0 = param1.getBlock();
            StateDefinition<Block, BlockState> var1 = var0.getStateDefinition();
            Collection<Property<?>> var2 = var1.getProperties();
            String var3 = Registry.BLOCK.getKey(var0).toString();
            if (var2.isEmpty()) {
                message(param0, Component.translatable(this.getDescriptionId() + ".empty", var3));
                return false;
            } else {
                CompoundTag var4 = param5.getOrCreateTagElement("DebugProperty");
                String var5 = var4.getString(var3);
                Property<?> var6 = var1.getProperty(var5);
                if (param4) {
                    if (var6 == null) {
                        var6 = var2.iterator().next();
                    }

                    BlockState var7 = cycleState(param1, var6, param0.isSecondaryUseActive());
                    param2.setBlock(param3, var7, 18);
                    message(param0, Component.translatable(this.getDescriptionId() + ".update", var6.getName(), getNameHelper(var7, var6)));
                } else {
                    var6 = getRelative(var2, var6, param0.isSecondaryUseActive());
                    String var8 = var6.getName();
                    var4.putString(var3, var8);
                    message(param0, Component.translatable(this.getDescriptionId() + ".select", var8, getNameHelper(param1, var6)));
                }

                return true;
            }
        }
    }

    private static <T extends Comparable<T>> BlockState cycleState(BlockState param0, Property<T> param1, boolean param2) {
        return param0.setValue(param1, getRelative(param1.getPossibleValues(), param0.getValue(param1), param2));
    }

    private static <T> T getRelative(Iterable<T> param0, @Nullable T param1, boolean param2) {
        return (T)(param2 ? Util.findPreviousInIterable(param0, param1) : Util.findNextInIterable(param0, param1));
    }

    private static void message(Player param0, Component param1) {
        ((ServerPlayer)param0).sendSystemMessage(param1, true);
    }

    private static <T extends Comparable<T>> String getNameHelper(BlockState param0, Property<T> param1) {
        return param1.getName(param0.getValue(param1));
    }
}
