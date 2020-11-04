package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum BannerPattern {
    BASE("base", "b", false),
    SQUARE_BOTTOM_LEFT("square_bottom_left", "bl"),
    SQUARE_BOTTOM_RIGHT("square_bottom_right", "br"),
    SQUARE_TOP_LEFT("square_top_left", "tl"),
    SQUARE_TOP_RIGHT("square_top_right", "tr"),
    STRIPE_BOTTOM("stripe_bottom", "bs"),
    STRIPE_TOP("stripe_top", "ts"),
    STRIPE_LEFT("stripe_left", "ls"),
    STRIPE_RIGHT("stripe_right", "rs"),
    STRIPE_CENTER("stripe_center", "cs"),
    STRIPE_MIDDLE("stripe_middle", "ms"),
    STRIPE_DOWNRIGHT("stripe_downright", "drs"),
    STRIPE_DOWNLEFT("stripe_downleft", "dls"),
    STRIPE_SMALL("small_stripes", "ss"),
    CROSS("cross", "cr"),
    STRAIGHT_CROSS("straight_cross", "sc"),
    TRIANGLE_BOTTOM("triangle_bottom", "bt"),
    TRIANGLE_TOP("triangle_top", "tt"),
    TRIANGLES_BOTTOM("triangles_bottom", "bts"),
    TRIANGLES_TOP("triangles_top", "tts"),
    DIAGONAL_LEFT("diagonal_left", "ld"),
    DIAGONAL_RIGHT("diagonal_up_right", "rd"),
    DIAGONAL_LEFT_MIRROR("diagonal_up_left", "lud"),
    DIAGONAL_RIGHT_MIRROR("diagonal_right", "rud"),
    CIRCLE_MIDDLE("circle", "mc"),
    RHOMBUS_MIDDLE("rhombus", "mr"),
    HALF_VERTICAL("half_vertical", "vh"),
    HALF_HORIZONTAL("half_horizontal", "hh"),
    HALF_VERTICAL_MIRROR("half_vertical_right", "vhr"),
    HALF_HORIZONTAL_MIRROR("half_horizontal_bottom", "hhb"),
    BORDER("border", "bo"),
    CURLY_BORDER("curly_border", "cbo"),
    GRADIENT("gradient", "gra"),
    GRADIENT_UP("gradient_up", "gru"),
    BRICKS("bricks", "bri"),
    GLOBE("globe", "glb", true),
    CREEPER("creeper", "cre", true),
    SKULL("skull", "sku", true),
    FLOWER("flower", "flo", true),
    MOJANG("mojang", "moj", true),
    PIGLIN("piglin", "pig", true);

    private static final BannerPattern[] VALUES = values();
    public static final int COUNT = VALUES.length;
    public static final int PATTERN_ITEM_COUNT = (int)Arrays.stream(VALUES).filter(param0 -> param0.hasPatternItem).count();
    public static final int AVAILABLE_PATTERNS = COUNT - PATTERN_ITEM_COUNT - 1;
    private final boolean hasPatternItem;
    private final String filename;
    private final String hashname;

    private BannerPattern(String param0, String param1) {
        this(param0, param1, false);
    }

    private BannerPattern(String param0, String param1, boolean param2) {
        this.filename = param0;
        this.hashname = param1;
        this.hasPatternItem = param2;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation location(boolean param0) {
        String var0 = param0 ? "banner" : "shield";
        return new ResourceLocation("entity/" + var0 + "/" + this.getFilename());
    }

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

    @Nullable
    public static BannerPattern byFilename(String param0) {
        for(BannerPattern var0 : values()) {
            if (var0.filename.equals(param0)) {
                return var0;
            }
        }

        return null;
    }

    public static class Builder {
        private final List<Pair<BannerPattern, DyeColor>> patterns = Lists.newArrayList();

        public BannerPattern.Builder addPattern(BannerPattern param0, DyeColor param1) {
            return this.addPattern(Pair.of(param0, param1));
        }

        public BannerPattern.Builder addPattern(Pair<BannerPattern, DyeColor> param0) {
            this.patterns.add(param0);
            return this;
        }

        public ListTag toListTag() {
            ListTag var0 = new ListTag();

            for(Pair<BannerPattern, DyeColor> var1 : this.patterns) {
                CompoundTag var2 = new CompoundTag();
                var2.putString("Pattern", var1.getFirst().hashname);
                var2.putInt("Color", var1.getSecond().getId());
                var0.add(var2);
            }

            return var0;
        }
    }
}
