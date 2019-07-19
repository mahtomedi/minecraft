package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FishBucketItem extends BucketItem {
    private final EntityType<?> type;

    public FishBucketItem(EntityType<?> param0, Fluid param1, Item.Properties param2) {
        super(param1, param2);
        this.type = param0;
    }

    @Override
    public void checkExtraContent(Level param0, ItemStack param1, BlockPos param2) {
        if (!param0.isClientSide) {
            this.spawn(param0, param1, param2);
        }

    }

    @Override
    protected void playEmptySound(@Nullable Player param0, LevelAccessor param1, BlockPos param2) {
        param1.playSound(param0, param2, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private void spawn(Level param0, ItemStack param1, BlockPos param2) {
        Entity var0 = this.type.spawn(param0, param1, null, param2, MobSpawnType.BUCKET, true, false);
        if (var0 != null) {
            ((AbstractFish)var0).setFromBucket(true);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        if (this.type == EntityType.TROPICAL_FISH) {
            CompoundTag var0 = param0.getTag();
            if (var0 != null && var0.contains("BucketVariantTag", 3)) {
                int var1 = var0.getInt("BucketVariantTag");
                ChatFormatting[] var2 = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};
                String var3 = "color.minecraft." + TropicalFish.getBaseColor(var1);
                String var4 = "color.minecraft." + TropicalFish.getPatternColor(var1);

                for(int var5 = 0; var5 < TropicalFish.COMMON_VARIANTS.length; ++var5) {
                    if (var1 == TropicalFish.COMMON_VARIANTS[var5]) {
                        param2.add(new TranslatableComponent(TropicalFish.getPredefinedName(var5)).withStyle(var2));
                        return;
                    }
                }

                param2.add(new TranslatableComponent(TropicalFish.getFishTypeName(var1)).withStyle(var2));
                Component var6 = new TranslatableComponent(var3);
                if (!var3.equals(var4)) {
                    var6.append(", ").append(new TranslatableComponent(var4));
                }

                var6.withStyle(var2);
                param2.add(var6);
            }
        }

    }
}
