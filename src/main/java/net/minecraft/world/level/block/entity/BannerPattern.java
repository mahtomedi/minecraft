package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class BannerPattern {
    final String hashname;

    public BannerPattern(String param0) {
        this.hashname = param0;
    }

    public static ResourceLocation location(ResourceKey<BannerPattern> param0, boolean param1) {
        String var0 = param1 ? "banner" : "shield";
        return param0.location().withPrefix("entity/" + var0 + "/");
    }

    public String getHashname() {
        return this.hashname;
    }

    @Nullable
    public static Holder<BannerPattern> byHash(String param0) {
        return Registry.BANNER_PATTERN.holders().filter(param1 -> param1.value().hashname.equals(param0)).findAny().orElse(null);
    }

    public static class Builder {
        private final List<Pair<Holder<BannerPattern>, DyeColor>> patterns = Lists.newArrayList();

        public BannerPattern.Builder addPattern(ResourceKey<BannerPattern> param0, DyeColor param1) {
            return this.addPattern(Registry.BANNER_PATTERN.getHolderOrThrow(param0), param1);
        }

        public BannerPattern.Builder addPattern(Holder<BannerPattern> param0, DyeColor param1) {
            return this.addPattern(Pair.of(param0, param1));
        }

        public BannerPattern.Builder addPattern(Pair<Holder<BannerPattern>, DyeColor> param0) {
            this.patterns.add(param0);
            return this;
        }

        public ListTag toListTag() {
            ListTag var0 = new ListTag();

            for(Pair<Holder<BannerPattern>, DyeColor> var1 : this.patterns) {
                CompoundTag var2 = new CompoundTag();
                var2.putString("Pattern", var1.getFirst().value().hashname);
                var2.putInt("Color", var1.getSecond().getId());
                var0.add(var2);
            }

            return var0;
        }
    }
}
