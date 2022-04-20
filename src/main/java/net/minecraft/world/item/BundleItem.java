package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;

public class BundleItem extends Item {
    private static final String TAG_ITEMS = "Items";
    public static final int MAX_WEIGHT = 64;
    private static final int BUNDLE_IN_BUNDLE_WEIGHT = 4;
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public BundleItem(Item.Properties param0) {
        super(param0);
    }

    public static float getFullnessDisplay(ItemStack param0) {
        return (float)getContentWeight(param0) / 64.0F;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack param0, Slot param1, ClickAction param2, Player param3) {
        if (param2 != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack var0 = param1.getItem();
            if (var0.isEmpty()) {
                this.playRemoveOneSound(param3);
                removeOne(param0).ifPresent(param2x -> add(param0, param1.safeInsert(param2x)));
            } else if (var0.getItem().canFitInsideContainerItems()) {
                int var1 = (64 - getContentWeight(param0)) / getWeight(var0);
                int var2 = add(param0, param1.safeTake(var0.getCount(), var1, param3));
                if (var2 > 0) {
                    this.playInsertSound(param3);
                }
            }

            return true;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack param0, ItemStack param1, Slot param2, ClickAction param3, Player param4, SlotAccess param5) {
        if (param3 == ClickAction.SECONDARY && param2.allowModification(param4)) {
            if (param1.isEmpty()) {
                removeOne(param0).ifPresent(param2x -> {
                    this.playRemoveOneSound(param4);
                    param5.set(param2x);
                });
            } else {
                int var0 = add(param0, param1);
                if (var0 > 0) {
                    this.playInsertSound(param4);
                    param1.shrink(var0);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        if (dropContents(var0, param1)) {
            this.playDropContentsSound(param1);
            param1.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(var0, param0.isClientSide());
        } else {
            return InteractionResultHolder.fail(var0);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack param0) {
        return getContentWeight(param0) > 0;
    }

    @Override
    public int getBarWidth(ItemStack param0) {
        return Math.min(1 + 12 * getContentWeight(param0) / 64, 13);
    }

    @Override
    public int getBarColor(ItemStack param0) {
        return BAR_COLOR;
    }

    private static int add(ItemStack param0, ItemStack param1) {
        if (!param1.isEmpty() && param1.getItem().canFitInsideContainerItems()) {
            CompoundTag var0 = param0.getOrCreateTag();
            if (!var0.contains("Items")) {
                var0.put("Items", new ListTag());
            }

            int var1 = getContentWeight(param0);
            int var2 = getWeight(param1);
            int var3 = Math.min(param1.getCount(), (64 - var1) / var2);
            if (var3 == 0) {
                return 0;
            } else {
                ListTag var4 = var0.getList("Items", 10);
                Optional<CompoundTag> var5 = getMatchingItem(param1, var4);
                if (var5.isPresent()) {
                    CompoundTag var6 = var5.get();
                    ItemStack var7 = ItemStack.of(var6);
                    var7.grow(var3);
                    var7.save(var6);
                    var4.remove(var6);
                    var4.add(0, var6);
                } else {
                    ItemStack var8 = param1.copy();
                    var8.setCount(var3);
                    CompoundTag var9 = new CompoundTag();
                    var8.save(var9);
                    var4.add(0, var9);
                }

                return var3;
            }
        } else {
            return 0;
        }
    }

    private static Optional<CompoundTag> getMatchingItem(ItemStack param0, ListTag param1) {
        return param0.is(Items.BUNDLE)
            ? Optional.empty()
            : param1.stream()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .filter(param1x -> ItemStack.isSameItemSameTags(ItemStack.of(param1x), param0))
                .findFirst();
    }

    private static int getWeight(ItemStack param0) {
        if (param0.is(Items.BUNDLE)) {
            return 4 + getContentWeight(param0);
        } else {
            if ((param0.is(Items.BEEHIVE) || param0.is(Items.BEE_NEST)) && param0.hasTag()) {
                CompoundTag var0 = BlockItem.getBlockEntityData(param0);
                if (var0 != null && !var0.getList("Bees", 10).isEmpty()) {
                    return 64;
                }
            }

            return 64 / param0.getMaxStackSize();
        }
    }

    private static int getContentWeight(ItemStack param0) {
        return getContents(param0).mapToInt(param0x -> getWeight(param0x) * param0x.getCount()).sum();
    }

    private static Optional<ItemStack> removeOne(ItemStack param0) {
        CompoundTag var0 = param0.getOrCreateTag();
        if (!var0.contains("Items")) {
            return Optional.empty();
        } else {
            ListTag var1 = var0.getList("Items", 10);
            if (var1.isEmpty()) {
                return Optional.empty();
            } else {
                int var2 = 0;
                CompoundTag var3 = var1.getCompound(0);
                ItemStack var4 = ItemStack.of(var3);
                var1.remove(0);
                if (var1.isEmpty()) {
                    param0.removeTagKey("Items");
                }

                return Optional.of(var4);
            }
        }
    }

    private static boolean dropContents(ItemStack param0, Player param1) {
        CompoundTag var0 = param0.getOrCreateTag();
        if (!var0.contains("Items")) {
            return false;
        } else {
            if (param1 instanceof ServerPlayer) {
                ListTag var1 = var0.getList("Items", 10);

                for(int var2 = 0; var2 < var1.size(); ++var2) {
                    CompoundTag var3 = var1.getCompound(var2);
                    ItemStack var4 = ItemStack.of(var3);
                    param1.drop(var4, true);
                }
            }

            param0.removeTagKey("Items");
            return true;
        }
    }

    private static Stream<ItemStack> getContents(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        if (var0 == null) {
            return Stream.empty();
        } else {
            ListTag var1 = var0.getList("Items", 10);
            return var1.stream().map(CompoundTag.class::cast).map(ItemStack::of);
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack param0) {
        NonNullList<ItemStack> var0 = NonNullList.create();
        getContents(param0).forEach(var0::add);
        return Optional.of(new BundleTooltip(var0, getContentWeight(param0)));
    }

    @Override
    public void appendHoverText(ItemStack param0, Level param1, List<Component> param2, TooltipFlag param3) {
        param2.add(Component.translatable("item.minecraft.bundle.fullness", getContentWeight(param0), 64).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void onDestroyed(ItemEntity param0) {
        ItemUtils.onContainerDestroyed(param0, getContents(param0.getItem()));
    }

    private void playRemoveOneSound(Entity param0) {
        param0.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + param0.getLevel().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity param0) {
        param0.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + param0.getLevel().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity param0) {
        param0.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + param0.getLevel().getRandom().nextFloat() * 0.4F);
    }
}
