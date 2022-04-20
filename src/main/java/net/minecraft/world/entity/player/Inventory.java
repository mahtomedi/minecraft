package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class Inventory implements Container, Nameable {
    public static final int POP_TIME_DURATION = 5;
    public static final int INVENTORY_SIZE = 36;
    private static final int SELECTION_SIZE = 9;
    public static final int SLOT_OFFHAND = 40;
    public static final int NOT_FOUND_INDEX = -1;
    public static final int[] ALL_ARMOR_SLOTS = new int[]{0, 1, 2, 3};
    public static final int[] HELMET_SLOT_ONLY = new int[]{3};
    public final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
    public final NonNullList<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
    public final NonNullList<ItemStack> offhand = NonNullList.withSize(1, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> compartments = ImmutableList.of(this.items, this.armor, this.offhand);
    public int selected;
    public final Player player;
    private int timesChanged;

    public Inventory(Player param0) {
        this.player = param0;
    }

    public ItemStack getSelected() {
        return isHotbarSlot(this.selected) ? this.items.get(this.selected) : ItemStack.EMPTY;
    }

    public static int getSelectionSize() {
        return 9;
    }

    private boolean hasRemainingSpaceForItem(ItemStack param0, ItemStack param1) {
        return !param0.isEmpty()
            && ItemStack.isSameItemSameTags(param0, param1)
            && param0.isStackable()
            && param0.getCount() < param0.getMaxStackSize()
            && param0.getCount() < this.getMaxStackSize();
    }

    public int getFreeSlot() {
        for(int var0 = 0; var0 < this.items.size(); ++var0) {
            if (this.items.get(var0).isEmpty()) {
                return var0;
            }
        }

        return -1;
    }

    public void setPickedItem(ItemStack param0) {
        int var0 = this.findSlotMatchingItem(param0);
        if (isHotbarSlot(var0)) {
            this.selected = var0;
        } else {
            if (var0 == -1) {
                this.selected = this.getSuitableHotbarSlot();
                if (!this.items.get(this.selected).isEmpty()) {
                    int var1 = this.getFreeSlot();
                    if (var1 != -1) {
                        this.items.set(var1, this.items.get(this.selected));
                    }
                }

                this.items.set(this.selected, param0);
            } else {
                this.pickSlot(var0);
            }

        }
    }

    public void pickSlot(int param0) {
        this.selected = this.getSuitableHotbarSlot();
        ItemStack var0 = this.items.get(this.selected);
        this.items.set(this.selected, this.items.get(param0));
        this.items.set(param0, var0);
    }

    public static boolean isHotbarSlot(int param0) {
        return param0 >= 0 && param0 < 9;
    }

    public int findSlotMatchingItem(ItemStack param0) {
        for(int var0 = 0; var0 < this.items.size(); ++var0) {
            if (!this.items.get(var0).isEmpty() && ItemStack.isSameItemSameTags(param0, this.items.get(var0))) {
                return var0;
            }
        }

        return -1;
    }

    public int findSlotMatchingUnusedItem(ItemStack param0) {
        for(int var0 = 0; var0 < this.items.size(); ++var0) {
            ItemStack var1 = this.items.get(var0);
            if (!this.items.get(var0).isEmpty()
                && ItemStack.isSameItemSameTags(param0, this.items.get(var0))
                && !this.items.get(var0).isDamaged()
                && !var1.isEnchanted()
                && !var1.hasCustomHoverName()) {
                return var0;
            }
        }

        return -1;
    }

    public int getSuitableHotbarSlot() {
        for(int var0 = 0; var0 < 9; ++var0) {
            int var1 = (this.selected + var0) % 9;
            if (this.items.get(var1).isEmpty()) {
                return var1;
            }
        }

        for(int var2 = 0; var2 < 9; ++var2) {
            int var3 = (this.selected + var2) % 9;
            if (!this.items.get(var3).isEnchanted()) {
                return var3;
            }
        }

        return this.selected;
    }

    public void swapPaint(double param0) {
        int var0 = (int)Math.signum(param0);
        this.selected -= var0;

        while(this.selected < 0) {
            this.selected += 9;
        }

        while(this.selected >= 9) {
            this.selected -= 9;
        }

    }

    public int clearOrCountMatchingItems(Predicate<ItemStack> param0, int param1, Container param2) {
        int var0 = 0;
        boolean var1 = param1 == 0;
        var0 += ContainerHelper.clearOrCountMatchingItems(this, param0, param1 - var0, var1);
        var0 += ContainerHelper.clearOrCountMatchingItems(param2, param0, param1 - var0, var1);
        ItemStack var2 = this.player.containerMenu.getCarried();
        var0 += ContainerHelper.clearOrCountMatchingItems(var2, param0, param1 - var0, var1);
        if (var2.isEmpty()) {
            this.player.containerMenu.setCarried(ItemStack.EMPTY);
        }

        return var0;
    }

    private int addResource(ItemStack param0) {
        int var0 = this.getSlotWithRemainingSpace(param0);
        if (var0 == -1) {
            var0 = this.getFreeSlot();
        }

        return var0 == -1 ? param0.getCount() : this.addResource(var0, param0);
    }

    private int addResource(int param0, ItemStack param1) {
        Item var0 = param1.getItem();
        int var1 = param1.getCount();
        ItemStack var2 = this.getItem(param0);
        if (var2.isEmpty()) {
            var2 = new ItemStack(var0, 0);
            if (param1.hasTag()) {
                var2.setTag(param1.getTag().copy());
            }

            this.setItem(param0, var2);
        }

        int var3 = var1;
        if (var1 > var2.getMaxStackSize() - var2.getCount()) {
            var3 = var2.getMaxStackSize() - var2.getCount();
        }

        if (var3 > this.getMaxStackSize() - var2.getCount()) {
            var3 = this.getMaxStackSize() - var2.getCount();
        }

        if (var3 == 0) {
            return var1;
        } else {
            var1 -= var3;
            var2.grow(var3);
            var2.setPopTime(5);
            return var1;
        }
    }

    public int getSlotWithRemainingSpace(ItemStack param0) {
        if (this.hasRemainingSpaceForItem(this.getItem(this.selected), param0)) {
            return this.selected;
        } else if (this.hasRemainingSpaceForItem(this.getItem(40), param0)) {
            return 40;
        } else {
            for(int var0 = 0; var0 < this.items.size(); ++var0) {
                if (this.hasRemainingSpaceForItem(this.items.get(var0), param0)) {
                    return var0;
                }
            }

            return -1;
        }
    }

    public void tick() {
        for(NonNullList<ItemStack> var0 : this.compartments) {
            for(int var1 = 0; var1 < var0.size(); ++var1) {
                if (!var0.get(var1).isEmpty()) {
                    var0.get(var1).inventoryTick(this.player.level, this.player, var1, this.selected == var1);
                }
            }
        }

    }

    public boolean add(ItemStack param0) {
        return this.add(-1, param0);
    }

    public boolean add(int param0, ItemStack param1) {
        if (param1.isEmpty()) {
            return false;
        } else {
            try {
                if (param1.isDamaged()) {
                    if (param0 == -1) {
                        param0 = this.getFreeSlot();
                    }

                    if (param0 >= 0) {
                        this.items.set(param0, param1.copy());
                        this.items.get(param0).setPopTime(5);
                        param1.setCount(0);
                        return true;
                    } else if (this.player.getAbilities().instabuild) {
                        param1.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int var0;
                    do {
                        var0 = param1.getCount();
                        if (param0 == -1) {
                            param1.setCount(this.addResource(param1));
                        } else {
                            param1.setCount(this.addResource(param0, param1));
                        }
                    } while(!param1.isEmpty() && param1.getCount() < var0);

                    if (param1.getCount() == var0 && this.player.getAbilities().instabuild) {
                        param1.setCount(0);
                        return true;
                    } else {
                        return param1.getCount() < var0;
                    }
                }
            } catch (Throwable var6) {
                CrashReport var2 = CrashReport.forThrowable(var6, "Adding item to inventory");
                CrashReportCategory var3 = var2.addCategory("Item being added");
                var3.setDetail("Item ID", Item.getId(param1.getItem()));
                var3.setDetail("Item data", param1.getDamageValue());
                var3.setDetail("Item name", () -> param1.getHoverName().getString());
                throw new ReportedException(var2);
            }
        }
    }

    public void placeItemBackInInventory(ItemStack param0) {
        this.placeItemBackInInventory(param0, true);
    }

    public void placeItemBackInInventory(ItemStack param0, boolean param1) {
        while(!param0.isEmpty()) {
            int var0 = this.getSlotWithRemainingSpace(param0);
            if (var0 == -1) {
                var0 = this.getFreeSlot();
            }

            if (var0 == -1) {
                this.player.drop(param0, false);
                break;
            }

            int var1 = param0.getMaxStackSize() - this.getItem(var0).getCount();
            if (this.add(var0, param0.split(var1)) && param1 && this.player instanceof ServerPlayer) {
                ((ServerPlayer)this.player).connection.send(new ClientboundContainerSetSlotPacket(-2, 0, var0, this.getItem(var0)));
            }
        }

    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        List<ItemStack> var0 = null;

        for(NonNullList<ItemStack> var1 : this.compartments) {
            if (param0 < var1.size()) {
                var0 = var1;
                break;
            }

            param0 -= var1.size();
        }

        return var0 != null && !var0.get(param0).isEmpty() ? ContainerHelper.removeItem(var0, param0, param1) : ItemStack.EMPTY;
    }

    public void removeItem(ItemStack param0) {
        for(NonNullList<ItemStack> var0 : this.compartments) {
            for(int var1 = 0; var1 < var0.size(); ++var1) {
                if (var0.get(var1) == param0) {
                    var0.set(var1, ItemStack.EMPTY);
                    break;
                }
            }
        }

    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        NonNullList<ItemStack> var0 = null;

        for(NonNullList<ItemStack> var1 : this.compartments) {
            if (param0 < var1.size()) {
                var0 = var1;
                break;
            }

            param0 -= var1.size();
        }

        if (var0 != null && !var0.get(param0).isEmpty()) {
            ItemStack var2 = var0.get(param0);
            var0.set(param0, ItemStack.EMPTY);
            return var2;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        NonNullList<ItemStack> var0 = null;

        for(NonNullList<ItemStack> var1 : this.compartments) {
            if (param0 < var1.size()) {
                var0 = var1;
                break;
            }

            param0 -= var1.size();
        }

        if (var0 != null) {
            var0.set(param0, param1);
        }

    }

    public float getDestroySpeed(BlockState param0) {
        return this.items.get(this.selected).getDestroySpeed(param0);
    }

    public ListTag save(ListTag param0) {
        for(int var0 = 0; var0 < this.items.size(); ++var0) {
            if (!this.items.get(var0).isEmpty()) {
                CompoundTag var1 = new CompoundTag();
                var1.putByte("Slot", (byte)var0);
                this.items.get(var0).save(var1);
                param0.add(var1);
            }
        }

        for(int var2 = 0; var2 < this.armor.size(); ++var2) {
            if (!this.armor.get(var2).isEmpty()) {
                CompoundTag var3 = new CompoundTag();
                var3.putByte("Slot", (byte)(var2 + 100));
                this.armor.get(var2).save(var3);
                param0.add(var3);
            }
        }

        for(int var4 = 0; var4 < this.offhand.size(); ++var4) {
            if (!this.offhand.get(var4).isEmpty()) {
                CompoundTag var5 = new CompoundTag();
                var5.putByte("Slot", (byte)(var4 + 150));
                this.offhand.get(var4).save(var5);
                param0.add(var5);
            }
        }

        return param0;
    }

    public void load(ListTag param0) {
        this.items.clear();
        this.armor.clear();
        this.offhand.clear();

        for(int var0 = 0; var0 < param0.size(); ++var0) {
            CompoundTag var1 = param0.getCompound(var0);
            int var2 = var1.getByte("Slot") & 255;
            ItemStack var3 = ItemStack.of(var1);
            if (!var3.isEmpty()) {
                if (var2 >= 0 && var2 < this.items.size()) {
                    this.items.set(var2, var3);
                } else if (var2 >= 100 && var2 < this.armor.size() + 100) {
                    this.armor.set(var2 - 100, var3);
                } else if (var2 >= 150 && var2 < this.offhand.size() + 150) {
                    this.offhand.set(var2 - 150, var3);
                }
            }
        }

    }

    @Override
    public int getContainerSize() {
        return this.items.size() + this.armor.size() + this.offhand.size();
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack var0 : this.items) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        for(ItemStack var1 : this.armor) {
            if (!var1.isEmpty()) {
                return false;
            }
        }

        for(ItemStack var2 : this.offhand) {
            if (!var2.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int param0) {
        List<ItemStack> var0 = null;

        for(NonNullList<ItemStack> var1 : this.compartments) {
            if (param0 < var1.size()) {
                var0 = var1;
                break;
            }

            param0 -= var1.size();
        }

        return var0 == null ? ItemStack.EMPTY : var0.get(param0);
    }

    @Override
    public Component getName() {
        return Component.translatable("container.inventory");
    }

    public ItemStack getArmor(int param0) {
        return this.armor.get(param0);
    }

    public void hurtArmor(DamageSource param0, float param1, int[] param2) {
        if (!(param1 <= 0.0F)) {
            param1 /= 4.0F;
            if (param1 < 1.0F) {
                param1 = 1.0F;
            }

            for(int var0 : param2) {
                ItemStack var1 = this.armor.get(var0);
                if ((!param0.isFire() || !var1.getItem().isFireResistant()) && var1.getItem() instanceof ArmorItem) {
                    var1.hurtAndBreak(
                        (int)param1, this.player, param1x -> param1x.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, var0))
                    );
                }
            }

        }
    }

    public void dropAll() {
        for(List<ItemStack> var0 : this.compartments) {
            for(int var1 = 0; var1 < var0.size(); ++var1) {
                ItemStack var2 = var0.get(var1);
                if (!var2.isEmpty()) {
                    this.player.drop(var2, true, false);
                    var0.set(var1, ItemStack.EMPTY);
                }
            }
        }

    }

    @Override
    public void setChanged() {
        ++this.timesChanged;
    }

    public int getTimesChanged() {
        return this.timesChanged;
    }

    @Override
    public boolean stillValid(Player param0) {
        if (this.player.isRemoved()) {
            return false;
        } else {
            return !(param0.distanceToSqr(this.player) > 64.0);
        }
    }

    public boolean contains(ItemStack param0) {
        for(List<ItemStack> var0 : this.compartments) {
            for(ItemStack var1 : var0) {
                if (!var1.isEmpty() && var1.sameItem(param0)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean contains(TagKey<Item> param0) {
        for(List<ItemStack> var0 : this.compartments) {
            for(ItemStack var1 : var0) {
                if (!var1.isEmpty() && var1.is(param0)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void replaceWith(Inventory param0) {
        for(int var0 = 0; var0 < this.getContainerSize(); ++var0) {
            this.setItem(var0, param0.getItem(var0));
        }

        this.selected = param0.selected;
    }

    @Override
    public void clearContent() {
        for(List<ItemStack> var0 : this.compartments) {
            var0.clear();
        }

    }

    public void fillStackedContents(StackedContents param0) {
        for(ItemStack var0 : this.items) {
            param0.accountSimpleStack(var0);
        }

    }

    public ItemStack removeFromSelected(boolean param0) {
        ItemStack var0 = this.getSelected();
        return var0.isEmpty() ? ItemStack.EMPTY : this.removeItem(this.selected, param0 ? var0.getCount() : 1);
    }
}
