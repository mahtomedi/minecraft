package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

public abstract class ContainerOpenersCounter {
    private int openCount;

    protected abstract void onOpen(Level var1, BlockPos var2, BlockState var3);

    protected abstract void onClose(Level var1, BlockPos var2, BlockState var3);

    protected abstract void openerCountChanged(Level var1, BlockPos var2, BlockState var3, int var4, int var5);

    protected abstract boolean isOwnContainer(Player var1);

    public void incrementOpeners(Level param0, BlockPos param1, BlockState param2) {
        int var0 = this.openCount++;
        if (var0 == 0) {
            this.onOpen(param0, param1, param2);
            scheduleRecheck(param0, param1, param2);
        }

        this.openerCountChanged(param0, param1, param2, var0, this.openCount);
    }

    public void decrementOpeners(Level param0, BlockPos param1, BlockState param2) {
        int var0 = this.openCount--;
        if (this.openCount == 0) {
            this.onClose(param0, param1, param2);
        }

        this.openerCountChanged(param0, param1, param2, var0, this.openCount);
    }

    private int getOpenCount(Level param0, BlockPos param1) {
        int var0 = param1.getX();
        int var1 = param1.getY();
        int var2 = param1.getZ();
        float var3 = 5.0F;
        AABB var4 = new AABB(
            (double)((float)var0 - 5.0F),
            (double)((float)var1 - 5.0F),
            (double)((float)var2 - 5.0F),
            (double)((float)(var0 + 1) + 5.0F),
            (double)((float)(var1 + 1) + 5.0F),
            (double)((float)(var2 + 1) + 5.0F)
        );
        return param0.getEntities(EntityTypeTest.forClass(Player.class), var4, this::isOwnContainer).size();
    }

    public void recheckOpeners(Level param0, BlockPos param1, BlockState param2) {
        int var0 = this.getOpenCount(param0, param1);
        int var1 = this.openCount;
        if (var1 != var0) {
            boolean var2 = var0 != 0;
            boolean var3 = var1 != 0;
            if (var2 && !var3) {
                this.onOpen(param0, param1, param2);
            } else if (!var2) {
                this.onClose(param0, param1, param2);
            }

            this.openCount = var0;
        }

        this.openerCountChanged(param0, param1, param2, var1, var0);
        if (var0 > 0) {
            scheduleRecheck(param0, param1, param2);
        }

    }

    public int getOpenerCount() {
        return this.openCount;
    }

    private static void scheduleRecheck(Level param0, BlockPos param1, BlockState param2) {
        param0.getBlockTicks().scheduleTick(param1, param2.getBlock(), 5);
    }
}
