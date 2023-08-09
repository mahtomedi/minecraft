package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class SuspiciousStewItem extends Item {
    public static final String EFFECTS_TAG = "effects";
    public static final int DEFAULT_DURATION = 160;

    public SuspiciousStewItem(Item.Properties param0) {
        super(param0);
    }

    public static void saveMobEffects(ItemStack param0, List<SuspiciousEffectHolder.EffectEntry> param1) {
        CompoundTag var0 = param0.getOrCreateTag();
        SuspiciousEffectHolder.EffectEntry.LIST_CODEC.encodeStart(NbtOps.INSTANCE, param1).result().ifPresent(param1x -> var0.put("effects", param1x));
    }

    public static void appendMobEffects(ItemStack param0, List<SuspiciousEffectHolder.EffectEntry> param1) {
        CompoundTag var0 = param0.getOrCreateTag();
        List<SuspiciousEffectHolder.EffectEntry> var1 = new ArrayList<>();
        listPotionEffects(param0, var1::add);
        var1.addAll(param1);
        SuspiciousEffectHolder.EffectEntry.LIST_CODEC.encodeStart(NbtOps.INSTANCE, var1).result().ifPresent(param1x -> var0.put("effects", param1x));
    }

    private static void listPotionEffects(ItemStack param0, Consumer<SuspiciousEffectHolder.EffectEntry> param1) {
        CompoundTag var0 = param0.getTag();
        if (var0 != null && var0.contains("effects", 9)) {
            SuspiciousEffectHolder.EffectEntry.LIST_CODEC
                .parse(NbtOps.INSTANCE, var0.getList("effects", 10))
                .result()
                .ifPresent(param1x -> param1x.forEach(param1));
        }

    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        if (param3.isCreative()) {
            List<MobEffectInstance> var0 = new ArrayList<>();
            listPotionEffects(param0, param1x -> var0.add(param1x.createEffectInstance()));
            PotionUtils.addPotionTooltip(var0, param2, 1.0F);
        }

    }

    @Override
    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        ItemStack var0 = super.finishUsingItem(param0, param1, param2);
        listPotionEffects(var0, param1x -> param2.addEffect(param1x.createEffectInstance()));
        return param2 instanceof Player && ((Player)param2).getAbilities().instabuild ? var0 : new ItemStack(Items.BOWL);
    }
}
