package net.minecraft.world.item;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
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
    public boolean overrideStackedOnOther(ItemStack param0, ItemStack param1, ClickAction param2, Inventory param3) {
        if (param2 == ClickAction.SECONDARY) {
            add(param0, param1);
            return true;
        } else {
            return super.overrideStackedOnOther(param0, param1, param2, param3);
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack param0, ItemStack param1, ClickAction param2, Inventory param3) {
        if (param2 == ClickAction.SECONDARY) {
            if (param1.isEmpty()) {
                removeAll(param0, param3);
            } else {
                add(param0, param1);
            }

            return true;
        } else {
            return super.overrideOtherStackedOnMe(param0, param1, param2, param3);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        removeAll(var0, param1.getInventory());
        return InteractionResultHolder.success(var0);
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

    private static void add(ItemStack param0, ItemStack param1) {
        if (param1.getItem().canFitInsideContainerItems()) {
            CompoundTag var0 = param0.getOrCreateTag();
            if (!var0.contains("Items")) {
                var0.put("Items", new ListTag());
            }

            int var1 = getContentWeight(param0);
            int var2 = getWeight(param1);
            int var3 = Math.min(param1.getCount(), (64 - var1) / var2);
            if (var3 != 0) {
                ListTag var4 = var0.getList("Items", 10);
                Optional<CompoundTag> var5 = getMatchingItem(param1, var4);
                if (var5.isPresent()) {
                    CompoundTag var6 = var5.get();
                    ItemStack var7 = ItemStack.of(var6);
                    var7.grow(var3);
                    var7.save(var6);
                } else {
                    ItemStack var8 = param1.copy();
                    var8.setCount(var3);
                    CompoundTag var9 = new CompoundTag();
                    var8.save(var9);
                    var4.add(var9);
                }

                param1.shrink(var3);
            }
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

    private static void removeAll(ItemStack param0, Inventory param1) {
        getContents(param0).forEach(param1x -> {
            if (param1.player instanceof ServerPlayer || param1.player.isCreative()) {
                param1.placeItemBackInInventory(param1x);
            }

        });
        param0.removeTagKey("Items");
    }

    private static Stream<ItemStack> getContents(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        if (var0 == null) {
            return Stream.empty();
        } else {
            ListTag var1 = var0.getList("Items", 10);
            return var1.stream().map(param0x -> ItemStack.of((CompoundTag)param0x));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack param0) {
        NonNullList<ItemStack> var0 = NonNullList.create();
        getContents(param0).forEach(var0::add);
        return Optional.of(new BundleTooltip(var0, getContentWeight(param0) < 64));
    }
}
