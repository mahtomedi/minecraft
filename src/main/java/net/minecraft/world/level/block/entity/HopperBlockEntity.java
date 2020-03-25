package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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

public class HopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper, TickableBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int cooldownTime = -1;
    private long tickedGameTime;

    public HopperBlockEntity() {
        super(BlockEntityType.HOPPER);
    }

    @Override
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(param1)) {
            ContainerHelper.loadAllItems(param1, this.items);
        }

        this.cooldownTime = param1.getInt("TransferCooldown");
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        if (!this.trySaveLootTable(param0)) {
            ContainerHelper.saveAllItems(param0, this.items);
        }

        param0.putInt("TransferCooldown", this.cooldownTime);
        return param0;
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
        return new TranslatableComponent("container.hopper");
    }

    @Override
    public void tick() {
        if (this.level != null && !this.level.isClientSide) {
            --this.cooldownTime;
            this.tickedGameTime = this.level.getGameTime();
            if (!this.isOnCooldown()) {
                this.setCooldown(0);
                this.tryMoveItems(() -> suckInItems(this));
            }

        }
    }

    private boolean tryMoveItems(Supplier<Boolean> param0) {
        if (this.level != null && !this.level.isClientSide) {
            if (!this.isOnCooldown() && this.getBlockState().getValue(HopperBlock.ENABLED)) {
                boolean var0 = false;
                if (!this.isEmpty()) {
                    var0 = this.ejectItems();
                }

                if (!this.inventoryFull()) {
                    var0 |= param0.get();
                }

                if (var0) {
                    this.setCooldown(8);
                    this.setChanged();
                    return true;
                }
            }

            return false;
        } else {
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

    private boolean ejectItems() {
        Container var0 = this.getAttachedContainer();
        if (var0 == null) {
            return false;
        } else {
            Direction var1 = this.getBlockState().getValue(HopperBlock.FACING).getOpposite();
            if (this.isFullContainer(var0, var1)) {
                return false;
            } else {
                for(int var2 = 0; var2 < this.getContainerSize(); ++var2) {
                    if (!this.getItem(var2).isEmpty()) {
                        ItemStack var3 = this.getItem(var2).copy();
                        ItemStack var4 = addItem(this, var0, this.removeItem(var2, 1), var1);
                        if (var4.isEmpty()) {
                            var0.setChanged();
                            return true;
                        }

                        this.setItem(var2, var3);
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

    private boolean isFullContainer(Container param0, Direction param1) {
        return getSlots(param0, param1).allMatch(param1x -> {
            ItemStack var0 = param0.getItem(param1x);
            return var0.getCount() >= var0.getMaxStackSize();
        });
    }

    private static boolean isEmptyContainer(Container param0, Direction param1) {
        return getSlots(param0, param1).allMatch(param1x -> param0.getItem(param1x).isEmpty());
    }

    public static boolean suckInItems(Hopper param0) {
        Container var0 = getSourceContainer(param0);
        if (var0 != null) {
            Direction var1 = Direction.DOWN;
            return isEmptyContainer(var0, var1) ? false : getSlots(var0, var1).anyMatch(param3 -> tryTakeInItemFromSlot(param0, var0, param3, var1));
        } else {
            for(ItemEntity var2 : getItemsAtAndAbove(param0)) {
                if (addItem(param0, var2)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean tryTakeInItemFromSlot(Hopper param0, Container param1, int param2, Direction param3) {
        ItemStack var0 = param1.getItem(param2);
        if (!var0.isEmpty() && canTakeItemFromContainer(param1, var0, param2, param3)) {
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
            param1.remove();
        } else {
            param1.setItem(var2);
        }

        return var0;
    }

    public static ItemStack addItem(@Nullable Container param0, Container param1, ItemStack param2, @Nullable Direction param3) {
        if (param1 instanceof WorldlyContainer && param3 != null) {
            WorldlyContainer var0 = (WorldlyContainer)param1;
            int[] var1 = var0.getSlotsForFace(param3);

            for(int var2 = 0; var2 < var1.length && !param2.isEmpty(); ++var2) {
                param2 = tryMoveInItem(param0, param1, param2, var1[var2], param3);
            }
        } else {
            int var3 = param1.getContainerSize();

            for(int var4 = 0; var4 < var3 && !param2.isEmpty(); ++var4) {
                param2 = tryMoveInItem(param0, param1, param2, var4, param3);
            }
        }

        return param2;
    }

    private static boolean canPlaceItemInContainer(Container param0, ItemStack param1, int param2, @Nullable Direction param3) {
        if (!param0.canPlaceItem(param2, param1)) {
            return false;
        } else {
            return !(param0 instanceof WorldlyContainer) || ((WorldlyContainer)param0).canPlaceItemThroughFace(param2, param1, param3);
        }
    }

    private static boolean canTakeItemFromContainer(Container param0, ItemStack param1, int param2, Direction param3) {
        return !(param0 instanceof WorldlyContainer) || ((WorldlyContainer)param0).canTakeItemThroughFace(param2, param1, param3);
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
                if (var2 && param1 instanceof HopperBlockEntity) {
                    HopperBlockEntity var5 = (HopperBlockEntity)param1;
                    if (!var5.isOnCustomCooldown()) {
                        int var6 = 0;
                        if (param0 instanceof HopperBlockEntity) {
                            HopperBlockEntity var7 = (HopperBlockEntity)param0;
                            if (var5.tickedGameTime >= var7.tickedGameTime) {
                                var6 = 1;
                            }
                        }

                        var5.setCooldown(8 - var6);
                    }
                }

                param1.setChanged();
            }
        }

        return param2;
    }

    @Nullable
    private Container getAttachedContainer() {
        Direction var0 = this.getBlockState().getValue(HopperBlock.FACING);
        return getContainerAt(this.getLevel(), this.worldPosition.relative(var0));
    }

    @Nullable
    public static Container getSourceContainer(Hopper param0) {
        return getContainerAt(param0.getLevel(), param0.getLevelX(), param0.getLevelY() + 1.0, param0.getLevelZ());
    }

    public static List<ItemEntity> getItemsAtAndAbove(Hopper param0) {
        return param0.getSuckShape()
            .toAabbs()
            .stream()
            .flatMap(
                param1 -> param0.getLevel()
                        .getEntitiesOfClass(
                            ItemEntity.class,
                            param1.move(param0.getLevelX() - 0.5, param0.getLevelY() - 0.5, param0.getLevelZ() - 0.5),
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
    public static Container getContainerAt(Level param0, double param1, double param2, double param3) {
        Container var0 = null;
        BlockPos var1 = new BlockPos(param1, param2, param3);
        BlockState var2 = param0.getBlockState(var1);
        Block var3 = var2.getBlock();
        if (var3 instanceof WorldlyContainerHolder) {
            var0 = ((WorldlyContainerHolder)var3).getContainer(var2, param0, var1);
        } else if (var3.isEntityBlock()) {
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
        if (param0.getItem() != param1.getItem()) {
            return false;
        } else if (param0.getDamageValue() != param1.getDamageValue()) {
            return false;
        } else if (param0.getCount() > param0.getMaxStackSize()) {
            return false;
        } else {
            return ItemStack.tagMatches(param0, param1);
        }
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

    public void entityInside(Entity param0) {
        if (param0 instanceof ItemEntity) {
            BlockPos var0 = this.getBlockPos();
            if (Shapes.joinIsNotEmpty(
                Shapes.create(param0.getBoundingBox().move((double)(-var0.getX()), (double)(-var0.getY()), (double)(-var0.getZ()))),
                this.getSuckShape(),
                BooleanOp.AND
            )) {
                this.tryMoveItems(() -> addItem(this, (ItemEntity)param0));
            }
        }

    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return new HopperMenu(param0, param1, this);
    }
}
