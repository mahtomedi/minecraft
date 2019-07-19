package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

public enum BannerPattern {
    BASE("base", "b"),
    SQUARE_BOTTOM_LEFT("square_bottom_left", "bl", "   ", "   ", "#  "),
    SQUARE_BOTTOM_RIGHT("square_bottom_right", "br", "   ", "   ", "  #"),
    SQUARE_TOP_LEFT("square_top_left", "tl", "#  ", "   ", "   "),
    SQUARE_TOP_RIGHT("square_top_right", "tr", "  #", "   ", "   "),
    STRIPE_BOTTOM("stripe_bottom", "bs", "   ", "   ", "###"),
    STRIPE_TOP("stripe_top", "ts", "###", "   ", "   "),
    STRIPE_LEFT("stripe_left", "ls", "#  ", "#  ", "#  "),
    STRIPE_RIGHT("stripe_right", "rs", "  #", "  #", "  #"),
    STRIPE_CENTER("stripe_center", "cs", " # ", " # ", " # "),
    STRIPE_MIDDLE("stripe_middle", "ms", "   ", "###", "   "),
    STRIPE_DOWNRIGHT("stripe_downright", "drs", "#  ", " # ", "  #"),
    STRIPE_DOWNLEFT("stripe_downleft", "dls", "  #", " # ", "#  "),
    STRIPE_SMALL("small_stripes", "ss", "# #", "# #", "   "),
    CROSS("cross", "cr", "# #", " # ", "# #"),
    STRAIGHT_CROSS("straight_cross", "sc", " # ", "###", " # "),
    TRIANGLE_BOTTOM("triangle_bottom", "bt", "   ", " # ", "# #"),
    TRIANGLE_TOP("triangle_top", "tt", "# #", " # ", "   "),
    TRIANGLES_BOTTOM("triangles_bottom", "bts", "   ", "# #", " # "),
    TRIANGLES_TOP("triangles_top", "tts", " # ", "# #", "   "),
    DIAGONAL_LEFT("diagonal_left", "ld", "## ", "#  ", "   "),
    DIAGONAL_RIGHT("diagonal_up_right", "rd", "   ", "  #", " ##"),
    DIAGONAL_LEFT_MIRROR("diagonal_up_left", "lud", "   ", "#  ", "## "),
    DIAGONAL_RIGHT_MIRROR("diagonal_right", "rud", " ##", "  #", "   "),
    CIRCLE_MIDDLE("circle", "mc", "   ", " # ", "   "),
    RHOMBUS_MIDDLE("rhombus", "mr", " # ", "# #", " # "),
    HALF_VERTICAL("half_vertical", "vh", "## ", "## ", "## "),
    HALF_HORIZONTAL("half_horizontal", "hh", "###", "###", "   "),
    HALF_VERTICAL_MIRROR("half_vertical_right", "vhr", " ##", " ##", " ##"),
    HALF_HORIZONTAL_MIRROR("half_horizontal_bottom", "hhb", "   ", "###", "###"),
    BORDER("border", "bo", "###", "# #", "###"),
    CURLY_BORDER("curly_border", "cbo", new ItemStack(Blocks.VINE)),
    GRADIENT("gradient", "gra", "# #", " # ", " # "),
    GRADIENT_UP("gradient_up", "gru", " # ", " # ", "# #"),
    BRICKS("bricks", "bri", new ItemStack(Blocks.BRICKS)),
    GLOBE("globe", "glb"),
    CREEPER("creeper", "cre", new ItemStack(Items.CREEPER_HEAD)),
    SKULL("skull", "sku", new ItemStack(Items.WITHER_SKELETON_SKULL)),
    FLOWER("flower", "flo", new ItemStack(Blocks.OXEYE_DAISY)),
    MOJANG("mojang", "moj", new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));

    public static final int COUNT = values().length;
    public static final int AVAILABLE_PATTERNS = COUNT - 5 - 1;
    private final String filename;
    private final String hashname;
    private final String[] patterns = new String[3];
    private ItemStack patternItem = ItemStack.EMPTY;

    private BannerPattern(String param0, String param1) {
        this.filename = param0;
        this.hashname = param1;
    }

    private BannerPattern(String param0, String param1, ItemStack param2) {
        this(param0, param1);
        this.patternItem = param2;
    }

    private BannerPattern(String param0, String param1, String param2, String param3, String param4) {
        this(param0, param1);
        this.patterns[0] = param2;
        this.patterns[1] = param3;
        this.patterns[2] = param4;
    }

    @OnlyIn(Dist.CLIENT)
    public String getFilename() {
        return this.filename;
    }

    public String getHashname() {
        return this.hashname;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static BannerPattern byHash(String param0) {
        for(BannerPattern var0 : values()) {
            if (var0.hashname.equals(param0)) {
                return var0;
            }
        }

        return null;
    }

    public static class Builder {
        private final List<Pair<BannerPattern, DyeColor>> patterns = Lists.newArrayList();

        public BannerPattern.Builder addPattern(BannerPattern param0, DyeColor param1) {
            this.patterns.add(Pair.of(param0, param1));
            return this;
        }

        public ListTag toListTag() {
            ListTag var0 = new ListTag();

            for(Pair<BannerPattern, DyeColor> var1 : this.patterns) {
                CompoundTag var2 = new CompoundTag();
                var2.putString("Pattern", var1.getLeft().hashname);
                var2.putInt("Color", var1.getRight().getId());
                var0.add(var2);
            }

            return var0;
        }
    }
}
