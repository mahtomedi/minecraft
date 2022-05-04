package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class PotionItem extends Item {
    private static final int DRINK_DURATION = 32;

    public PotionItem(Item.Properties param0) {
        super(param0);
    }

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
            if (!var0.getAbilities().instabuild) {
                param0.shrink(1);
            }
        }

        if (var0 == null || !var0.getAbilities().instabuild) {
            if (param0.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }

            if (var0 != null) {
                var0.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        param1.gameEvent(param2, GameEvent.DRINK, param2.getEyePosition());
        return param0;
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        Player var2 = param0.getPlayer();
        ItemStack var3 = param0.getItemInHand();
        BlockState var4 = var0.getBlockState(var1);
        if (param0.getClickedFace() != Direction.DOWN && var4.is(BlockTags.CONVERTABLE_TO_MUD) && PotionUtils.getPotion(var3) == Potions.WATER) {
            var0.playSound(null, var1, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
            var2.setItemInHand(param0.getHand(), ItemUtils.createFilledResult(var3, var2, new ItemStack(Items.GLASS_BOTTLE)));
            var2.awardStat(Stats.ITEM_USED.get(var3.getItem()));
            if (!var0.isClientSide) {
                ServerLevel var5 = (ServerLevel)var0;

                for(int var6 = 0; var6 < 5; ++var6) {
                    var5.sendParticles(
                        ParticleTypes.SPLASH,
                        (double)var1.getX() + var0.random.nextDouble(),
                        (double)(var1.getY() + 1),
                        (double)var1.getZ() + var0.random.nextDouble(),
                        1,
                        0.0,
                        0.0,
                        0.0,
                        1.0
                    );
                }
            }

            var0.playSound(null, var1, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            var0.gameEvent(null, GameEvent.FLUID_PLACE, var1);
            var0.setBlockAndUpdate(var1, Blocks.MUD.defaultBlockState());
            return InteractionResult.sidedSuccess(var0.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
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
        return ItemUtils.startUsingInstantly(param0, param1, param2);
    }

    @Override
    public String getDescriptionId(ItemStack param0) {
        return PotionUtils.getPotion(param0).getName(this.getDescriptionId() + ".effect.");
    }

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
        if (this.allowedIn(param0)) {
            for(Potion var0 : Registry.POTION) {
                if (var0 != Potions.EMPTY) {
                    param1.add(PotionUtils.setPotion(new ItemStack(this), var0));
                }
            }
        }

    }
}
