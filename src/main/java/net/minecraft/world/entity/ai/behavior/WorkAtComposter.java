package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
            if (var2.is(Blocks.COMPOSTER)) {
                this.makeBread(param1);
                this.compostItems(param0, param1, var1, var2);
            }

        }
    }

    private void compostItems(ServerLevel param0, Villager param1, GlobalPos param2, BlockState param3) {
        BlockPos var0 = param2.pos();
        if (param3.getValue(ComposterBlock.LEVEL) == 8) {
            param3 = ComposterBlock.extractProduce(param1, param3, param0, var0);
        }

        int var1 = 20;
        int var2 = 10;
        int[] var3 = new int[COMPOSTABLE_ITEMS.size()];
        SimpleContainer var4 = param1.getInventory();
        int var5 = var4.getContainerSize();
        BlockState var6 = param3;

        for(int var7 = var5 - 1; var7 >= 0 && var1 > 0; --var7) {
            ItemStack var8 = var4.getItem(var7);
            int var9 = COMPOSTABLE_ITEMS.indexOf(var8.getItem());
            if (var9 != -1) {
                int var10 = var8.getCount();
                int var11 = var3[var9] + var10;
                var3[var9] = var11;
                int var12 = Math.min(Math.min(var11 - 10, var1), var10);
                if (var12 > 0) {
                    var1 -= var12;

                    for(int var13 = 0; var13 < var12; ++var13) {
                        var6 = ComposterBlock.insertItem(param1, var6, param0, var8, var0);
                        if (var6.getValue(ComposterBlock.LEVEL) == 7) {
                            this.spawnComposterFillEffects(param0, param3, var0, var6);
                            return;
                        }
                    }
                }
            }
        }

        this.spawnComposterFillEffects(param0, param3, var0, var6);
    }

    private void spawnComposterFillEffects(ServerLevel param0, BlockState param1, BlockPos param2, BlockState param3) {
        param0.levelEvent(1500, param2, param3 != param1 ? 1 : 0);
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
