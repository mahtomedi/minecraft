package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WorkAtComposter extends WorkAtPoi {
    private static final List<Item> COMPOSTABLE_ITEMS = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);

    @Override
    protected void useWorkstation(ServerLevel param0, Villager param1) {
        Optional<GlobalPos> var0 = param1.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (var0.isPresent()) {
            GlobalPos var1 = var0.get();
            BlockState var2 = param0.getBlockState(var1.pos());
            Block var3 = var2.getBlock();
            if (var3 == Blocks.COMPOSTER) {
                this.makeBread(param1);
                this.compostItems(param0, param1, var1, var2);
            }

        }
    }

    private void compostItems(ServerLevel param0, Villager param1, GlobalPos param2, BlockState param3) {
        if (param3.getValue(ComposterBlock.LEVEL) == 8) {
            param3 = ComposterBlock.extractProduce(param3, param0, param2.pos());
        }

        int var0 = 20;
        int var1 = 10;
        int[] var2 = new int[COMPOSTABLE_ITEMS.size()];
        SimpleContainer var3 = param1.getInventory();
        int var4 = var3.getContainerSize();

        for(int var5 = var4 - 1; var5 >= 0 && var0 > 0; --var5) {
            ItemStack var6 = var3.getItem(var5);
            int var7 = COMPOSTABLE_ITEMS.indexOf(var6.getItem());
            if (var7 != -1) {
                int var8 = var6.getCount();
                int var9 = var2[var7] + var8;
                var2[var7] = var9;
                int var10 = Math.min(Math.min(var9 - 10, var0), var8);
                if (var10 > 0) {
                    var0 -= var10;

                    for(int var11 = 0; var11 < var10; ++var11) {
                        param3 = ComposterBlock.insertItem(param3, param0, var6, param2.pos());
                        if (param3.getValue(ComposterBlock.LEVEL) == 7) {
                            return;
                        }
                    }
                }
            }
        }

    }

    private void makeBread(Villager param0) {
        SimpleContainer var0 = param0.getInventory();
        if (var0.countItem(Items.BREAD) <= 36) {
            int var1 = var0.countItem(Items.WHEAT);
            int var2 = 3;
            int var3 = 3;
            int var4 = Math.min(3, var1 / 3);
            if (var4 != 0) {
                int var5 = var4 * 3;
                var0.removeItemType(Items.WHEAT, var5);
                ItemStack var6 = var0.addItem(new ItemStack(Items.BREAD, var4));
                if (!var6.isEmpty()) {
                    param0.spawnAtLocation(var6, 0.5F);
                }

            }
        }
    }
}
