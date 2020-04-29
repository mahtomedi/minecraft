package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WrittenBookItem extends Item {
    public WrittenBookItem(Item.Properties param0) {
        super(param0);
    }

    public static boolean makeSureTagIsValid(@Nullable CompoundTag param0) {
        if (!WritableBookItem.makeSureTagIsValid(param0)) {
            return false;
        } else if (!param0.contains("title", 8)) {
            return false;
        } else {
            String var0 = param0.getString("title");
            return var0.length() > 32 ? false : param0.contains("author", 8);
        }
    }

    public static int getGeneration(ItemStack param0) {
        return param0.getTag().getInt("generation");
    }

    public static int getPageCount(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        return var0 != null ? var0.getList("pages", 8).size() : 0;
    }

    @Override
    public Component getName(ItemStack param0) {
        if (param0.hasTag()) {
            CompoundTag var0 = param0.getTag();
            String var1 = var0.getString("title");
            if (!StringUtil.isNullOrEmpty(var1)) {
                return new TextComponent(var1);
            }
        }

        return super.getName(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        if (param0.hasTag()) {
            CompoundTag var0 = param0.getTag();
            String var1 = var0.getString("author");
            if (!StringUtil.isNullOrEmpty(var1)) {
                param2.add(new TranslatableComponent("book.byAuthor", var1).withStyle(ChatFormatting.GRAY));
            }

            param2.add(new TranslatableComponent("book.generation." + var0.getInt("generation")).withStyle(ChatFormatting.GRAY));
        }

    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        if (var2.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(var0, var1, var2, param0.getItemInHand()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        param1.openItemGui(var0, param2);
        param1.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.success(var0);
    }

    public static boolean resolveBookComponents(ItemStack param0, @Nullable CommandSourceStack param1, @Nullable Player param2) {
        CompoundTag var0 = param0.getTag();
        if (var0 != null && !var0.getBoolean("resolved")) {
            var0.putBoolean("resolved", true);
            if (!makeSureTagIsValid(var0)) {
                return false;
            } else {
                ListTag var1 = var0.getList("pages", 8);

                for(int var2 = 0; var2 < var1.size(); ++var2) {
                    String var3 = var1.getString(var2);

                    Component var6;
                    try {
                        var6 = Component.Serializer.fromJsonLenient(var3);
                        var6 = ComponentUtils.updateForEntity(param1, var6, param2, 0);
                    } catch (Exception var9) {
                        var6 = new TextComponent(var3);
                    }

                    var1.set(var2, (Tag)StringTag.valueOf(Component.Serializer.toJson(var6)));
                }

                var0.put("pages", var1);
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return true;
    }
}
