package net.minecraft.world.inventory;

import java.util.List;
import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnchantmentMenu extends AbstractContainerMenu {
    private final Container enchantSlots = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            EnchantmentMenu.this.slotsChanged(this);
        }
    };
    private final ContainerLevelAccess access;
    private final Random random = new Random();
    private final DataSlot enchantmentSeed = DataSlot.standalone();
    public final int[] costs = new int[3];
    public final int[] enchantClue = new int[]{-1, -1, -1};
    public final int[] levelClue = new int[]{-1, -1, -1};

    public EnchantmentMenu(int param0, Inventory param1) {
        this(param0, param1, ContainerLevelAccess.NULL);
    }

    public EnchantmentMenu(int param0, Inventory param1, ContainerLevelAccess param2) {
        super(MenuType.ENCHANTMENT, param0);
        this.access = param2;
        this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return true;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return param0.getItem() == Items.LAPIS_LAZULI;
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

        this.addDataSlot(DataSlot.shared(this.costs, 0));
        this.addDataSlot(DataSlot.shared(this.costs, 1));
        this.addDataSlot(DataSlot.shared(this.costs, 2));
        this.addDataSlot(this.enchantmentSeed).set(param1.player.getEnchantmentSeed());
        this.addDataSlot(DataSlot.shared(this.enchantClue, 0));
        this.addDataSlot(DataSlot.shared(this.enchantClue, 1));
        this.addDataSlot(DataSlot.shared(this.enchantClue, 2));
        this.addDataSlot(DataSlot.shared(this.levelClue, 0));
        this.addDataSlot(DataSlot.shared(this.levelClue, 1));
        this.addDataSlot(DataSlot.shared(this.levelClue, 2));
    }

    @Override
    public void slotsChanged(Container param0) {
        if (param0 == this.enchantSlots) {
            ItemStack var0 = param0.getItem(0);
            if (!var0.isEmpty() && var0.isEnchantable()) {
                this.access
                    .execute(
                        (param1, param2) -> {
                            int var0x = 0;
        
                            for(int var1x = -1; var1x <= 1; ++var1x) {
                                for(int var2 = -1; var2 <= 1; ++var2) {
                                    if ((var1x != 0 || var2 != 0)
                                        && param1.isEmptyBlock(param2.offset(var2, 0, var1x))
                                        && param1.isEmptyBlock(param2.offset(var2, 1, var1x))) {
                                        if (param1.getBlockState(param2.offset(var2 * 2, 0, var1x * 2)).getBlock() == Blocks.BOOKSHELF) {
                                            ++var0x;
                                        }
        
                                        if (param1.getBlockState(param2.offset(var2 * 2, 1, var1x * 2)).getBlock() == Blocks.BOOKSHELF) {
                                            ++var0x;
                                        }
        
                                        if (var2 != 0 && var1x != 0) {
                                            if (param1.getBlockState(param2.offset(var2 * 2, 0, var1x)).getBlock() == Blocks.BOOKSHELF) {
                                                ++var0x;
                                            }
        
                                            if (param1.getBlockState(param2.offset(var2 * 2, 1, var1x)).getBlock() == Blocks.BOOKSHELF) {
                                                ++var0x;
                                            }
        
                                            if (param1.getBlockState(param2.offset(var2, 0, var1x * 2)).getBlock() == Blocks.BOOKSHELF) {
                                                ++var0x;
                                            }
        
                                            if (param1.getBlockState(param2.offset(var2, 1, var1x * 2)).getBlock() == Blocks.BOOKSHELF) {
                                                ++var0x;
                                            }
                                        }
                                    }
                                }
                            }
        
                            this.random.setSeed((long)this.enchantmentSeed.get());
        
                            for(int var3 = 0; var3 < 3; ++var3) {
                                this.costs[var3] = EnchantmentHelper.getEnchantmentCost(this.random, var3, var0x, var0);
                                this.enchantClue[var3] = -1;
                                this.levelClue[var3] = -1;
                                if (this.costs[var3] < var3 + 1) {
                                    this.costs[var3] = 0;
                                }
                            }
        
                            for(int var4 = 0; var4 < 3; ++var4) {
                                if (this.costs[var4] > 0) {
                                    List<EnchantmentInstance> var5 = this.getEnchantmentList(var0, var4, this.costs[var4]);
                                    if (var5 != null && !var5.isEmpty()) {
                                        EnchantmentInstance var6 = var5.get(this.random.nextInt(var5.size()));
                                        this.enchantClue[var4] = Registry.ENCHANTMENT.getId(var6.enchantment);
                                        this.levelClue[var4] = var6.level;
                                    }
                                }
                            }
        
                            this.broadcastChanges();
                        }
                    );
            } else {
                for(int var1 = 0; var1 < 3; ++var1) {
                    this.costs[var1] = 0;
                    this.enchantClue[var1] = -1;
                    this.levelClue[var1] = -1;
                }
            }
        }

    }

    @Override
    public boolean clickMenuButton(Player param0, int param1) {
        ItemStack var0 = this.enchantSlots.getItem(0);
        ItemStack var1 = this.enchantSlots.getItem(1);
        int var2 = param1 + 1;
        if ((var1.isEmpty() || var1.getCount() < var2) && !param0.abilities.instabuild) {
            return false;
        } else if (this.costs[param1] <= 0
            || var0.isEmpty()
            || (param0.experienceLevel < var2 || param0.experienceLevel < this.costs[param1]) && !param0.abilities.instabuild) {
            return false;
        } else {
            this.access.execute((param5, param6) -> {
                ItemStack var0x = var0;
                List<EnchantmentInstance> var1x = this.getEnchantmentList(var0, param1, this.costs[param1]);
                if (!var1x.isEmpty()) {
                    param0.onEnchantmentPerformed(var0, var2);
                    boolean var2x = var0.getItem() == Items.BOOK;
                    if (var2x) {
                        var0x = new ItemStack(Items.ENCHANTED_BOOK);
                        this.enchantSlots.setItem(0, var0x);
                    }

                    for(int var3x = 0; var3x < var1x.size(); ++var3x) {
                        EnchantmentInstance var4x = var1x.get(var3x);
                        if (var2x) {
                            EnchantedBookItem.addEnchantment(var0x, var4x);
                        } else {
                            var0x.enchant(var4x.enchantment, var4x.level);
                        }
                    }

                    if (!param0.abilities.instabuild) {
                        var1.shrink(var2);
                        if (var1.isEmpty()) {
                            this.enchantSlots.setItem(1, ItemStack.EMPTY);
                        }
                    }

                    param0.awardStat(Stats.ENCHANT_ITEM);
                    if (param0 instanceof ServerPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)param0, var0x, var2);
                    }

                    this.enchantSlots.setChanged();
                    this.enchantmentSeed.set(param0.getEnchantmentSeed());
                    this.slotsChanged(this.enchantSlots);
                    param5.playSound(null, param6, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, param5.random.nextFloat() * 0.1F + 0.9F);
                }

            });
            return true;
        }
    }

    private List<EnchantmentInstance> getEnchantmentList(ItemStack param0, int param1, int param2) {
        this.random.setSeed((long)(this.enchantmentSeed.get() + param1));
        List<EnchantmentInstance> var0 = EnchantmentHelper.selectEnchantment(this.random, param0, param2, false);
        if (param0.getItem() == Items.BOOK && var0.size() > 1) {
            var0.remove(this.random.nextInt(var0.size()));
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public int getGoldCount() {
        ItemStack var0 = this.enchantSlots.getItem(1);
        return var0.isEmpty() ? 0 : var0.getCount();
    }

    @OnlyIn(Dist.CLIENT)
    public int getEnchantmentSeed() {
        return this.enchantmentSeed.get();
    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.access.execute((param1, param2) -> this.clearContainer(param0, param0.level, this.enchantSlots));
    }

    @Override
    public boolean stillValid(Player param0) {
        return stillValid(this.access, param0, Blocks.ENCHANTING_TABLE);
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 == 0) {
                if (!this.moveItemStackTo(var2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (param1 == 1) {
                if (!this.moveItemStackTo(var2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (var2.getItem() == Items.LAPIS_LAZULI) {
                if (!this.moveItemStackTo(var2, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.get(0).hasItem() || !this.slots.get(0).mayPlace(var2)) {
                    return ItemStack.EMPTY;
                }

                if (var2.hasTag() && var2.getCount() == 1) {
                    this.slots.get(0).set(var2.copy());
                    var2.setCount(0);
                } else if (!var2.isEmpty()) {
                    this.slots.get(0).set(new ItemStack(var2.getItem()));
                    var2.shrink(1);
                }
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
}
