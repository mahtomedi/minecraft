package net.minecraft.world.inventory;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

public abstract class AbstractContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int SLOT_CLICKED_OUTSIDE = -999;
    public static final int QUICKCRAFT_TYPE_CHARITABLE = 0;
    public static final int QUICKCRAFT_TYPE_GREEDY = 1;
    public static final int QUICKCRAFT_TYPE_CLONE = 2;
    public static final int QUICKCRAFT_HEADER_START = 0;
    public static final int QUICKCRAFT_HEADER_CONTINUE = 1;
    public static final int QUICKCRAFT_HEADER_END = 2;
    public static final int CARRIED_SLOT_SIZE = Integer.MAX_VALUE;
    private final NonNullList<ItemStack> lastSlots = NonNullList.create();
    public final NonNullList<Slot> slots = NonNullList.create();
    private final List<DataSlot> dataSlots = Lists.newArrayList();
    private ItemStack carried = ItemStack.EMPTY;
    private final NonNullList<ItemStack> remoteSlots = NonNullList.create();
    private final IntList remoteDataSlots = new IntArrayList();
    private ItemStack remoteCarried = ItemStack.EMPTY;
    private int stateId;
    @Nullable
    private final MenuType<?> menuType;
    public final int containerId;
    private int quickcraftType = -1;
    private int quickcraftStatus;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();
    private final List<ContainerListener> containerListeners = Lists.newArrayList();
    @Nullable
    private ContainerSynchronizer synchronizer;
    private boolean suppressRemoteUpdates;

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

    public boolean isValidSlotIndex(int param0) {
        return param0 == -1 || param0 == -999 || param0 < this.slots.size();
    }

    protected Slot addSlot(Slot param0) {
        param0.index = this.slots.size();
        this.slots.add(param0);
        this.lastSlots.add(ItemStack.EMPTY);
        this.remoteSlots.add(ItemStack.EMPTY);
        return param0;
    }

    protected DataSlot addDataSlot(DataSlot param0) {
        this.dataSlots.add(param0);
        this.remoteDataSlots.add(0);
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
            this.broadcastChanges();
        }
    }

    public void setSynchronizer(ContainerSynchronizer param0) {
        this.synchronizer = param0;
        this.sendAllDataToRemote();
    }

    public void sendAllDataToRemote() {
        int var0 = 0;

        for(int var1 = this.slots.size(); var0 < var1; ++var0) {
            this.remoteSlots.set(var0, this.slots.get(var0).getItem().copy());
        }

        this.remoteCarried = this.getCarried().copy();
        var0 = 0;

        for(int var3 = this.dataSlots.size(); var0 < var3; ++var0) {
            this.remoteDataSlots.set(var0, this.dataSlots.get(var0).get());
        }

        if (this.synchronizer != null) {
            this.synchronizer.sendInitialData(this, this.remoteSlots, this.remoteCarried, this.remoteDataSlots.toIntArray());
        }

    }

    public void removeSlotListener(ContainerListener param0) {
        this.containerListeners.remove(param0);
    }

    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> var0 = NonNullList.create();

        for(Slot var1 : this.slots) {
            var0.add(var1.getItem());
        }

        return var0;
    }

    public void broadcastChanges() {
        for(int var0 = 0; var0 < this.slots.size(); ++var0) {
            ItemStack var1 = this.slots.get(var0).getItem();
            Supplier<ItemStack> var2 = Suppliers.memoize(var1::copy);
            this.triggerSlotListeners(var0, var1, var2);
            this.synchronizeSlotToRemote(var0, var1, var2);
        }

        this.synchronizeCarriedToRemote();

        for(int var3 = 0; var3 < this.dataSlots.size(); ++var3) {
            DataSlot var4 = this.dataSlots.get(var3);
            int var5 = var4.get();
            if (var4.checkAndClearUpdateFlag()) {
                this.updateDataSlotListeners(var3, var5);
            }

            this.synchronizeDataSlotToRemote(var3, var5);
        }

    }

    public void broadcastFullState() {
        for(int var0 = 0; var0 < this.slots.size(); ++var0) {
            ItemStack var1 = this.slots.get(var0).getItem();
            this.triggerSlotListeners(var0, var1, var1::copy);
        }

        for(int var2 = 0; var2 < this.dataSlots.size(); ++var2) {
            DataSlot var3 = this.dataSlots.get(var2);
            if (var3.checkAndClearUpdateFlag()) {
                this.updateDataSlotListeners(var2, var3.get());
            }
        }

        this.sendAllDataToRemote();
    }

    private void updateDataSlotListeners(int param0, int param1) {
        for(ContainerListener var0 : this.containerListeners) {
            var0.dataChanged(this, param0, param1);
        }

    }

    private void triggerSlotListeners(int param0, ItemStack param1, Supplier<ItemStack> param2) {
        ItemStack var0 = this.lastSlots.get(param0);
        if (!ItemStack.matches(var0, param1)) {
            ItemStack var1 = param2.get();
            this.lastSlots.set(param0, var1);

            for(ContainerListener var2 : this.containerListeners) {
                var2.slotChanged(this, param0, var1);
            }
        }

    }

    private void synchronizeSlotToRemote(int param0, ItemStack param1, Supplier<ItemStack> param2) {
        if (!this.suppressRemoteUpdates) {
            ItemStack var0 = this.remoteSlots.get(param0);
            if (!ItemStack.matches(var0, param1)) {
                ItemStack var1 = param2.get();
                this.remoteSlots.set(param0, var1);
                if (this.synchronizer != null) {
                    this.synchronizer.sendSlotChange(this, param0, var1);
                }
            }

        }
    }

    private void synchronizeDataSlotToRemote(int param0, int param1) {
        if (!this.suppressRemoteUpdates) {
            int var0 = this.remoteDataSlots.getInt(param0);
            if (var0 != param1) {
                this.remoteDataSlots.set(param0, param1);
                if (this.synchronizer != null) {
                    this.synchronizer.sendDataChange(this, param0, param1);
                }
            }

        }
    }

    private void synchronizeCarriedToRemote() {
        if (!this.suppressRemoteUpdates) {
            if (!ItemStack.matches(this.getCarried(), this.remoteCarried)) {
                this.remoteCarried = this.getCarried().copy();
                if (this.synchronizer != null) {
                    this.synchronizer.sendCarriedChange(this, this.remoteCarried);
                }
            }

        }
    }

    public void setRemoteSlot(int param0, ItemStack param1) {
        this.remoteSlots.set(param0, param1.copy());
    }

    public void setRemoteSlotNoCopy(int param0, ItemStack param1) {
        if (param0 >= 0 && param0 < this.remoteSlots.size()) {
            this.remoteSlots.set(param0, param1);
        } else {
            LOGGER.debug("Incorrect slot index: {} available slots: {}", param0, this.remoteSlots.size());
        }
    }

    public void setRemoteCarried(ItemStack param0) {
        this.remoteCarried = param0.copy();
    }

    public boolean clickMenuButton(Player param0, int param1) {
        return false;
    }

    public Slot getSlot(int param0) {
        return this.slots.get(param0);
    }

    public abstract ItemStack quickMoveStack(Player var1, int var2);

    public void clicked(int param0, int param1, ClickType param2, Player param3) {
        try {
            this.doClick(param0, param1, param2, param3);
        } catch (Exception var8) {
            CrashReport var1 = CrashReport.forThrowable(var8, "Container click");
            CrashReportCategory var2 = var1.addCategory("Click info");
            var2.setDetail("Menu Type", () -> this.menuType != null ? BuiltInRegistries.MENU.getKey(this.menuType).toString() : "<no type>");
            var2.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
            var2.setDetail("Slot Count", this.slots.size());
            var2.setDetail("Slot", param0);
            var2.setDetail("Button", param1);
            var2.setDetail("Type", param2);
            throw new ReportedException(var1);
        }
    }

    private void doClick(int param0, int param1, ClickType param2, Player param3) {
        Inventory var0 = param3.getInventory();
        if (param2 == ClickType.QUICK_CRAFT) {
            int var1 = this.quickcraftStatus;
            this.quickcraftStatus = getQuickcraftHeader(param1);
            if ((var1 != 1 || this.quickcraftStatus != 2) && var1 != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
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
                Slot var2 = this.slots.get(param0);
                ItemStack var3 = this.getCarried();
                if (canItemQuickReplace(var2, var3, true)
                    && var2.mayPlace(var3)
                    && (this.quickcraftType == 2 || var3.getCount() > this.quickcraftSlots.size())
                    && this.canDragTo(var2)) {
                    this.quickcraftSlots.add(var2);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (this.quickcraftSlots.size() == 1) {
                        int var4 = this.quickcraftSlots.iterator().next().index;
                        this.resetQuickCraft();
                        this.doClick(var4, this.quickcraftType, ClickType.PICKUP, param3);
                        return;
                    }

                    ItemStack var5 = this.getCarried().copy();
                    int var6 = this.getCarried().getCount();

                    for(Slot var7 : this.quickcraftSlots) {
                        ItemStack var8 = this.getCarried();
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
                            var7.setByPlayer(var9);
                        }
                    }

                    var5.setCount(var6);
                    this.setCarried(var5);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else if ((param2 == ClickType.PICKUP || param2 == ClickType.QUICK_MOVE) && (param1 == 0 || param1 == 1)) {
            ClickAction var12 = param1 == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
            if (param0 == -999) {
                if (!this.getCarried().isEmpty()) {
                    if (var12 == ClickAction.PRIMARY) {
                        param3.drop(this.getCarried(), true);
                        this.setCarried(ItemStack.EMPTY);
                    } else {
                        param3.drop(this.getCarried().split(1), true);
                    }
                }
            } else if (param2 == ClickType.QUICK_MOVE) {
                if (param0 < 0) {
                    return;
                }

                Slot var13 = this.slots.get(param0);
                if (!var13.mayPickup(param3)) {
                    return;
                }

                ItemStack var14 = this.quickMoveStack(param3, param0);

                while(!var14.isEmpty() && ItemStack.isSame(var13.getItem(), var14)) {
                    var14 = this.quickMoveStack(param3, param0);
                }
            } else {
                if (param0 < 0) {
                    return;
                }

                Slot var15 = this.slots.get(param0);
                ItemStack var16 = var15.getItem();
                ItemStack var17 = this.getCarried();
                param3.updateTutorialInventoryAction(var17, var15.getItem(), var12);
                if (!this.tryItemClickBehaviourOverride(param3, var12, var15, var16, var17)) {
                    if (var16.isEmpty()) {
                        if (!var17.isEmpty()) {
                            int var18 = var12 == ClickAction.PRIMARY ? var17.getCount() : 1;
                            this.setCarried(var15.safeInsert(var17, var18));
                        }
                    } else if (var15.mayPickup(param3)) {
                        if (var17.isEmpty()) {
                            int var19 = var12 == ClickAction.PRIMARY ? var16.getCount() : (var16.getCount() + 1) / 2;
                            Optional<ItemStack> var20 = var15.tryRemove(var19, Integer.MAX_VALUE, param3);
                            var20.ifPresent(param2x -> {
                                this.setCarried(param2x);
                                var15.onTake(param3, param2x);
                            });
                        } else if (var15.mayPlace(var17)) {
                            if (ItemStack.isSameItemSameTags(var16, var17)) {
                                int var21 = var12 == ClickAction.PRIMARY ? var17.getCount() : 1;
                                this.setCarried(var15.safeInsert(var17, var21));
                            } else if (var17.getCount() <= var15.getMaxStackSize(var17)) {
                                this.setCarried(var16);
                                var15.setByPlayer(var17);
                            }
                        } else if (ItemStack.isSameItemSameTags(var16, var17)) {
                            Optional<ItemStack> var22 = var15.tryRemove(var16.getCount(), var17.getMaxStackSize() - var17.getCount(), param3);
                            var22.ifPresent(param3x -> {
                                var17.grow(param3x.getCount());
                                var15.onTake(param3, param3x);
                            });
                        }
                    }
                }

                var15.setChanged();
            }
        } else if (param2 == ClickType.SWAP) {
            Slot var23 = this.slots.get(param0);
            ItemStack var24 = var0.getItem(param1);
            ItemStack var25 = var23.getItem();
            if (!var24.isEmpty() || !var25.isEmpty()) {
                if (var24.isEmpty()) {
                    if (var23.mayPickup(param3)) {
                        var0.setItem(param1, var25);
                        var23.onSwapCraft(var25.getCount());
                        var23.setByPlayer(ItemStack.EMPTY);
                        var23.onTake(param3, var25);
                    }
                } else if (var25.isEmpty()) {
                    if (var23.mayPlace(var24)) {
                        int var26 = var23.getMaxStackSize(var24);
                        if (var24.getCount() > var26) {
                            var23.setByPlayer(var24.split(var26));
                        } else {
                            var0.setItem(param1, ItemStack.EMPTY);
                            var23.setByPlayer(var24);
                        }
                    }
                } else if (var23.mayPickup(param3) && var23.mayPlace(var24)) {
                    int var27 = var23.getMaxStackSize(var24);
                    if (var24.getCount() > var27) {
                        var23.setByPlayer(var24.split(var27));
                        var23.onTake(param3, var25);
                        if (!var0.add(var25)) {
                            param3.drop(var25, true);
                        }
                    } else {
                        var0.setItem(param1, var25);
                        var23.setByPlayer(var24);
                        var23.onTake(param3, var25);
                    }
                }
            }
        } else if (param2 == ClickType.CLONE && param3.getAbilities().instabuild && this.getCarried().isEmpty() && param0 >= 0) {
            Slot var28 = this.slots.get(param0);
            if (var28.hasItem()) {
                ItemStack var29 = var28.getItem().copy();
                var29.setCount(var29.getMaxStackSize());
                this.setCarried(var29);
            }
        } else if (param2 == ClickType.THROW && this.getCarried().isEmpty() && param0 >= 0) {
            Slot var30 = this.slots.get(param0);
            int var31 = param1 == 0 ? 1 : var30.getItem().getCount();
            ItemStack var32 = var30.safeTake(var31, Integer.MAX_VALUE, param3);
            param3.drop(var32, true);
        } else if (param2 == ClickType.PICKUP_ALL && param0 >= 0) {
            Slot var33 = this.slots.get(param0);
            ItemStack var34 = this.getCarried();
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
        }

    }

    private boolean tryItemClickBehaviourOverride(Player param0, ClickAction param1, Slot param2, ItemStack param3, ItemStack param4) {
        FeatureFlagSet var0 = param0.getLevel().enabledFeatures();
        if (param4.isItemEnabled(var0) && param4.overrideStackedOnOther(param2, param1, param0)) {
            return true;
        } else {
            return param3.isItemEnabled(var0) && param3.overrideOtherStackedOnMe(param4, param2, param1, param0, this.createCarriedSlotAccess());
        }
    }

    private SlotAccess createCarriedSlotAccess() {
        return new SlotAccess() {
            @Override
            public ItemStack get() {
                return AbstractContainerMenu.this.getCarried();
            }

            @Override
            public boolean set(ItemStack param0) {
                AbstractContainerMenu.this.setCarried(param0);
                return true;
            }
        };
    }

    public boolean canTakeItemForPickAll(ItemStack param0, Slot param1) {
        return true;
    }

    public void removed(Player param0) {
        if (param0 instanceof ServerPlayer) {
            ItemStack var0 = this.getCarried();
            if (!var0.isEmpty()) {
                if (param0.isAlive() && !((ServerPlayer)param0).hasDisconnected()) {
                    param0.getInventory().placeItemBackInInventory(var0);
                } else {
                    param0.drop(var0, false);
                }

                this.setCarried(ItemStack.EMPTY);
            }
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

    public void setItem(int param0, int param1, ItemStack param2) {
        this.getSlot(param0).set(param2);
        this.stateId = param1;
    }

    public void initializeContents(int param0, List<ItemStack> param1, ItemStack param2) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            this.getSlot(var0).set(param1.get(var0));
        }

        this.carried = param2;
        this.stateId = param0;
    }

    public void setData(int param0, int param1) {
        this.dataSlots.get(param0).set(param1);
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
                        var5.setByPlayer(param0.split(var5.getMaxStackSize()));
                    } else {
                        var5.setByPlayer(param0.split(param0.getCount()));
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

    public void setCarried(ItemStack param0) {
        this.carried = param0;
    }

    public ItemStack getCarried() {
        return this.carried;
    }

    public void suppressRemoteUpdates() {
        this.suppressRemoteUpdates = true;
    }

    public void resumeRemoteUpdates() {
        this.suppressRemoteUpdates = false;
    }

    public void transferState(AbstractContainerMenu param0) {
        Table<Container, Integer, Integer> var0 = HashBasedTable.create();

        for(int var1 = 0; var1 < param0.slots.size(); ++var1) {
            Slot var2 = param0.slots.get(var1);
            var0.put(var2.container, var2.getContainerSlot(), var1);
        }

        for(int var3 = 0; var3 < this.slots.size(); ++var3) {
            Slot var4 = this.slots.get(var3);
            Integer var5 = var0.get(var4.container, var4.getContainerSlot());
            if (var5 != null) {
                this.lastSlots.set(var3, param0.lastSlots.get(var5));
                this.remoteSlots.set(var3, param0.remoteSlots.get(var5));
            }
        }

    }

    public OptionalInt findSlot(Container param0, int param1) {
        for(int var0 = 0; var0 < this.slots.size(); ++var0) {
            Slot var1 = this.slots.get(var0);
            if (var1.container == param0 && param1 == var1.getContainerSlot()) {
                return OptionalInt.of(var0);
            }
        }

        return OptionalInt.empty();
    }

    public int getStateId() {
        return this.stateId;
    }

    public int incrementStateId() {
        this.stateId = this.stateId + 1 & 32767;
        return this.stateId;
    }
}
