package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class SuspiciousStewItem extends Item {
    public static final String EFFECTS_TAG = "Effects";
    public static final String EFFECT_ID_TAG = "EffectId";
    public static final String EFFECT_DURATION_TAG = "EffectDuration";
    public static final int DEFAULT_DURATION = 160;

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

    private static void listPotionEffects(ItemStack param0, Consumer<MobEffectInstance> param1) {
        CompoundTag var0 = param0.getTag();
        if (var0 != null && var0.contains("Effects", 9)) {
            ListTag var1 = var0.getList("Effects", 10);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                CompoundTag var3 = var1.getCompound(var2);
                int var4;
                if (var3.contains("EffectDuration", 3)) {
                    var4 = var3.getInt("EffectDuration");
                } else {
                    var4 = 160;
                }

                MobEffect var6 = MobEffect.byId(var3.getInt("EffectId"));
                if (var6 != null) {
                    param1.accept(new MobEffectInstance(var6, var4));
                }
            }
        }

    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        if (param3.isCreative()) {
            List<MobEffectInstance> var0 = new ArrayList<>();
            listPotionEffects(param0, var0::add);
            PotionUtils.addPotionTooltip(var0, param2, 1.0F);
        }

    }

    @Override
    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        ItemStack var0 = super.finishUsingItem(param0, param1, param2);
        listPotionEffects(var0, param2::addEffect);
        return param2 instanceof Player && ((Player)param2).getAbilities().instabuild ? var0 : new ItemStack(Items.BOWL);
    }
}
