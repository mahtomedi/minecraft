package net.minecraft.world.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractContainerMenu {
    private final NonNullList<ItemStack> lastSlots = NonNullList.create();
    public final List<Slot> slots = Lists.newArrayList();
    private final List<DataSlot> dataSlots = Lists.newArrayList();
    @Nullable
    private final MenuType<?> menuType;
    public final int containerId;
    @OnlyIn(Dist.CLIENT)
    private short changeUid;
    private int quickcraftType = -1;
    private int quickcraftStatus;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();
    private final List<ContainerListener> containerListeners = Lists.newArrayList();
    private final Set<Player> unSynchedPlayers = Sets.newHashSet();

    protected AbstractContainerMenu(@Nullable MenuType<?> param0, int param1) {
        this.menuType = param0;
        this.containerId = param1;
    }

    protected static boolean stillValid(ContainerLevelAccess param0, Player param1, Block param2) {
        return param0.evaluate(
            (param2x, param3) -> param2x.getBlockState(param3).getBlock() != param2
                    ? false
                    : param1.distanceToSqr((double)param3.getX() + 0.5, (double)param3.getY() + 0.5, (double)param3.getZ() + 0.5) <= 64.0,
            true
        );
    }

    public MenuType<?> getType() {
        if (this.menuType == null) {
            throw new UnsupportedOperationException("Unable to construct this menu by type");
        } else {
            return this.menuType;
        }
    }

    protected static void checkContainerSize(Container param0, int param1) {
        int var0 = param0.getContainerSize();
        if (var0 < param1) {
            throw new IllegalArgumentException("Container size " + var0 + " is smaller than expected " + param1);
        }
    }

    protected static void checkContainerDataCount(ContainerData param0, int param1) {
        int var0 = param0.getCount();
        if (var0 < param1) {
            throw new IllegalArgumentException("Container data count " + var0 + " is smaller than expected " + param1);
        }
    }

    protected Slot addSlot(Slot param0) {
        param0.index = this.slots.size();
        this.slots.add(param0);
        this.lastSlots.add(ItemStack.EMPTY);
        return param0;
    }

    protected DataSlot addDataSlot(DataSlot param0) {
        this.dataSlots.add(param0);
        return param0;
    }

    protected void addDataSlots(ContainerData param0) {
        for(int var0 = 0; var0 < param0.getCount(); ++var0) {
            this.addDataSlot(DataSlot.forContainer(param0, var0));
        }

    }

    public void addSlotListener(ContainerListener param0) {
        if (!this.containerListeners.contains(param0)) {
            this.containerListeners.add(param0);
            param0.refreshContainer(this, this.getItems());
            this.broadcastChanges();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void removeSlotListener(ContainerListener param0) {
        this.containerListeners.remove(param0);
    }

    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> var0 = NonNullList.create();

        for(int var1 = 0; var1 < this.slots.size(); ++var1) {
            var0.add(this.slots.get(var1).getItem());
        }

        return var0;
    }

    public void broadcastChanges() {
        for(int var0 = 0; var0 < this.slots.size(); ++var0) {
            ItemStack var1 = this.slots.get(var0).getItem();
            ItemStack var2 = this.lastSlots.get(var0);
            if (!ItemStack.matches(var2, var1)) {
                var2 = var1.copy();
                this.lastSlots.set(var0, var2);

                for(ContainerListener var3 : this.containerListeners) {
                    var3.slotChanged(this, var0, var2);
                }
            }
        }

        for(int var4 = 0; var4 < this.dataSlots.size(); ++var4) {
            DataSlot var5 = this.dataSlots.get(var4);
            if (var5.checkAndClearUpdateFlag()) {
                for(ContainerListener var6 : this.containerListeners) {
                    var6.setContainerData(this, var4, var5.get());
                }
            }
        }

    }

    public boolean clickMenuButton(Player param0, int param1) {
        return false;
    }

    public Slot getSlot(int param0) {
        return this.slots.get(param0);
    }

    public ItemStack quickMoveStack(Player param0, int param1) {
        Slot var0 = this.slots.get(param1);
        return var0 != null ? var0.getItem() : ItemStack.EMPTY;
    }

    public ItemStack clicked(int param0, int param1, ClickType param2, Player param3) {
        ItemStack var0 = ItemStack.EMPTY;
        Inventory var1 = param3.inventory;
        if (param2 == ClickType.QUICK_CRAFT) {
            int var2 = this.quickcraftStatus;
            this.quickcraftStatus = getQuickcraftHeader(param1);
            if ((var2 != 1 || this.quickcraftStatus != 2) && var2 != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (var1.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = getQuickcraftType(param1);
                if (isValidQuickcraftType(this.quickcraftType, param3)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                Slot var3 = this.slots.get(param0);
                ItemStack var4 = var1.getCarried();
                if (var3 != null
                    && canItemQuickReplace(var3, var4, true)
                    && var3.mayPlace(var4)
                    && (this.quickcraftType == 2 || var4.getCount() > this.quickcraftSlots.size())
                    && this.canDragTo(var3)) {
                    this.quickcraftSlots.add(var3);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    ItemStack var5 = var1.getCarried().copy();
                    int var6 = var1.getCarried().getCount();

                    for(Slot var7 : this.quickcraftSlots) {
                        ItemStack var8 = var1.getCarried();
                        if (var7 != null
                            && canItemQuickReplace(var7, var8, true)
                            && var7.mayPlace(var8)
                            && (this.quickcraftType == 2 || var8.getCount() >= this.quickcraftSlots.size())
                            && this.canDragTo(var7)) {
                            ItemStack var9 = var5.copy();
                            int var10 = var7.hasItem() ? var7.getItem().getCount() : 0;
                            getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, var9, var10);
                            int var11 = Math.min(var9.getMaxStackSize(), var7.getMaxStackSize(var9));
                            if (var9.getCount() > var11) {
                                var9.setCount(var11);
                            }

                            var6 -= var9.getCount() - var10;
                            var7.set(var9);
                        }
                    }

                    var5.setCount(var6);
                    var1.setCarried(var5);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else if ((param2 == ClickType.PICKUP || param2 == ClickType.QUICK_MOVE) && (param1 == 0 || param1 == 1)) {
            if (param0 == -999) {
                if (!var1.getCarried().isEmpty()) {
                    if (param1 == 0) {
                        param3.drop(var1.getCarried(), true);
                        var1.setCarried(ItemStack.EMPTY);
                    }

                    if (param1 == 1) {
                        param3.drop(var1.getCarried().split(1), true);
                    }
                }
            } else if (param2 == ClickType.QUICK_MOVE) {
                if (param0 < 0) {
                    return ItemStack.EMPTY;
                }

                Slot var12 = this.slots.get(param0);
                if (var12 == null || !var12.mayPickup(param3)) {
                    return ItemStack.EMPTY;
                }

                for(ItemStack var13 = this.quickMoveStack(param3, param0);
                    !var13.isEmpty() && ItemStack.isSame(var12.getItem(), var13);
                    var13 = this.quickMoveStack(param3, param0)
                ) {
                    var0 = var13.copy();
                }
            } else {
                if (param0 < 0) {
                    return ItemStack.EMPTY;
                }

                Slot var14 = this.slots.get(param0);
                if (var14 != null) {
                    ItemStack var15 = var14.getItem();
                    ItemStack var16 = var1.getCarried();
                    if (!var15.isEmpty()) {
                        var0 = var15.copy();
                    }

                    if (var15.isEmpty()) {
                        if (!var16.isEmpty() && var14.mayPlace(var16)) {
                            int var17 = param1 == 0 ? var16.getCount() : 1;
                            if (var17 > var14.getMaxStackSize(var16)) {
                                var17 = var14.getMaxStackSize(var16);
                            }

                            var14.set(var16.split(var17));
                        }
                    } else if (var14.mayPickup(param3)) {
                        if (var16.isEmpty()) {
                            if (var15.isEmpty()) {
                                var14.set(ItemStack.EMPTY);
                                var1.setCarried(ItemStack.EMPTY);
                            } else {
                                int var18 = param1 == 0 ? var15.getCount() : (var15.getCount() + 1) / 2;
                                var1.setCarried(var14.remove(var18));
                                if (var15.isEmpty()) {
                                    var14.set(ItemStack.EMPTY);
                                }

                                var14.onTake(param3, var1.getCarried());
                            }
                        } else if (var14.mayPlace(var16)) {
                            if (consideredTheSameItem(var15, var16)) {
                                int var19 = param1 == 0 ? var16.getCount() : 1;
                                if (var19 > var14.getMaxStackSize(var16) - var15.getCount()) {
                                    var19 = var14.getMaxStackSize(var16) - var15.getCount();
                                }

                                if (var19 > var16.getMaxStackSize() - var15.getCount()) {
                                    var19 = var16.getMaxStackSize() - var15.getCount();
                                }

                                var16.shrink(var19);
                                var15.grow(var19);
                            } else if (var16.getCount() <= var14.getMaxStackSize(var16)) {
                                var14.set(var16);
                                var1.setCarried(var15);
                            }
                        } else if (var16.getMaxStackSize() > 1 && consideredTheSameItem(var15, var16) && !var15.isEmpty()) {
                            int var20 = var15.getCount();
                            if (var20 + var16.getCount() <= var16.getMaxStackSize()) {
                                var16.grow(var20);
                                var15 = var14.remove(var20);
                                if (var15.isEmpty()) {
                                    var14.set(ItemStack.EMPTY);
                                }

                                var14.onTake(param3, var1.getCarried());
                            }
                        }
                    }

                    var14.setChanged();
                }
            }
        } else if (param2 == ClickType.SWAP && param1 >= 0 && param1 < 9) {
            Slot var21 = this.slots.get(param0);
            ItemStack var22 = var1.getItem(param1);
            ItemStack var23 = var21.getItem();
            if (!var22.isEmpty() || !var23.isEmpty()) {
                if (var22.isEmpty()) {
                    if (var21.mayPickup(param3)) {
                        var1.setItem(param1, var23);
                        var21.onSwapCraft(var23.getCount());
                        var21.set(ItemStack.EMPTY);
                        var21.onTake(param3, var23);
                    }
                } else if (var23.isEmpty()) {
                    if (var21.mayPlace(var22)) {
                        int var24 = var21.getMaxStackSize(var22);
                        if (var22.getCount() > var24) {
                            var21.set(var22.split(var24));
                        } else {
                            var21.set(var22);
                            var1.setItem(param1, ItemStack.EMPTY);
                        }
                    }
                } else if (var21.mayPickup(param3) && var21.mayPlace(var22)) {
                    int var25 = var21.getMaxStackSize(var22);
                    if (var22.getCount() > var25) {
                        var21.set(var22.split(var25));
                        var21.onTake(param3, var23);
                        if (!var1.add(var23)) {
                            param3.drop(var23, true);
                        }
                    } else {
                        var21.set(var22);
                        var1.setItem(param1, var23);
                        var21.onTake(param3, var23);
                    }
                }
            }
        } else if (param2 == ClickType.CLONE && param3.abilities.instabuild && var1.getCarried().isEmpty() && param0 >= 0) {
            Slot var26 = this.slots.get(param0);
            if (var26 != null && var26.hasItem()) {
                ItemStack var27 = var26.getItem().copy();
                var27.setCount(var27.getMaxStackSize());
                var1.setCarried(var27);
            }
        } else if (param2 == ClickType.THROW && var1.getCarried().isEmpty() && param0 >= 0) {
            Slot var28 = this.slots.get(param0);
            if (var28 != null && var28.hasItem() && var28.mayPickup(param3)) {
                ItemStack var29 = var28.remove(param1 == 0 ? 1 : var28.getItem().getCount());
                var28.onTake(param3, var29);
                param3.drop(var29, true);
            }
        } else if (param2 == ClickType.PICKUP_ALL && param0 >= 0) {
            Slot var30 = this.slots.get(param0);
            ItemStack var31 = var1.getCarried();
            if (!var31.isEmpty() && (var30 == null || !var30.hasItem() || !var30.mayPickup(param3))) {
                int var32 = param1 == 0 ? 0 : this.slots.size() - 1;
                int var33 = param1 == 0 ? 1 : -1;

                for(int var34 = 0; var34 < 2; ++var34) {
                    for(int var35 = var32; var35 >= 0 && var35 < this.slots.size() && var31.getCount() < var31.getMaxStackSize(); var35 += var33) {
                        Slot var36 = this.slots.get(var35);
                        if (var36.hasItem() && canItemQuickReplace(var36, var31, true) && var36.mayPickup(param3) && this.canTakeItemForPickAll(var31, var36)) {
                            ItemStack var37 = var36.getItem();
                            if (var34 != 0 || var37.getCount() != var37.getMaxStackSize()) {
                                int var38 = Math.min(var31.getMaxStackSize() - var31.getCount(), var37.getCount());
                                ItemStack var39 = var36.remove(var38);
                                var31.grow(var38);
                                if (var39.isEmpty()) {
                                    var36.set(ItemStack.EMPTY);
                                }

                                var36.onTake(param3, var39);
                            }
                        }
                    }
                }
            }

            this.broadcastChanges();
        }

        return var0;
    }

    public static boolean consideredTheSameItem(ItemStack param0, ItemStack param1) {
        return param0.getItem() == param1.getItem() && ItemStack.tagMatches(param0, param1);
    }

    public boolean canTakeItemForPickAll(ItemStack param0, Slot param1) {
        return true;
    }

    public void removed(Player param0) {
        Inventory var0 = param0.inventory;
        if (!var0.getCarried().isEmpty()) {
            param0.drop(var0.getCarried(), false);
            var0.setCarried(ItemStack.EMPTY);
        }

    }

    protected void clearContainer(Player param0, Level param1, Container param2) {
        if (!param0.isAlive() || param0 instanceof ServerPlayer && ((ServerPlayer)param0).hasDisconnected()) {
            for(int var0 = 0; var0 < param2.getContainerSize(); ++var0) {
                param0.drop(param2.removeItemNoUpdate(var0), false);
            }

        } else {
            for(int var1 = 0; var1 < param2.getContainerSize(); ++var1) {
                param0.inventory.placeItemBackInInventory(param1, param2.removeItemNoUpdate(var1));
            }

        }
    }

    public void slotsChanged(Container param0) {
        this.broadcastChanges();
    }

    public void setItem(int param0, ItemStack param1) {
        this.getSlot(param0).set(param1);
    }

    @OnlyIn(Dist.CLIENT)
    public void setAll(List<ItemStack> param0) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            this.getSlot(var0).set(param0.get(var0));
        }

    }

    public void setData(int param0, int param1) {
        this.dataSlots.get(param0).set(param1);
    }

    @OnlyIn(Dist.CLIENT)
    public short backup(Inventory param0) {
        ++this.changeUid;
        return this.changeUid;
    }

    public boolean isSynched(Player param0) {
        return !this.unSynchedPlayers.contains(param0);
    }

    public void setSynched(Player param0, boolean param1) {
        if (param1) {
            this.unSynchedPlayers.remove(param0);
        } else {
            this.unSynchedPlayers.add(param0);
        }

    }

    public abstract boolean stillValid(Player var1);

    protected boolean moveItemStackTo(ItemStack param0, int param1, int param2, boolean param3) {
        boolean var0 = false;
        int var1 = param1;
        if (param3) {
            var1 = param2 - 1;
        }

        if (param0.isStackable()) {
            while(!param0.isEmpty()) {
                if (param3) {
                    if (var1 < param1) {
                        break;
                    }
                } else if (var1 >= param2) {
                    break;
                }

                Slot var2 = this.slots.get(var1);
                ItemStack var3 = var2.getItem();
                if (!var3.isEmpty() && consideredTheSameItem(param0, var3)) {
                    int var4 = var3.getCount() + param0.getCount();
                    if (var4 <= param0.getMaxStackSize()) {
                        param0.setCount(0);
                        var3.setCount(var4);
                        var2.setChanged();
                        var0 = true;
                    } else if (var3.getCount() < param0.getMaxStackSize()) {
                        param0.shrink(param0.getMaxStackSize() - var3.getCount());
                        var3.setCount(param0.getMaxStackSize());
                        var2.setChanged();
                        var0 = true;
                    }
                }

                if (param3) {
                    --var1;
                } else {
                    ++var1;
                }
            }
        }

        if (!param0.isEmpty()) {
            if (param3) {
                var1 = param2 - 1;
            } else {
                var1 = param1;
            }

            while(true) {
                if (param3) {
                    if (var1 < param1) {
                        break;
                    }
                } else if (var1 >= param2) {
                    break;
                }

                Slot var5 = this.slots.get(var1);
                ItemStack var6 = var5.getItem();
                if (var6.isEmpty() && var5.mayPlace(param0)) {
                    if (param0.getCount() > var5.getMaxStackSize()) {
                        var5.set(param0.split(var5.getMaxStackSize()));
                    } else {
                        var5.set(param0.split(param0.getCount()));
                    }

                    var5.setChanged();
                    var0 = true;
                    break;
                }

                if (param3) {
                    --var1;
                } else {
                    ++var1;
                }
            }
        }

        return var0;
    }

    public static int getQuickcraftType(int param0) {
        return param0 >> 2 & 3;
    }

    public static int getQuickcraftHeader(int param0) {
        return param0 & 3;
    }

    @OnlyIn(Dist.CLIENT)
    public static int getQuickcraftMask(int param0, int param1) {
        return param0 & 3 | (param1 & 3) << 2;
    }

    public static boolean isValidQuickcraftType(int param0, Player param1) {
        if (param0 == 0) {
            return true;
        } else if (param0 == 1) {
            return true;
        } else {
            return param0 == 2 && param1.abilities.instabuild;
        }
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    public static boolean canItemQuickReplace(@Nullable Slot param0, ItemStack param1, boolean param2) {
        boolean var0 = param0 == null || !param0.hasItem();
        if (!var0 && param1.sameItem(param0.getItem()) && ItemStack.tagMatches(param0.getItem(), param1)) {
            return param0.getItem().getCount() + (param2 ? 0 : param1.getCount()) <= param1.getMaxStackSize();
        } else {
            return var0;
        }
    }

    public static void getQuickCraftSlotCount(Set<Slot> param0, int param1, ItemStack param2, int param3) {
        switch(param1) {
            case 0:
                param2.setCount(Mth.floor((float)param2.getCount() / (float)param0.size()));
                break;
            case 1:
                param2.setCount(1);
                break;
            case 2:
                param2.setCount(param2.getItem().getMaxStackSize());
        }

        param2.grow(param3);
    }

    public boolean canDragTo(Slot param0) {
        return true;
    }

    public static int getRedstoneSignalFromBlockEntity(@Nullable BlockEntity param0) {
        return param0 instanceof Container ? getRedstoneSignalFromContainer((Container)param0) : 0;
    }

    public static int getRedstoneSignalFromContainer(@Nullable Container param0) {
        if (param0 == null) {
            return 0;
        } else {
            int var0 = 0;
            float var1 = 0.0F;

            for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
                ItemStack var3 = param0.getItem(var2);
                if (!var3.isEmpty()) {
                    var1 += (float)var3.getCount() / (float)Math.min(param0.getMaxStackSize(), var3.getMaxStackSize());
                    ++var0;
                }
            }

            var1 /= (float)param0.getContainerSize();
            return Mth.floor(var1 * 14.0F) + (var0 > 0 ? 1 : 0);
        }
    }
}
