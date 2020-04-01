package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;

public interface BookAccess {
    BookAccess EMPTY_ACCESS = new BookAccess() {
        @Override
        public int getPageCount() {
            return 0;
        }

        @Override
        public Component getPageRaw(int param0) {
            return new TextComponent("");
        }
    };

    static List<String> convertPages(CompoundTag param0) {
        ListTag var0 = param0.getList("pages", 8).copy();
        Builder<String> var1 = ImmutableList.builder();

        for(int var2 = 0; var2 < var0.size(); ++var2) {
            var1.add(var0.getString(var2));
        }

        return var1.build();
    }

    int getPageCount();

    Component getPageRaw(int var1);

    default Component getPage(int param0) {
        return (Component)(param0 >= 0 && param0 < this.getPageCount() ? this.getPageRaw(param0) : new TextComponent(""));
    }

    static BookAccess fromItem(ItemStack param0) {
        Item var0 = param0.getItem();
        if (var0 == Items.WRITTEN_BOOK) {
            return new BookAccess.WrittenBookAccess(param0);
        } else {
            return (BookAccess)(var0 == Items.WRITABLE_BOOK ? new BookAccess.WritableBookAccess(param0) : EMPTY_ACCESS);
        }
    }

    public static class WritableBookAccess implements BookAccess {
        private final List<String> pages;

        public WritableBookAccess(ItemStack param0) {
            this.pages = readPages(param0);
        }

        private static List<String> readPages(ItemStack param0) {
            CompoundTag var0 = param0.getTag();
            return (List<String>)(var0 != null ? BookAccess.convertPages(var0) : ImmutableList.of());
        }

        @Override
        public int getPageCount() {
            return this.pages.size();
        }

        @Override
        public Component getPageRaw(int param0) {
            return new TextComponent(this.pages.get(param0));
        }
    }

    public static class WrittenBookAccess implements BookAccess {
        private final List<String> pages;

        public WrittenBookAccess(ItemStack param0) {
            this.pages = readPages(param0);
        }

        private static List<String> readPages(ItemStack param0) {
            CompoundTag var0 = param0.getTag();
            return (List<String>)(var0 != null && WrittenBookItem.makeSureTagIsValid(var0)
                ? BookAccess.convertPages(var0)
                : ImmutableList.of(new TranslatableComponent("book.invalid.tag").withStyle(ChatFormatting.DARK_RED).getColoredString()));
        }

        @Override
        public int getPageCount() {
            return this.pages.size();
        }

        @Override
        public Component getPageRaw(int param0) {
            String var0 = this.pages.get(param0);

            try {
                Component var1 = Component.Serializer.fromJson(var0);
                if (var1 != null) {
                    return var1;
                }
            } catch (Exception var4) {
            }

            return new TextComponent(var0);
        }
    }
}
