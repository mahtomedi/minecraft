package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemOverrides {
    public static final ItemOverrides EMPTY = new ItemOverrides();
    private final List<ItemOverride> overrides = Lists.newArrayList();
    private final List<BakedModel> overrideModels;

    private ItemOverrides() {
        this.overrideModels = Collections.emptyList();
    }

    public ItemOverrides(ModelBakery param0, BlockModel param1, Function<ResourceLocation, UnbakedModel> param2, List<ItemOverride> param3) {
        this.overrideModels = param3.stream().map(param3x -> {
            UnbakedModel var0x = param2.apply(param3x.getModel());
            return Objects.equals(var0x, param1) ? null : param0.bake(param3x.getModel(), BlockModelRotation.X0_Y0);
        }).collect(Collectors.toList());
        Collections.reverse(this.overrideModels);

        for(int var0 = param3.size() - 1; var0 >= 0; --var0) {
            this.overrides.add(param3.get(var0));
        }

    }

    @Nullable
    public BakedModel resolve(BakedModel param0, ItemStack param1, @Nullable ClientLevel param2, @Nullable LivingEntity param3) {
        if (!this.overrides.isEmpty()) {
            for(int var0 = 0; var0 < this.overrides.size(); ++var0) {
                ItemOverride var1 = this.overrides.get(var0);
                if (var1.test(param1, param2, param3)) {
                    BakedModel var2 = this.overrideModels.get(var0);
                    if (var2 == null) {
                        return param0;
                    }

                    return var2;
                }
            }
        }

        return param0;
    }
}
