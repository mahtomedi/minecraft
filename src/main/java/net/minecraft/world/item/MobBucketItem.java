package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public class MobBucketItem extends BucketItem {
    private final EntityType<?> type;
    private final SoundEvent emptySound;

    public MobBucketItem(EntityType<?> param0, Fluid param1, SoundEvent param2, Item.Properties param3) {
        super(param1, param3);
        this.type = param0;
        this.emptySound = param2;
    }

    @Override
    public void checkExtraContent(@Nullable Player param0, Level param1, ItemStack param2, BlockPos param3) {
        if (param1 instanceof ServerLevel) {
            this.spawn((ServerLevel)param1, param2, param3);
            param1.gameEvent(param0, GameEvent.ENTITY_PLACE, param3);
        }

    }

    @Override
    protected void playEmptySound(@Nullable Player param0, LevelAccessor param1, BlockPos param2) {
        param1.playSound(param0, param2, this.emptySound, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private void spawn(ServerLevel param0, ItemStack param1, BlockPos param2) {
        Entity var0 = this.type.spawn(param0, param1, null, param2, MobSpawnType.BUCKET, true, false);
        if (var0 instanceof Bucketable var1) {
            var1.loadFromBucketTag(param1.getOrCreateTag());
            var1.setFromBucket(true);
        }

    }

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
                MutableComponent var6 = new TranslatableComponent(var3);
                if (!var3.equals(var4)) {
                    var6.append(", ").append(new TranslatableComponent(var4));
                }

                var6.withStyle(var2);
                param2.add(var6);
            }
        }

    }
}
