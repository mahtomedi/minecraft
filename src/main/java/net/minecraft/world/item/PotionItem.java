package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionItem extends Item {
    public PotionItem(Item.Properties param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getDefaultInstance() {
        return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        Player var0 = param2 instanceof Player ? (Player)param2 : null;
        if (var0 instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)var0, param0);
        }

        if (!param1.isClientSide) {
            for(MobEffectInstance var2 : PotionUtils.getMobEffects(param0)) {
                if (var2.getEffect().isInstantenous()) {
                    var2.getEffect().applyInstantenousEffect(var0, var0, param2, var2.getAmplifier(), 1.0);
                } else {
                    param2.addEffect(new MobEffectInstance(var2));
                }
            }
        }

        if (var0 != null) {
            var0.awardStat(Stats.ITEM_USED.get(this));
            if (!var0.abilities.instabuild) {
                param0.shrink(1);
            }
        }

        if (var0 == null || !var0.abilities.instabuild) {
            if (param0.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }

            if (var0 != null) {
                var0.inventory.add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        return param0;
    }

    @Override
    public int getUseDuration(ItemStack param0) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        param1.startUsingItem(param2);
        return InteractionResultHolder.success(param1.getItemInHand(param2));
    }

    @Override
    public String getDescriptionId(ItemStack param0) {
        return PotionUtils.getPotion(param0).getName(this.getDescriptionId() + ".effect.");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        PotionUtils.addPotionTooltip(param0, param2, 1.0F);
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return super.isFoil(param0) || !PotionUtils.getMobEffects(param0).isEmpty();
    }

    @Override
    public void fillItemCategory(CreativeModeTab param0, NonNullList<ItemStack> param1) {
        if (this.allowdedIn(param0)) {
            for(Potion var0 : Registry.POTION) {
                if (var0 != Potions.EMPTY) {
                    param1.add(PotionUtils.setPotion(new ItemStack(this), var0));
                }
            }
        }

    }
}
