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
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WrittenBookItem extends Item {
    public static final int TITLE_LENGTH = 16;
    public static final int TITLE_MAX_LENGTH = 32;
    public static final int PAGE_EDIT_LENGTH = 1024;
    public static final int PAGE_LENGTH = 32767;
    public static final int MAX_PAGES = 100;
    public static final int MAX_GENERATION = 2;
    public static final String TAG_TITLE = "title";
    public static final String TAG_FILTERED_TITLE = "filtered_title";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_PAGES = "pages";
    public static final String TAG_FILTERED_PAGES = "filtered_pages";
    public static final String TAG_GENERATION = "generation";
    public static final String TAG_RESOLVED = "resolved";

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
        CompoundTag var0 = param0.getTag();
        if (var0 != null) {
            String var1 = var0.getString("title");
            if (!StringUtil.isNullOrEmpty(var1)) {
                return Component.literal(var1);
            }
        }

        return super.getName(param0);
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        if (param0.hasTag()) {
            CompoundTag var0 = param0.getTag();
            String var1 = var0.getString("author");
            if (!StringUtil.isNullOrEmpty(var1)) {
                param2.add(Component.translatable("book.byAuthor", var1).withStyle(ChatFormatting.GRAY));
            }

            param2.add(Component.translatable("book.generation." + var0.getInt("generation")).withStyle(ChatFormatting.GRAY));
        }

    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        if (var2.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(param0.getPlayer(), var0, var1, var2, param0.getItemInHand())
                ? InteractionResult.sidedSuccess(var0.isClientSide)
                : InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        param1.openItemGui(var0, param2);
        param1.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(var0, param0.isClientSide());
    }

    public static boolean resolveBookComponents(ItemStack param0, @Nullable CommandSourceStack param1, @Nullable Player param2) {
        CompoundTag var0 = param0.getTag();
        if (var0 != null && !var0.getBoolean("resolved")) {
            var0.putBoolean("resolved", true);
            if (!makeSureTagIsValid(var0)) {
                return false;
            } else {
                ListTag var1 = var0.getList("pages", 8);
                ListTag var2 = new ListTag();

                for(int var3 = 0; var3 < var1.size(); ++var3) {
                    String var4 = resolvePage(param1, param2, var1.getString(var3));
                    if (var4.length() > 32767) {
                        return false;
                    }

                    var2.add(var3, (Tag)StringTag.valueOf(var4));
                }

                if (var0.contains("filtered_pages", 10)) {
                    CompoundTag var5 = var0.getCompound("filtered_pages");
                    CompoundTag var6 = new CompoundTag();

                    for(String var7 : var5.getAllKeys()) {
                        String var8 = resolvePage(param1, param2, var5.getString(var7));
                        if (var8.length() > 32767) {
                            return false;
                        }

                        var6.putString(var7, var8);
                    }

                    var0.put("filtered_pages", var6);
                }

                var0.put("pages", var2);
                return true;
            }
        } else {
            return false;
        }
    }

    private static String resolvePage(@Nullable CommandSourceStack param0, @Nullable Player param1, String param2) {
        Component var2;
        try {
            var2 = Component.Serializer.fromJsonLenient(param2);
            var2 = ComponentUtils.updateForEntity(param0, var2, param1, 0);
        } catch (Exception var5) {
            var2 = Component.literal(param2);
        }

        return Component.Serializer.toJson(var2);
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return true;
    }
}
