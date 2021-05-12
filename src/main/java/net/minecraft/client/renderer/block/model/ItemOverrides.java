package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemOverrides {
    public static final ItemOverrides EMPTY = new ItemOverrides();
    private final ItemOverrides.BakedOverride[] overrides;
    private final ResourceLocation[] properties;

    private ItemOverrides() {
        this.overrides = new ItemOverrides.BakedOverride[0];
        this.properties = new ResourceLocation[0];
    }

    public ItemOverrides(ModelBakery param0, BlockModel param1, Function<ResourceLocation, UnbakedModel> param2, List<ItemOverride> param3) {
        this.properties = param3.stream()
            .flatMap(ItemOverride::getPredicates)
            .map(ItemOverride.Predicate::getProperty)
            .distinct()
            .toArray(param0x -> new ResourceLocation[param0x]);
        Object2IntMap<ResourceLocation> var0 = new Object2IntOpenHashMap<>();

        for(int var1 = 0; var1 < this.properties.length; ++var1) {
            var0.put(this.properties[var1], var1);
        }

        List<ItemOverrides.BakedOverride> var2 = Lists.newArrayList();

        for(int var3 = param3.size() - 1; var3 >= 0; --var3) {
            ItemOverride var4 = param3.get(var3);
            BakedModel var5 = this.bakeModel(param0, param1, param2, var4);
            ItemOverrides.PropertyMatcher[] var6 = var4.getPredicates().map(param1x -> {
                int var0x = var0.getInt(param1x.getProperty());
                return new ItemOverrides.PropertyMatcher(var0x, param1x.getValue());
            }).toArray(param0x -> new ItemOverrides.PropertyMatcher[param0x]);
            var2.add(new ItemOverrides.BakedOverride(var6, var5));
        }

        this.overrides = var2.toArray(new ItemOverrides.BakedOverride[0]);
    }

    @Nullable
    private BakedModel bakeModel(ModelBakery param0, BlockModel param1, Function<ResourceLocation, UnbakedModel> param2, ItemOverride param3) {
        UnbakedModel var0 = param2.apply(param3.getModel());
        return Objects.equals(var0, param1) ? null : param0.bake(param3.getModel(), BlockModelRotation.X0_Y0);
    }

    @Nullable
    public BakedModel resolve(BakedModel param0, ItemStack param1, @Nullable ClientLevel param2, @Nullable LivingEntity param3, int param4) {
        if (this.overrides.length != 0) {
            Item var0 = param1.getItem();
            int var1 = this.properties.length;
            float[] var2 = new float[var1];

            for(int var3 = 0; var3 < var1; ++var3) {
                ResourceLocation var4 = this.properties[var3];
                ItemPropertyFunction var5 = ItemProperties.getProperty(var0, var4);
                if (var5 != null) {
                    var2[var3] = var5.call(param1, param2, param3, param4);
                } else {
                    var2[var3] = Float.NEGATIVE_INFINITY;
                }
            }

            for(ItemOverrides.BakedOverride var6 : this.overrides) {
                if (var6.test(var2)) {
                    BakedModel var7 = var6.model;
                    if (var7 == null) {
                        return param0;
                    }

                    return var7;
                }
            }
        }

        return param0;
    }

    @OnlyIn(Dist.CLIENT)
    static class BakedOverride {
        private final ItemOverrides.PropertyMatcher[] matchers;
        @Nullable
        final BakedModel model;

        BakedOverride(ItemOverrides.PropertyMatcher[] param0, @Nullable BakedModel param1) {
            this.matchers = param0;
            this.model = param1;
        }

        boolean test(float[] param0) {
            for(ItemOverrides.PropertyMatcher var0 : this.matchers) {
                float var1 = param0[var0.index];
                if (var1 < var0.value) {
                    return false;
                }
            }

            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class PropertyMatcher {
        public final int index;
        public final float value;

        PropertyMatcher(int param0, float param1) {
            this.index = param0;
            this.value = param1;
        }
    }
}
