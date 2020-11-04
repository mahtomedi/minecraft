package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
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
        return var0 != 0 && var0 != 64;
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
                Optional<Tag> var5 = var4.stream()
                    .filter(param1x -> param1x instanceof CompoundTag && ItemStack.isSameItemSameTags(ItemStack.of((CompoundTag)param1x), param1))
                    .findFirst();
                if (var5.isPresent()) {
                    CompoundTag var6 = (CompoundTag)var5.get();
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

    private static int getWeight(ItemStack param0) {
        return param0.is(Items.BUNDLE) ? 4 + getContentWeight(param0) : 64 / param0.getMaxStackSize();
    }

    private static int getContentWeight(ItemStack param0) {
        CompoundTag var0 = param0.getOrCreateTag();
        if (!var0.contains("Items")) {
            return 0;
        } else {
            ListTag var1 = var0.getList("Items", 10);
            return var1.stream().map(param0x -> ItemStack.of((CompoundTag)param0x)).mapToInt(param0x -> getWeight(param0x) * param0x.getCount()).sum();
        }
    }

    private static void removeAll(ItemStack param0, Inventory param1) {
        CompoundTag var0 = param0.getOrCreateTag();
        if (var0.contains("Items")) {
            ListTag var1 = var0.getList("Items", 10);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                CompoundTag var3 = var1.getCompound(var2);
                ItemStack var4 = ItemStack.of(var3);
                if (param1.player instanceof ServerPlayer || param1.player.isCreative()) {
                    param1.placeItemBackInInventory(var4);
                }
            }

            param0.removeTagKey("Items");
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        CompoundTag var0 = param0.getOrCreateTag();
        if (var0.contains("Items", 9)) {
            ListTag var1 = var0.getList("Items", 10);
            int var2 = 0;
            int var3 = 0;

            for(Tag var4 : var1) {
                ItemStack var5 = ItemStack.of((CompoundTag)var4);
                if (!var5.isEmpty()) {
                    ++var3;
                    if (var2 <= 8) {
                        ++var2;
                        MutableComponent var6 = var5.getHoverName().copy();
                        var6.append(" x").append(String.valueOf(var5.getCount()));
                        param2.add(var6);
                    }
                }
            }

            if (var3 - var2 > 0) {
                param2.add(new TranslatableComponent("container.shulkerBox.more", var3 - var2).withStyle(ChatFormatting.ITALIC));
            }
        }

    }
}
