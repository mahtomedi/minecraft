package net.minecraft.world.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractContainerMenu {
    private final NonNullList<ItemStack> lastSlots = NonNullList.create();
    public final NonNullList<Slot> slots = NonNullList.create();
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
            (param2x, param3) -> !param2x.getBlockState(param3).is(param2)
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
                ItemStack var3 = var1.copy();
                this.lastSlots.set(var0, var3);

                for(ContainerListener var4 : this.containerListeners) {
                    var4.slotChanged(this, var0, var3);
                }
            }
        }

        for(int var5 = 0; var5 < this.dataSlots.size(); ++var5) {
            DataSlot var6 = this.dataSlots.get(var5);
            if (var6.checkAndClearUpdateFlag()) {
                for(ContainerListener var7 : this.containerListeners) {
                    var7.setContainerData(this, var5, var6.get());
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
        return this.slots.get(param1).getItem();
    }

    public ItemStack clicked(int param0, int param1, ClickType param2, Player param3) {
        try {
            return this.doClick(param0, param1, param2, param3);
        } catch (Exception var8) {
            CrashReport var1 = CrashReport.forThrowable(var8, "Container click");
            CrashReportCategory var2 = var1.addCategory("Click info");
            var2.setDetail("Menu Type", () -> this.menuType != null ? Registry.MENU.getKey(this.menuType).toString() : "<no type>");
            var2.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
            var2.setDetail("Slot Count", this.slots.size());
            var2.setDetail("Slot", param0);
            var2.setDetail("Button", param1);
            var2.setDetail("Type", param2);
            throw new ReportedException(var1);
        }
    }

    private ItemStack doClick(int param0, int param1, ClickType param2, Player param3) {
        ItemStack var0 = ItemStack.EMPTY;
        Inventory var1 = param3.getInventory();
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
                if (canItemQuickReplace(var3, var4, true)
                    && var3.mayPlace(var4)
                    && (this.quickcraftType == 2 || var4.getCount() > this.quickcraftSlots.size())
                    && this.canDragTo(var3)) {
                    this.quickcraftSlots.add(var3);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (this.quickcraftSlots.size() == 1) {
                        int var5 = this.quickcraftSlots.iterator().next().index;
                        this.resetQuickCraft();
                        return this.doClick(var5, this.quickcraftType, ClickType.PICKUP, param3);
                    }

                    ItemStack var6 = var1.getCarried().copy();
                    int var7 = var1.getCarried().getCount();

                    for(Slot var8 : this.quickcraftSlots) {
                        ItemStack var9 = var1.getCarried();
                        if (var8 != null
                            && canItemQuickReplace(var8, var9, true)
                            && var8.mayPlace(var9)
                            && (this.quickcraftType == 2 || var9.getCount() >= this.quickcraftSlots.size())
                            && this.canDragTo(var8)) {
                            ItemStack var10 = var6.copy();
                            int var11 = var8.hasItem() ? var8.getItem().getCount() : 0;
                            getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, var10, var11);
                            int var12 = Math.min(var10.getMaxStackSize(), var8.getMaxStackSize(var10));
                            if (var10.getCount() > var12) {
                                var10.setCount(var12);
                            }

                            var7 -= var10.getCount() - var11;
                            var8.set(var10);
                        }
                    }

                    var6.setCount(var7);
                    var1.setCarried(var6);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else if ((param2 == ClickType.PICKUP || param2 == ClickType.QUICK_MOVE) && (param1 == 0 || param1 == 1)) {
            ClickAction var13 = param1 == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
            if (param0 == -999) {
                if (!var1.getCarried().isEmpty()) {
                    if (var13 == ClickAction.PRIMARY) {
                        param3.drop(var1.getCarried(), true);
                        var1.setCarried(ItemStack.EMPTY);
                    } else {
                        param3.drop(var1.getCarried().split(1), true);
                    }
                }
            } else if (param2 == ClickType.QUICK_MOVE) {
                if (param0 < 0) {
                    return ItemStack.EMPTY;
                }

                Slot var14 = this.slots.get(param0);
                if (!var14.mayPickup(param3)) {
                    return ItemStack.EMPTY;
                }

                for(ItemStack var15 = this.quickMoveStack(param3, param0);
                    !var15.isEmpty() && ItemStack.isSame(var14.getItem(), var15);
                    var15 = this.quickMoveStack(param3, param0)
                ) {
                    var0 = var15.copy();
                }
            } else {
                if (param0 < 0) {
                    return ItemStack.EMPTY;
                }

                Slot var16 = this.slots.get(param0);
                ItemStack var17 = var16.getItem();
                ItemStack var18 = var1.getCarried();
                if (!var17.isEmpty()) {
                    var0 = var17.copy();
                }

                if (!var18.overrideStackedOnOther(var16, var13, var1) && !var17.overrideOtherStackedOnMe(var18, var16, var13, var1)) {
                    if (var17.isEmpty()) {
                        if (!var18.isEmpty()) {
                            int var19 = var13 == ClickAction.PRIMARY ? var18.getCount() : 1;
                            var1.setCarried(var16.safeInsert(var18, var19));
                        }
                    } else if (var16.mayPickup(param3)) {
                        if (var18.isEmpty()) {
                            int var20 = var13 == ClickAction.PRIMARY ? var17.getCount() : (var17.getCount() + 1) / 2;
                            var1.setCarried(var16.safeTake(var20, Integer.MAX_VALUE, param3));
                        } else if (var16.mayPlace(var18)) {
                            if (ItemStack.isSameItemSameTags(var17, var18)) {
                                int var21 = var13 == ClickAction.PRIMARY ? var18.getCount() : 1;
                                var1.setCarried(var16.safeInsert(var18, var21));
                            } else if (var18.getCount() <= var16.getMaxStackSize(var18)) {
                                var16.set(var18);
                                var1.setCarried(var17);
                            }
                        } else if (ItemStack.isSameItemSameTags(var17, var18)) {
                            ItemStack var22 = var16.safeTake(var17.getCount(), var18.getMaxStackSize() - var18.getCount(), param3);
                            var18.grow(var22.getCount());
                        }
                    }
                }

                var16.setChanged();
            }
        } else if (param2 == ClickType.SWAP) {
            Slot var23 = this.slots.get(param0);
            ItemStack var24 = var1.getItem(param1);
            ItemStack var25 = var23.getItem();
            if (!var24.isEmpty() || !var25.isEmpty()) {
                if (var24.isEmpty()) {
                    if (var23.mayPickup(param3)) {
                        var1.setItem(param1, var25);
                        var23.onSwapCraft(var25.getCount());
                        var23.set(ItemStack.EMPTY);
                        var23.onTake(param3, var25);
                    }
                } else if (var25.isEmpty()) {
                    if (var23.mayPlace(var24)) {
                        int var26 = var23.getMaxStackSize(var24);
                        if (var24.getCount() > var26) {
                            var23.set(var24.split(var26));
                        } else {
                            var23.set(var24);
                            var1.setItem(param1, ItemStack.EMPTY);
                        }
                    }
                } else if (var23.mayPickup(param3) && var23.mayPlace(var24)) {
                    int var27 = var23.getMaxStackSize(var24);
                    if (var24.getCount() > var27) {
                        var23.set(var24.split(var27));
                        var23.onTake(param3, var25);
                        if (!var1.add(var25)) {
                            param3.drop(var25, true);
                        }
                    } else {
                        var23.set(var24);
                        var1.setItem(param1, var25);
                        var23.onTake(param3, var25);
                    }
                }
            }
        } else if (param2 == ClickType.CLONE && param3.getAbilities().instabuild && var1.getCarried().isEmpty() && param0 >= 0) {
            Slot var28 = this.slots.get(param0);
            if (var28.hasItem()) {
                ItemStack var29 = var28.getItem().copy();
                var29.setCount(var29.getMaxStackSize());
                var1.setCarried(var29);
            }
        } else if (param2 == ClickType.THROW && var1.getCarried().isEmpty() && param0 >= 0) {
            Slot var30 = this.slots.get(param0);
            int var31 = param1 == 0 ? 1 : var30.getItem().getCount();
            ItemStack var32 = var30.safeTake(var31, Integer.MAX_VALUE, param3);
            param3.drop(var32, true);
        } else if (param2 == ClickType.PICKUP_ALL && param0 >= 0) {
            Slot var33 = this.slots.get(param0);
            ItemStack var34 = var1.getCarried();
            if (!var34.isEmpty() && (!var33.hasItem() || !var33.mayPickup(param3))) {
                int var35 = param1 == 0 ? 0 : this.slots.size() - 1;
                int var36 = param1 == 0 ? 1 : -1;

                for(int var37 = 0; var37 < 2; ++var37) {
                    for(int var38 = var35; var38 >= 0 && var38 < this.slots.size() && var34.getCount() < var34.getMaxStackSize(); var38 += var36) {
                        Slot var39 = this.slots.get(var38);
                        if (var39.hasItem() && canItemQuickReplace(var39, var34, true) && var39.mayPickup(param3) && this.canTakeItemForPickAll(var34, var39)) {
                            ItemStack var40 = var39.getItem();
                            if (var37 != 0 || var40.getCount() != var40.getMaxStackSize()) {
                                ItemStack var41 = var39.safeTake(var40.getCount(), var34.getMaxStackSize() - var34.getCount(), param3);
                                var34.grow(var41.getCount());
                            }
                        }
                    }
                }
            }

            this.broadcastChanges();
        }

        return var0;
    }

    public boolean canTakeItemForPickAll(ItemStack param0, Slot param1) {
        return true;
    }

    public void removed(Player param0) {
        Inventory var0 = param0.getInventory();
        if (!var0.getCarried().isEmpty()) {
            param0.drop(var0.getCarried(), false);
            var0.setCarried(ItemStack.EMPTY);
        }

    }

    protected void clearContainer(Player param0, Container param1) {
        if (!param0.isAlive() || param0 instanceof ServerPlayer && ((ServerPlayer)param0).hasDisconnected()) {
            for(int var0 = 0; var0 < param1.getContainerSize(); ++var0) {
                param0.drop(param1.removeItemNoUpdate(var0), false);
            }

        } else {
            for(int var1 = 0; var1 < param1.getContainerSize(); ++var1) {
                Inventory var2 = param0.getInventory();
                if (var2.player instanceof ServerPlayer) {
                    var2.placeItemBackInInventory(param1.removeItemNoUpdate(var1));
                }
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
                if (!var3.isEmpty() && ItemStack.isSameItemSameTags(param0, var3)) {
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
            return param0 == 2 && param1.getAbilities().instabuild;
        }
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    public static boolean canItemQuickReplace(@Nullable Slot param0, ItemStack param1, boolean param2) {
        boolean var0 = param0 == null || !param0.hasItem();
        if (!var0 && ItemStack.isSameItemSameTags(param1, param0.getItem())) {
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
