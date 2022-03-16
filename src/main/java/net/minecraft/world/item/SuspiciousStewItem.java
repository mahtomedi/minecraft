package net.minecraft.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SuspiciousStewItem extends Item {
    public static final String EFFECTS_TAG = "Effects";
    public static final String EFFECT_ID_TAG = "EffectId";
    public static final String EFFECT_DURATION_TAG = "EffectDuration";

    public SuspiciousStewItem(Item.Properties param0) {
        super(param0);
    }

    public static void saveMobEffect(ItemStack param0, MobEffect param1, int param2) {
        CompoundTag var0 = param0.getOrCreateTag();
        ListTag var1 = var0.getList("Effects", 9);
        CompoundTag var2 = new CompoundTag();
        var2.putInt("EffectId", MobEffect.getId(param1));
        var2.putInt("EffectDuration", param2);
        var1.add(var2);
        var0.put("Effects", var1);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        ItemStack var0 = super.finishUsingItem(param0, param1, param2);
        CompoundTag var1 = param0.getTag();
        if (var1 != null && var1.contains("Effects", 9)) {
            ListTag var2 = var1.getList("Effects", 10);

            for(int var3 = 0; var3 < var2.size(); ++var3) {
                int var4 = 160;
                CompoundTag var5 = var2.getCompound(var3);
                if (var5.contains("EffectDuration", 3)) {
                    var4 = var5.getInt("EffectDuration");
                }

                MobEffect var6 = MobEffect.byId(var5.getInt("EffectId"));
                if (var6 != null) {
                    param2.addEffect(new MobEffectInstance(var6, var4));
                }
            }
        }

        return param2 instanceof Player && ((Player)param2).getAbilities().instabuild ? var0 : new ItemStack(Items.BOWL);
    }
}
