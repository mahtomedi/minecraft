package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BundleItem extends Item {
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public BundleItem(Item.Properties param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static float getFullnessDisplay(ItemStack param0) {
        return (float)getContentWeight(param0) / 64.0F;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack param0, Slot param1, ClickAction param2, Inventory param3) {
        if (param2 != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack var0 = param1.getItem();
            if (var0.isEmpty()) {
                removeOne(param0).ifPresent(param2x -> add(param0, param1.safeInsert(param2x)));
            } else if (var0.getItem().canFitInsideContainerItems()) {
                int var1 = (64 - getContentWeight(param0)) / getWeight(var0);
                add(param0, param1.safeTake(var0.getCount(), var1, param3.player));
            }

            return true;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack param0, ItemStack param1, Slot param2, ClickAction param3, Inventory param4) {
        if (param3 == ClickAction.SECONDARY && param2.allowModification(param4.player)) {
            if (param1.isEmpty()) {
                removeOne(param0).ifPresent(param4::setCarried);
            } else {
                param1.shrink(add(param0, param1));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        return dropContents(var0, param1) ? InteractionResultHolder.sidedSuccess(var0, param0.isClientSide()) : InteractionResultHolder.fail(var0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isBarVisible(ItemStack param0) {
        int var0 = getContentWeight(param0);
        return var0 > 0 && var0 < 64;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getBarWidth(ItemStack param0) {
        return 13 * getContentWeight(param0) / 64 + 1;
    }

    @OnlyIn(Dist.CLIENT)
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
        return param0.is(Items.BUNDLE) ? 4 + getContentWeight(param0) : 64 / param0.getMaxStackSize();
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack param0) {
        NonNullList<ItemStack> var0 = NonNullList.create();
        getContents(param0).forEach(var0::add);
        return Optional.of(new BundleTooltip(var0, getContentWeight(param0) < 64));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, Level param1, List<Component> param2, TooltipFlag param3) {
        if (param3.isAdvanced()) {
            param2.add(new TranslatableComponent("item.minecraft.bundle.fullness", getContentWeight(param0), 64).withStyle(ChatFormatting.GRAY));
        }

    }
}
