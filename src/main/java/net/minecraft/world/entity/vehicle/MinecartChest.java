package net.minecraft.world.entity.vehicle;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class MinecartChest extends AbstractMinecartContainer {
    public MinecartChest(EntityType<? extends MinecartChest> param0, Level param1) {
        super(param0, param1);
    }

    public MinecartChest(Level param0, double param1, double param2, double param3) {
        super(EntityType.CHEST_MINECART, param1, param2, param3, param0);
    }

    @Override
    protected Item getDropItem() {
        return Items.CHEST_MINECART;
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.CHEST;
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
    }

    @Override
    public int getDefaultDisplayOffset() {
        return 8;
    }

    @Override
    public AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return ChestMenu.threeRows(param0, param1, this);
    }

    @Override
    public void stopOpen(Player param0) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(param0));
    }

    @Override
    public InteractionResult interact(Player param0, InteractionHand param1) {
        InteractionResult var0 = this.interactWithContainerVehicle(param0);
        if (var0.consumesAction()) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, param0);
            PiglinAi.angerNearbyPiglins(param0, true);
        }

        return var0;
    }
}
