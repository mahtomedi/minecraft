package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GenericItemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class CarriedBlocks {
    public static Optional<FallingBlockEntity> createEntityFromItemStack(Level param0, double param1, double param2, double param3, ItemStack param4) {
        return getBlockFromItemStack(param4).map(param4x -> new FallingBlockEntity(param0, param1, param2, param3, param4x));
    }

    public static BlockState normalizeBlockState(BlockState param0) {
        BlockState var0 = param0;

        while(true) {
            BlockState var1 = GenericItemBlock.unwrap(var0);
            if (var1 == null || var1 == var0) {
                return var0;
            }

            var0 = var1;
        }
    }

    public static ItemStack getItemStackFromBlock(BlockState param0) {
        if (param0.isAir()) {
            return ItemStack.EMPTY;
        } else {
            Item var0 = GenericItemBlock.itemFromGenericBlock(param0);
            if (var0 != null) {
                return var0.getDefaultInstance();
            } else {
                Block var1 = param0.getBlock();
                ItemStack var2 = var1.asItem().getDefaultInstance();
                var2.addTagElement("BlockStateTag", serializeStateProperties(param0));
                return var2;
            }
        }
    }

    public static Optional<BlockState> getBlockFromItemStack(ItemStack param0) {
        return param0.isEmpty() ? Optional.empty() : getBlockFromItem(param0.getItem()).map(param1 -> deserializeStateProperties(param0, param1));
    }

    private static Optional<BlockState> getBlockFromItem(Item param0) {
        if (param0 == Items.AIR) {
            return Optional.empty();
        } else {
            return param0 instanceof BlockItem var0
                ? Optional.of(var0.getBlock().defaultBlockState())
                : Optional.ofNullable(GenericItemBlock.genericBlockFromItem(param0));
        }
    }

    private static CompoundTag serializeStateProperties(BlockState param0) {
        CompoundTag var0 = new CompoundTag();

        for(Property<?> var1 : param0.getProperties()) {
            var0.putString(var1.getName(), serializeStateProperty(param0, var1));
        }

        return var0;
    }

    private static <T extends Comparable<T>> String serializeStateProperty(BlockState param0, Property<T> param1) {
        T var0 = param0.getValue(param1);
        return param1.getName(var0);
    }

    private static BlockState deserializeStateProperties(ItemStack param0, BlockState param1) {
        CompoundTag var0 = param0.getTag();
        if (var0 != null && var0.contains("BlockStateTag")) {
            BlockState var1 = param1;
            CompoundTag var2 = var0.getCompound("BlockStateTag");
            StateDefinition<Block, BlockState> var3 = param1.getBlock().getStateDefinition();

            for(String var4 : var2.getAllKeys()) {
                Property<?> var5 = var3.getProperty(var4);
                if (var5 != null) {
                    String var6 = var2.get(var4).getAsString();

                    try {
                        var1 = deserializeStateProperty(var1, var5, var6);
                    } catch (Throwable var11) {
                        var11.printStackTrace();
                    }
                }
            }

            return var1;
        } else {
            return param1;
        }
    }

    private static <T extends Comparable<T>> BlockState deserializeStateProperty(BlockState param0, Property<T> param1, String param2) {
        return param1.getValue(param2).map(param2x -> param0.setValue(param1, param2x)).orElse(param0);
    }
}
