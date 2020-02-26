package net.minecraft.world.item;

import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TridentItem extends Item {
    public TridentItem(Item.Properties param0) {
        super(param0);
        this.addProperty(
            new ResourceLocation("throwing"),
            (param0x, param1, param2) -> param2 != null && param2.isUsingItem() && param2.getUseItem() == param0x ? 1.0F : 0.0F
        );
    }

    @Override
    public boolean canAttackBlock(BlockState param0, Level param1, BlockPos param2, Player param3) {
        return !param3.isCreative();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack param0) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack param0, Level param1, LivingEntity param2, int param3) {
        if (param2 instanceof Player) {
            Player var0 = (Player)param2;
            int var1 = this.getUseDuration(param0) - param3;
            if (var1 >= 10) {
                int var2 = EnchantmentHelper.getRiptide(param0);
                if (var2 <= 0 || var0.isInWaterOrRain()) {
                    if (!param1.isClientSide) {
                        param0.hurtAndBreak(1, var0, param1x -> param1x.broadcastBreakEvent(param2.getUsedItemHand()));
                        if (var2 == 0) {
                            ThrownTrident var3 = new ThrownTrident(param1, var0, param0);
                            var3.shootFromRotation(var0, var0.xRot, var0.yRot, 0.0F, 2.5F + (float)var2 * 0.5F, 1.0F);
                            if (var0.abilities.instabuild) {
                                var3.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }

                            param1.addFreshEntity(var3);
                            param1.playSound(null, var3, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
                            if (!var0.abilities.instabuild) {
                                var0.inventory.removeItem(param0);
                            }
                        }
                    }

                    var0.awardStat(Stats.ITEM_USED.get(this));
                    if (var2 > 0) {
                        float var4 = var0.yRot;
                        float var5 = var0.xRot;
                        float var6 = -Mth.sin(var4 * (float) (Math.PI / 180.0)) * Mth.cos(var5 * (float) (Math.PI / 180.0));
                        float var7 = -Mth.sin(var5 * (float) (Math.PI / 180.0));
                        float var8 = Mth.cos(var4 * (float) (Math.PI / 180.0)) * Mth.cos(var5 * (float) (Math.PI / 180.0));
                        float var9 = Mth.sqrt(var6 * var6 + var7 * var7 + var8 * var8);
                        float var10 = 3.0F * ((1.0F + (float)var2) / 4.0F);
                        var6 *= var10 / var9;
                        var7 *= var10 / var9;
                        var8 *= var10 / var9;
                        var0.push((double)var6, (double)var7, (double)var8);
                        var0.startAutoSpinAttack(20);
                        if (var0.isOnGround()) {
                            float var11 = 1.1999999F;
                            var0.move(MoverType.SELF, new Vec3(0.0, 1.1999999F, 0.0));
                        }

                        SoundEvent var12;
                        if (var2 >= 3) {
                            var12 = SoundEvents.TRIDENT_RIPTIDE_3;
                        } else if (var2 == 2) {
                            var12 = SoundEvents.TRIDENT_RIPTIDE_2;
                        } else {
                            var12 = SoundEvents.TRIDENT_RIPTIDE_1;
                        }

                        param1.playSound(null, var0, var12, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }

                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        if (var0.getDamageValue() >= var0.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(var0);
        } else if (EnchantmentHelper.getRiptide(var0) > 0 && !param1.isInWaterOrRain()) {
            return InteractionResultHolder.fail(var0);
        } else {
            param1.startUsingItem(param2);
            return InteractionResultHolder.consume(var0);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack param0, LivingEntity param1, LivingEntity param2) {
        param0.hurtAndBreak(1, param2, param0x -> param0x.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack param0, Level param1, BlockState param2, BlockPos param3, LivingEntity param4) {
        if ((double)param2.getDestroySpeed(param1, param3) != 0.0) {
            param0.hurtAndBreak(2, param4, param0x -> param0x.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        return true;
    }

    @Override
    public Multimap<String, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot param0) {
        Multimap<String, AttributeModifier> var0 = super.getDefaultAttributeModifiers(param0);
        if (param0 == EquipmentSlot.MAINHAND) {
            var0.put(
                SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0, AttributeModifier.Operation.ADDITION)
            );
            var0.put(
                SharedMonsterAttributes.ATTACK_SPEED.getName(),
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.9F, AttributeModifier.Operation.ADDITION)
            );
        }

        return var0;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}
