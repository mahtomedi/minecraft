package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class FurnaceResultSlot extends Slot {
    private final Player player;
    private int removeCount;

    public FurnaceResultSlot(Player param0, Container param1, int param2, int param3, int param4) {
        super(param1, param2, param3, param4);
        this.player = param0;
    }

    @Override
    public boolean mayPlace(ItemStack param0) {
        return false;
    }

    @Override
    public ItemStack remove(int param0) {
        if (this.hasItem()) {
            this.removeCount += Math.min(param0, this.getItem().getCount());
        }

        return super.remove(param0);
    }

    @Override
    public void onTake(Player param0, ItemStack param1) {
        this.checkTakeAchievements(param1);
        super.onTake(param0, param1);
    }

    @Override
    protected void onQuickCraft(ItemStack param0, int param1) {
        this.removeCount += param1;
        this.checkTakeAchievements(param0);
    }

    @Override
    protected void checkTakeAchievements(ItemStack param0) {
        param0.onCraftedBy(this.player.level, this.player, this.removeCount);
        if (this.player instanceof ServerPlayer && this.container instanceof AbstractFurnaceBlockEntity) {
            ((AbstractFurnaceBlockEntity)this.container).awardUsedRecipesAndPopExperience((ServerPlayer)this.player);
        }

        this.removeCount = 0;
    }
}
