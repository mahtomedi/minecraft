package net.minecraft.world.inventory;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class LoomMenu extends AbstractContainerMenu {
    private static final int PATTERN_NOT_SET = -1;
    private static final int INV_SLOT_START = 4;
    private static final int INV_SLOT_END = 31;
    private static final int USE_ROW_SLOT_START = 31;
    private static final int USE_ROW_SLOT_END = 40;
    private final ContainerLevelAccess access;
    final DataSlot selectedBannerPatternIndex = DataSlot.standalone();
    private List<Holder<BannerPattern>> selectablePatterns = List.of();
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
                    LoomMenu.this.selectedBannerPatternIndex.set(-1);
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

    @Override
    public boolean stillValid(Player param0) {
        return stillValid(this.access, param0, Blocks.LOOM);
    }

    @Override
    public boolean clickMenuButton(Player param0, int param1) {
        if (param1 >= 0 && param1 < this.selectablePatterns.size()) {
            this.selectedBannerPatternIndex.set(param1);
            this.setupResultSlot(this.selectablePatterns.get(param1));
            return true;
        } else {
            return false;
        }
    }

    private List<Holder<BannerPattern>> getSelectablePatterns(ItemStack param0) {
        if (param0.isEmpty()) {
            return BuiltInRegistries.BANNER_PATTERN.getTag(BannerPatternTags.NO_ITEM_REQUIRED).map(ImmutableList::copyOf).orElse(ImmutableList.of());
        } else {
            Item var3 = param0.getItem();
            return var3 instanceof BannerPatternItem var0
                ? BuiltInRegistries.BANNER_PATTERN.getTag(var0.getBannerPattern()).map(ImmutableList::copyOf).orElse(ImmutableList.of())
                : List.of();
        }
    }

    private boolean isValidPatternIndex(int param0) {
        return param0 >= 0 && param0 < this.selectablePatterns.size();
    }

    @Override
    public void slotsChanged(Container param0) {
        ItemStack var0 = this.bannerSlot.getItem();
        ItemStack var1 = this.dyeSlot.getItem();
        ItemStack var2 = this.patternSlot.getItem();
        if (!var0.isEmpty() && !var1.isEmpty()) {
            int var3 = this.selectedBannerPatternIndex.get();
            boolean var4 = this.isValidPatternIndex(var3);
            List<Holder<BannerPattern>> var5 = this.selectablePatterns;
            this.selectablePatterns = this.getSelectablePatterns(var2);
            Holder<BannerPattern> var6;
            if (this.selectablePatterns.size() == 1) {
                this.selectedBannerPatternIndex.set(0);
                var6 = this.selectablePatterns.get(0);
            } else if (!var4) {
                this.selectedBannerPatternIndex.set(-1);
                var6 = null;
            } else {
                Holder<BannerPattern> var8 = var5.get(var3);
                int var9 = this.selectablePatterns.indexOf(var8);
                if (var9 != -1) {
                    var6 = var8;
                    this.selectedBannerPatternIndex.set(var9);
                } else {
                    var6 = null;
                    this.selectedBannerPatternIndex.set(-1);
                }
            }

            if (var6 != null) {
                CompoundTag var12 = BlockItem.getBlockEntityData(var0);
                boolean var13 = var12 != null && var12.contains("Patterns", 9) && !var0.isEmpty() && var12.getList("Patterns", 10).size() >= 6;
                if (var13) {
                    this.selectedBannerPatternIndex.set(-1);
                    this.resultSlot.set(ItemStack.EMPTY);
                } else {
                    this.setupResultSlot(var6);
                }
            } else {
                this.resultSlot.set(ItemStack.EMPTY);
            }

            this.broadcastChanges();
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
            this.selectablePatterns = List.of();
            this.selectedBannerPatternIndex.set(-1);
        }
    }

    public List<Holder<BannerPattern>> getSelectablePatterns() {
        return this.selectablePatterns;
    }

    public int getSelectedBannerPatternIndex() {
        return this.selectedBannerPatternIndex.get();
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
                var1.setByPlayer(ItemStack.EMPTY);
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

    private void setupResultSlot(Holder<BannerPattern> param0) {
        ItemStack var0 = this.bannerSlot.getItem();
        ItemStack var1 = this.dyeSlot.getItem();
        ItemStack var2 = ItemStack.EMPTY;
        if (!var0.isEmpty() && !var1.isEmpty()) {
            var2 = var0.copy();
            var2.setCount(1);
            DyeColor var3 = ((DyeItem)var1.getItem()).getDyeColor();
            CompoundTag var4 = BlockItem.getBlockEntityData(var2);
            ListTag var5;
            if (var4 != null && var4.contains("Patterns", 9)) {
                var5 = var4.getList("Patterns", 10);
            } else {
                var5 = new ListTag();
                if (var4 == null) {
                    var4 = new CompoundTag();
                }

                var4.put("Patterns", var5);
            }

            CompoundTag var7 = new CompoundTag();
            var7.putString("Pattern", param0.value().getHashname());
            var7.putInt("Color", var3.getId());
            var5.add(var7);
            BlockItem.setBlockEntityData(var2, BlockEntityType.BANNER, var4);
        }

        if (!ItemStack.matches(var2, this.resultSlot.getItem())) {
            this.resultSlot.set(var2);
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
