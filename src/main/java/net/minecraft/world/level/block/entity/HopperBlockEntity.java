package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

public class HopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper {
    public static final int MOVE_ITEM_SPEED = 8;
    public static final int HOPPER_CONTAINER_SIZE = 5;
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int cooldownTime = -1;
    private long tickedGameTime;

    public HopperBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.HOPPER, param0, param1);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(param0)) {
            ContainerHelper.loadAllItems(param0, this.items);
        }

        this.cooldownTime = param0.getInt("TransferCooldown");
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        if (!this.trySaveLootTable(param0)) {
            ContainerHelper.saveAllItems(param0, this.items);
        }

        param0.putInt("TransferCooldown", this.cooldownTime);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        this.unpackLootTable(null);
        return ContainerHelper.removeItem(this.getItems(), param0, param1);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        this.unpackLootTable(null);
        this.getItems().set(param0, param1);
        if (param1.getCount() > this.getMaxStackSize()) {
            param1.setCount(this.getMaxStackSize());
        }

    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.hopper");
    }

    public static void pushItemsTick(Level param0, BlockPos param1, BlockState param2, HopperBlockEntity param3) {
        --param3.cooldownTime;
        param3.tickedGameTime = param0.getGameTime();
        if (!param3.isOnCooldown()) {
            param3.setCooldown(0);
            tryMoveItems(param0, param1, param2, param3, () -> suckInItems(param0, param3));
        }

    }

    private static boolean tryMoveItems(Level param0, BlockPos param1, BlockState param2, HopperBlockEntity param3, BooleanSupplier param4) {
        if (param0.isClientSide) {
            return false;
        } else {
            if (!param3.isOnCooldown() && param2.getValue(HopperBlock.ENABLED)) {
                boolean var0 = false;
                if (!param3.isEmpty()) {
                    var0 = ejectItems(param0, param1, param2, param3);
                }

                if (!param3.inventoryFull()) {
                    var0 |= param4.getAsBoolean();
                }

                if (var0) {
                    param3.setCooldown(8);
                    setChanged(param0, param1, param2);
                    return true;
                }
            }

            return false;
        }
    }

    private boolean inventoryFull() {
        for(ItemStack var0 : this.items) {
            if (var0.isEmpty() || var0.getCount() != var0.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    private static boolean ejectItems(Level param0, BlockPos param1, BlockState param2, Container param3) {
        Container var0 = getAttachedContainer(param0, param1, param2);
        if (var0 == null) {
            return false;
        } else {
            Direction var1 = param2.getValue(HopperBlock.FACING).getOpposite();
            if (isFullContainer(var0, var1)) {
                return false;
            } else {
                for(int var2 = 0; var2 < param3.getContainerSize(); ++var2) {
                    if (!param3.getItem(var2).isEmpty()) {
                        ItemStack var3 = param3.getItem(var2).copy();
                        ItemStack var4 = addItem(param3, var0, param3.removeItem(var2, 1), var1);
                        if (var4.isEmpty()) {
                            var0.setChanged();
                            return true;
                        }

                        param3.setItem(var2, var3);
                    }
                }

                return false;
            }
        }
    }

    private static IntStream getSlots(Container param0, Direction param1) {
        return param0 instanceof WorldlyContainer
            ? IntStream.of(((WorldlyContainer)param0).getSlotsForFace(param1))
            : IntStream.range(0, param0.getContainerSize());
    }

    private static boolean isFullContainer(Container param0, Direction param1) {
        return getSlots(param0, param1).allMatch(param1x -> {
            ItemStack var0x = param0.getItem(param1x);
            return var0x.getCount() >= var0x.getMaxStackSize();
        });
    }

    private static boolean isEmptyContainer(Container param0, Direction param1) {
        return getSlots(param0, param1).allMatch(param1x -> param0.getItem(param1x).isEmpty());
    }

    public static boolean suckInItems(Level param0, Hopper param1) {
        Container var0 = getSourceContainer(param0, param1);
        if (var0 != null) {
            Direction var1 = Direction.DOWN;
            return isEmptyContainer(var0, var1) ? false : getSlots(var0, var1).anyMatch(param3 -> tryTakeInItemFromSlot(param1, var0, param3, var1));
        } else {
            for(ItemEntity var2 : getItemsAtAndAbove(param0, param1)) {
                if (addItem(param1, var2)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean tryTakeInItemFromSlot(Hopper param0, Container param1, int param2, Direction param3) {
        ItemStack var0 = param1.getItem(param2);
        if (!var0.isEmpty() && canTakeItemFromContainer(param0, param1, var0, param2, param3)) {
            ItemStack var1 = var0.copy();
            ItemStack var2 = addItem(param1, param0, param1.removeItem(param2, 1), null);
            if (var2.isEmpty()) {
                param1.setChanged();
                return true;
            }

            param1.setItem(param2, var1);
        }

        return false;
    }

    public static boolean addItem(Container param0, ItemEntity param1) {
        boolean var0 = false;
        ItemStack var1 = param1.getItem().copy();
        ItemStack var2 = addItem(null, param0, var1, null);
        if (var2.isEmpty()) {
            var0 = true;
            param1.setItem(ItemStack.EMPTY);
            param1.discard();
        } else {
            param1.setItem(var2);
        }

        return var0;
    }

    public static ItemStack addItem(@Nullable Container param0, Container param1, ItemStack param2, @Nullable Direction param3) {
        if (param1 instanceof WorldlyContainer var0 && param3 != null) {
            int[] var1 = var0.getSlotsForFace(param3);

            for(int var2 = 0; var2 < var1.length && !param2.isEmpty(); ++var2) {
                param2 = tryMoveInItem(param0, param1, param2, var1[var2], param3);
            }

            return param2;
        }

        int var3 = param1.getContainerSize();

        for(int var4 = 0; var4 < var3 && !param2.isEmpty(); ++var4) {
            param2 = tryMoveInItem(param0, param1, param2, var4, param3);
        }

        return param2;
    }

    private static boolean canPlaceItemInContainer(Container param0, ItemStack param1, int param2, @Nullable Direction param3) {
        if (!param0.canPlaceItem(param2, param1)) {
            return false;
        } else {
            if (param0 instanceof WorldlyContainer var0 && !var0.canPlaceItemThroughFace(param2, param1, param3)) {
                return false;
            }

            return true;
        }
    }

    private static boolean canTakeItemFromContainer(Container param0, Container param1, ItemStack param2, int param3, Direction param4) {
        if (!param1.canTakeItem(param0, param3, param2)) {
            return false;
        } else {
            if (param1 instanceof WorldlyContainer var0 && !var0.canTakeItemThroughFace(param3, param2, param4)) {
                return false;
            }

            return true;
        }
    }

    private static ItemStack tryMoveInItem(@Nullable Container param0, Container param1, ItemStack param2, int param3, @Nullable Direction param4) {
        ItemStack var0 = param1.getItem(param3);
        if (canPlaceItemInContainer(param1, param2, param3, param4)) {
            boolean var1 = false;
            boolean var2 = param1.isEmpty();
            if (var0.isEmpty()) {
                param1.setItem(param3, param2);
                param2 = ItemStack.EMPTY;
                var1 = true;
            } else if (canMergeItems(var0, param2)) {
                int var3 = param2.getMaxStackSize() - var0.getCount();
                int var4 = Math.min(param2.getCount(), var3);
                param2.shrink(var4);
                var0.grow(var4);
                var1 = var4 > 0;
            }

            if (var1) {
                if (var2 && param1 instanceof HopperBlockEntity var5 && !var5.isOnCustomCooldown()) {
                    int var6 = 0;
                    if (param0 instanceof HopperBlockEntity var7 && var5.tickedGameTime >= var7.tickedGameTime) {
                        var6 = 1;
                    }

                    var5.setCooldown(8 - var6);
                }

                param1.setChanged();
            }
        }

        return param2;
    }

    @Nullable
    private static Container getAttachedContainer(Level param0, BlockPos param1, BlockState param2) {
        Direction var0 = param2.getValue(HopperBlock.FACING);
        return getContainerAt(param0, param1.relative(var0));
    }

    @Nullable
    private static Container getSourceContainer(Level param0, Hopper param1) {
        return getContainerAt(param0, param1.getLevelX(), param1.getLevelY() + 1.0, param1.getLevelZ());
    }

    public static List<ItemEntity> getItemsAtAndAbove(Level param0, Hopper param1) {
        return param1.getSuckShape()
            .toAabbs()
            .stream()
            .flatMap(
                param2 -> param0.getEntitiesOfClass(
                            ItemEntity.class,
                            param2.move(param1.getLevelX() - 0.5, param1.getLevelY() - 0.5, param1.getLevelZ() - 0.5),
                            EntitySelector.ENTITY_STILL_ALIVE
                        )
                        .stream()
            )
            .collect(Collectors.toList());
    }

    @Nullable
    public static Container getContainerAt(Level param0, BlockPos param1) {
        return getContainerAt(param0, (double)param1.getX() + 0.5, (double)param1.getY() + 0.5, (double)param1.getZ() + 0.5);
    }

    @Nullable
    private static Container getContainerAt(Level param0, double param1, double param2, double param3) {
        Container var0 = null;
        BlockPos var1 = BlockPos.containing(param1, param2, param3);
        BlockState var2 = param0.getBlockState(var1);
        Block var3 = var2.getBlock();
        if (var3 instanceof WorldlyContainerHolder) {
            var0 = ((WorldlyContainerHolder)var3).getContainer(var2, param0, var1);
        } else if (var2.hasBlockEntity()) {
            BlockEntity var4 = param0.getBlockEntity(var1);
            if (var4 instanceof Container) {
                var0 = (Container)var4;
                if (var0 instanceof ChestBlockEntity && var3 instanceof ChestBlock) {
                    var0 = ChestBlock.getContainer((ChestBlock)var3, var2, param0, var1, true);
                }
            }
        }

        if (var0 == null) {
            List<Entity> var5 = param0.getEntities(
                (Entity)null,
                new AABB(param1 - 0.5, param2 - 0.5, param3 - 0.5, param1 + 0.5, param2 + 0.5, param3 + 0.5),
                EntitySelector.CONTAINER_ENTITY_SELECTOR
            );
            if (!var5.isEmpty()) {
                var0 = (Container)var5.get(param0.random.nextInt(var5.size()));
            }
        }

        return var0;
    }

    private static boolean canMergeItems(ItemStack param0, ItemStack param1) {
        return param0.getCount() <= param0.getMaxStackSize() && ItemStack.isSameItemSameTags(param0, param1);
    }

    @Override
    public double getLevelX() {
        return (double)this.worldPosition.getX() + 0.5;
    }

    @Override
    public double getLevelY() {
        return (double)this.worldPosition.getY() + 0.5;
    }

    @Override
    public double getLevelZ() {
        return (double)this.worldPosition.getZ() + 0.5;
    }

    private void setCooldown(int param0) {
        this.cooldownTime = param0;
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    private boolean isOnCustomCooldown() {
        return this.cooldownTime > 8;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> param0) {
        this.items = param0;
    }

    public static void entityInside(Level param0, BlockPos param1, BlockState param2, Entity param3, HopperBlockEntity param4) {
        if (param3 instanceof ItemEntity var0
            && !var0.getItem().isEmpty()
            && Shapes.joinIsNotEmpty(
                Shapes.create(param3.getBoundingBox().move((double)(-param1.getX()), (double)(-param1.getY()), (double)(-param1.getZ()))),
                param4.getSuckShape(),
                BooleanOp.AND
            )) {
            tryMoveItems(param0, param1, param2, param4, () -> addItem(param4, var0));
        }

    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return new HopperMenu(param0, param1, this);
    }
}
