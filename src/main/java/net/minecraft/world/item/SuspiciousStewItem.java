package net.minecraft.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class SuspiciousStewItem extends Item {
    public SuspiciousStewItem(Item.Properties param0) {
        super(param0);
    }

    public static void saveMobEffect(ItemStack param0, MobEffect param1, int param2) {
        CompoundTag var0 = param0.getOrCreateTag();
        ListTag var1 = var0.getList("Effects", 9);
        CompoundTag var2 = new CompoundTag();
        var2.putByte("EffectId", (byte)MobEffect.getId(param1));
        var2.putInt("EffectDuration", param2);
        var1.add(var2);
        var0.put("Effects", var1);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        super.finishUsingItem(param0, param1, param2);
        CompoundTag var0 = param0.getTag();
        if (var0 != null && var0.contains("Effects", 9)) {
            ListTag var1 = var0.getList("Effects", 10);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                int var3 = 160;
                CompoundTag var4 = var1.getCompound(var2);
                if (var4.contains("EffectDuration", 3)) {
                    var3 = var4.getInt("EffectDuration");
                }

                MobEffect var5 = MobEffect.byId(var4.getByte("EffectId"));
                if (var5 != null) {
                    param2.addEffect(new MobEffectInstance(var5, var3));
                }
            }
        }

        return new ItemStack(Items.BOWL);
    }
}
