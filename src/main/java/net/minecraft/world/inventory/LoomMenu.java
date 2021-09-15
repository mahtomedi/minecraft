package net.minecraft.world.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class LoomMenu extends AbstractContainerMenu {
    private static final int INV_SLOT_START = 4;
    private static final int INV_SLOT_END = 31;
    private static final int USE_ROW_SLOT_START = 31;
    private static final int USE_ROW_SLOT_END = 40;
    private final ContainerLevelAccess access;
    final DataSlot selectedBannerPatternIndex = DataSlot.standalone();
    Runnable slotUpdateListener = () -> {
    };
    final Slot bannerSlot;
    final Slot dyeSlot;
    private final Slot patternSlot;
    private final Slot resultSlot;
    long lastSoundTime;
    private final Container inputContainer = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            LoomMenu.this.slotsChanged(this);
            LoomMenu.this.slotUpdateListener.run();
        }
    };
    private final Container outputContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            LoomMenu.this.slotUpdateListener.run();
        }
    };

    public LoomMenu(int param0, Inventory param1) {
        this(param0, param1, ContainerLevelAccess.NULL);
    }

    public LoomMenu(int param0, Inventory param1, final ContainerLevelAccess param2) {
        super(MenuType.LOOM, param0);
        this.access = param2;
        this.bannerSlot = this.addSlot(new Slot(this.inputContainer, 0, 13, 26) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return param0.getItem() instanceof BannerItem;
            }
        });
        this.dyeSlot = this.addSlot(new Slot(this.inputContainer, 1, 33, 26) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return param0.getItem() instanceof DyeItem;
            }
        });
        this.patternSlot = this.addSlot(new Slot(this.inputContainer, 2, 23, 45) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return param0.getItem() instanceof BannerPatternItem;
            }
        });
        this.resultSlot = this.addSlot(new Slot(this.outputContainer, 0, 143, 58) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return false;
            }

            @Override
            public void onTake(Player param0, ItemStack param1) {
                LoomMenu.this.bannerSlot.remove(1);
                LoomMenu.this.dyeSlot.remove(1);
                if (!LoomMenu.this.bannerSlot.hasItem() || !LoomMenu.this.dyeSlot.hasItem()) {
                    LoomMenu.this.selectedBannerPatternIndex.set(0);
                }

                param2.execute((param0x, param1x) -> {
                    long var0 = param0x.getGameTime();
                    if (LoomMenu.this.lastSoundTime != var0) {
                        param0x.playSound(null, param1x, SoundEvents.UI_LOOM_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        LoomMenu.this.lastSoundTime = var0;
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

        this.addDataSlot(this.selectedBannerPatternIndex);
    }

    public int getSelectedBannerPatternIndex() {
        return this.selectedBannerPatternIndex.get();
    }

    @Override
    public boolean stillValid(Player param0) {
        return stillValid(this.access, param0, Blocks.LOOM);
    }

    @Override
    public boolean clickMenuButton(Player param0, int param1) {
        if (param1 > 0 && param1 <= BannerPattern.AVAILABLE_PATTERNS) {
            this.selectedBannerPatternIndex.set(param1);
            this.setupResultSlot();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void slotsChanged(Container param0) {
        ItemStack var0 = this.bannerSlot.getItem();
        ItemStack var1 = this.dyeSlot.getItem();
        ItemStack var2 = this.patternSlot.getItem();
        ItemStack var3 = this.resultSlot.getItem();
        if (var3.isEmpty()
            || !var0.isEmpty()
                && !var1.isEmpty()
                && this.selectedBannerPatternIndex.get() > 0
                && (this.selectedBannerPatternIndex.get() < BannerPattern.COUNT - BannerPattern.PATTERN_ITEM_COUNT || !var2.isEmpty())) {
            if (!var2.isEmpty() && var2.getItem() instanceof BannerPatternItem) {
                CompoundTag var4 = BlockItem.getBlockEntityData(var0);
                boolean var5 = var4 != null && var4.contains("Patterns", 9) && !var0.isEmpty() && var4.getList("Patterns", 10).size() >= 6;
                if (var5) {
                    this.selectedBannerPatternIndex.set(0);
                } else {
                    this.selectedBannerPatternIndex.set(((BannerPatternItem)var2.getItem()).getBannerPattern().ordinal());
                }
            }
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
            this.selectedBannerPatternIndex.set(0);
        }

        this.setupResultSlot();
        this.broadcastChanges();
    }

    public void registerUpdateListener(Runnable param0) {
        this.slotUpdateListener = param0;
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 == this.resultSlot.index) {
                if (!this.moveItemStackTo(var2, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
            } else if (param1 != this.dyeSlot.index && param1 != this.bannerSlot.index && param1 != this.patternSlot.index) {
                if (var2.getItem() instanceof BannerItem) {
                    if (!this.moveItemStackTo(var2, this.bannerSlot.index, this.bannerSlot.index + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (var2.getItem() instanceof DyeItem) {
                    if (!this.moveItemStackTo(var2, this.dyeSlot.index, this.dyeSlot.index + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (var2.getItem() instanceof BannerPatternItem) {
                    if (!this.moveItemStackTo(var2, this.patternSlot.index, this.patternSlot.index + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (param1 >= 4 && param1 < 31) {
                    if (!this.moveItemStackTo(var2, 31, 40, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (param1 >= 31 && param1 < 40 && !this.moveItemStackTo(var2, 4, 31, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 4, 40, false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.set(ItemStack.EMPTY);
            } else {
                var1.setChanged();
            }

            if (var2.getCount() == var0.getCount()) {
                return ItemStack.EMPTY;
            }

            var1.onTake(param0, var2);
        }

        return var0;
    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.access.execute((param1, param2) -> this.clearContainer(param0, this.inputContainer));
    }

    private void setupResultSlot() {
        if (this.selectedBannerPatternIndex.get() > 0) {
            ItemStack var0 = this.bannerSlot.getItem();
            ItemStack var1 = this.dyeSlot.getItem();
            ItemStack var2 = ItemStack.EMPTY;
            if (!var0.isEmpty() && !var1.isEmpty()) {
                var2 = var0.copy();
                var2.setCount(1);
                BannerPattern var3 = BannerPattern.values()[this.selectedBannerPatternIndex.get()];
                DyeColor var4 = ((DyeItem)var1.getItem()).getDyeColor();
                CompoundTag var5 = BlockItem.getBlockEntityData(var2);
                ListTag var6;
                if (var5 != null && var5.contains("Patterns", 9)) {
                    var6 = var5.getList("Patterns", 10);
                } else {
                    var6 = new ListTag();
                    if (var5 == null) {
                        var5 = new CompoundTag();
                    }

                    var5.put("Patterns", var6);
                }

                CompoundTag var8 = new CompoundTag();
                var8.putString("Pattern", var3.getHashname());
                var8.putInt("Color", var4.getId());
                var6.add(var8);
                BlockItem.setBlockEntityData(var2, BlockEntityType.BANNER, var5);
            }

            if (!ItemStack.matches(var2, this.resultSlot.getItem())) {
                this.resultSlot.set(var2);
            }
        }

    }

    public Slot getBannerSlot() {
        return this.bannerSlot;
    }

    public Slot getDyeSlot() {
        return this.dyeSlot;
    }

    public Slot getPatternSlot() {
        return this.patternSlot;
    }

    public Slot getResultSlot() {
        return this.resultSlot;
    }
}
