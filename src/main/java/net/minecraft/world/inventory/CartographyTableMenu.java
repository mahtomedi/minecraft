package net.minecraft.world.inventory;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CartographyTableMenu extends AbstractContainerMenu {
    public static final int MAP_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    private final ContainerLevelAccess access;
    long lastSoundTime;
    public final Container container = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            CartographyTableMenu.this.slotsChanged(this);
            super.setChanged();
        }
    };
    private final ResultContainer resultContainer = new ResultContainer() {
        @Override
        public void setChanged() {
            CartographyTableMenu.this.slotsChanged(this);
            super.setChanged();
        }
    };

    public CartographyTableMenu(int param0, Inventory param1) {
        this(param0, param1, ContainerLevelAccess.NULL);
    }

    public CartographyTableMenu(int param0, Inventory param1, final ContainerLevelAccess param2) {
        super(MenuType.CARTOGRAPHY_TABLE, param0);
        this.access = param2;
        this.addSlot(new Slot(this.container, 0, 15, 15) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return param0.is(Items.FILLED_MAP);
            }
        });
        this.addSlot(new Slot(this.container, 1, 15, 52) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return param0.is(Items.PAPER) || param0.is(Items.MAP) || param0.is(Items.GLASS_PANE);
            }
        });
        this.addSlot(new Slot(this.resultContainer, 2, 145, 39) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return false;
            }

            @Override
            public void onTake(Player param0, ItemStack param1) {
                CartographyTableMenu.this.slots.get(0).remove(1);
                CartographyTableMenu.this.slots.get(1).remove(1);
                param1.getItem().onCraftedBy(param1, param0.level, param0);
                param2.execute((param0x, param1x) -> {
                    long var0 = param0x.getGameTime();
                    if (CartographyTableMenu.this.lastSoundTime != var0) {
                        param0x.playSound(null, param1x, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        CartographyTableMenu.this.lastSoundTime = var0;
                    }

                });
                super.onTake(param0, param1);
            }
        });

        for(int var0 = 0; var0 < 3; ++var0) {
            for(int var1 = 0; var1 < 9; ++var1) {
                this.addSlot(new Slot(param1, var1 + var0 * 9 + 9, 8 + var1 * 18, 84 + var0 * 18));
            }
        }

        for(int var2 = 0; var2 < 9; ++var2) {
            this.addSlot(new Slot(param1, var2, 8 + var2 * 18, 142));
        }

    }

    @Override
    public boolean stillValid(Player param0) {
        return stillValid(this.access, param0, Blocks.CARTOGRAPHY_TABLE);
    }

    @Override
    public void slotsChanged(Container param0) {
        ItemStack var0 = this.container.getItem(0);
        ItemStack var1 = this.container.getItem(1);
        ItemStack var2 = this.resultContainer.getItem(2);
        if (var2.isEmpty() || !var0.isEmpty() && !var1.isEmpty()) {
            if (!var0.isEmpty() && !var1.isEmpty()) {
                this.setupResultSlot(var0, var1, var2);
            }
        } else {
            this.resultContainer.removeItemNoUpdate(2);
        }

    }

    private void setupResultSlot(ItemStack param0, ItemStack param1, ItemStack param2) {
        this.access.execute((param3, param4) -> {
            MapItemSavedData var0 = MapItem.getSavedData(param0, param3);
            if (var0 != null) {
                ItemStack var1;
                if (param1.is(Items.PAPER) && !var0.locked && var0.scale < 4) {
                    var1 = param0.copy();
                    var1.setCount(1);
                    var1.getOrCreateTag().putInt("map_scale_direction", 1);
                    this.broadcastChanges();
                } else if (param1.is(Items.GLASS_PANE) && !var0.locked) {
                    var1 = param0.copy();
                    var1.setCount(1);
                    var1.getOrCreateTag().putBoolean("map_to_lock", true);
                    this.broadcastChanges();
                } else {
                    if (!param1.is(Items.MAP)) {
                        this.resultContainer.removeItemNoUpdate(2);
                        this.broadcastChanges();
                        return;
                    }

                    var1 = param0.copy();
                    var1.setCount(2);
                    this.broadcastChanges();
                }

                if (!ItemStack.matches(var1, param2)) {
                    this.resultContainer.setItem(2, var1);
                    this.broadcastChanges();
                }

            }
        });
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack param0, Slot param1) {
        return param1.container != this.resultContainer && super.canTakeItemForPickAll(param0, param1);
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 == 2) {
                var2.getItem().onCraftedBy(var2, param0.level, param0);
                if (!this.moveItemStackTo(var2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
            } else if (param1 != 1 && param1 != 0) {
                if (var2.is(Items.FILLED_MAP)) {
                    if (!this.moveItemStackTo(var2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!var2.is(Items.PAPER) && !var2.is(Items.MAP) && !var2.is(Items.GLASS_PANE)) {
                    if (param1 >= 3 && param1 < 30) {
                        if (!this.moveItemStackTo(var2, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (param1 >= 30 && param1 < 39 && !this.moveItemStackTo(var2, 3, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(var2, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.set(ItemStack.EMPTY);
            }

            var1.setChanged();
            if (var2.getCount() == var0.getCount()) {
                return ItemStack.EMPTY;
            }

            var1.onTake(param0, var2);
            this.broadcastChanges();
        }

        return var0;
    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.resultContainer.removeItemNoUpdate(2);
        this.access.execute((param1, param2) -> this.clearContainer(param0, this.container));
    }
}
