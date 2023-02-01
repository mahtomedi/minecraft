package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public class PotionUtils {
    public static final String TAG_CUSTOM_POTION_EFFECTS = "CustomPotionEffects";
    public static final String TAG_CUSTOM_POTION_COLOR = "CustomPotionColor";
    public static final String TAG_POTION = "Potion";
    private static final int EMPTY_COLOR = 16253176;
    private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);

    public static List<MobEffectInstance> getMobEffects(ItemStack param0) {
        return getAllEffects(param0.getTag());
    }

    public static List<MobEffectInstance> getAllEffects(Potion param0, Collection<MobEffectInstance> param1) {
        List<MobEffectInstance> var0 = Lists.newArrayList();
        var0.addAll(param0.getEffects());
        var0.addAll(param1);
        return var0;
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag param0) {
        List<MobEffectInstance> var0 = Lists.newArrayList();
        var0.addAll(getPotion(param0).getEffects());
        getCustomEffects(param0, var0);
        return var0;
    }

    public static List<MobEffectInstance> getCustomEffects(ItemStack param0) {
        return getCustomEffects(param0.getTag());
    }

    public static List<MobEffectInstance> getCustomEffects(@Nullable CompoundTag param0) {
        List<MobEffectInstance> var0 = Lists.newArrayList();
        getCustomEffects(param0, var0);
        return var0;
    }

    public static void getCustomEffects(@Nullable CompoundTag param0, List<MobEffectInstance> param1) {
        if (param0 != null && param0.contains("CustomPotionEffects", 9)) {
            ListTag var0 = param0.getList("CustomPotionEffects", 10);

            for(int var1 = 0; var1 < var0.size(); ++var1) {
                CompoundTag var2 = var0.getCompound(var1);
                MobEffectInstance var3 = MobEffectInstance.load(var2);
                if (var3 != null) {
                    param1.add(var3);
                }
            }
        }

    }

    public static int getColor(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        if (var0 != null && var0.contains("CustomPotionColor", 99)) {
            return var0.getInt("CustomPotionColor");
        } else {
            return getPotion(param0) == Potions.EMPTY ? 16253176 : getColor(getMobEffects(param0));
        }
    }

    public static int getColor(Potion param0) {
        return param0 == Potions.EMPTY ? 16253176 : getColor(param0.getEffects());
    }

    public static int getColor(Collection<MobEffectInstance> param0) {
        int var0 = 3694022;
        if (param0.isEmpty()) {
            return 3694022;
        } else {
            float var1 = 0.0F;
            float var2 = 0.0F;
            float var3 = 0.0F;
            int var4 = 0;

            for(MobEffectInstance var5 : param0) {
                if (var5.isVisible()) {
                    int var6 = var5.getEffect().getColor();
                    int var7 = var5.getAmplifier() + 1;
                    var1 += (float)(var7 * (var6 >> 16 & 0xFF)) / 255.0F;
                    var2 += (float)(var7 * (var6 >> 8 & 0xFF)) / 255.0F;
                    var3 += (float)(var7 * (var6 >> 0 & 0xFF)) / 255.0F;
                    var4 += var7;
                }
            }

            if (var4 == 0) {
                return 0;
            } else {
                var1 = var1 / (float)var4 * 255.0F;
                var2 = var2 / (float)var4 * 255.0F;
                var3 = var3 / (float)var4 * 255.0F;
                return (int)var1 << 16 | (int)var2 << 8 | (int)var3;
            }
        }
    }

    public static Potion getPotion(ItemStack param0) {
        return getPotion(param0.getTag());
    }

    public static Potion getPotion(@Nullable CompoundTag param0) {
        return param0 == null ? Potions.EMPTY : Potion.byName(param0.getString("Potion"));
    }

    public static ItemStack setPotion(ItemStack param0, Potion param1) {
        ResourceLocation var0 = BuiltInRegistries.POTION.getKey(param1);
        if (param1 == Potions.EMPTY) {
            param0.removeTagKey("Potion");
        } else {
            param0.getOrCreateTag().putString("Potion", var0.toString());
        }

        return param0;
    }

    public static ItemStack setCustomEffects(ItemStack param0, Collection<MobEffectInstance> param1) {
        if (param1.isEmpty()) {
            return param0;
        } else {
            CompoundTag var0 = param0.getOrCreateTag();
            ListTag var1 = var0.getList("CustomPotionEffects", 9);

            for(MobEffectInstance var2 : param1) {
                var1.add(var2.save(new CompoundTag()));
            }

            var0.put("CustomPotionEffects", var1);
            return param0;
        }
    }

    public static void addPotionTooltip(ItemStack param0, List<Component> param1, float param2) {
        addPotionTooltip(getMobEffects(param0), param1, param2);
    }

    public static void addPotionTooltip(List<MobEffectInstance> param0, List<Component> param1, float param2) {
        List<Pair<Attribute, AttributeModifier>> var0 = Lists.newArrayList();
        if (param0.isEmpty()) {
            param1.add(NO_EFFECT);
        } else {
            for(MobEffectInstance var1 : param0) {
                MutableComponent var2 = Component.translatable(var1.getDescriptionId());
                MobEffect var3 = var1.getEffect();
                Map<Attribute, AttributeModifier> var4 = var3.getAttributeModifiers();
                if (!var4.isEmpty()) {
                    for(Entry<Attribute, AttributeModifier> var5 : var4.entrySet()) {
                        AttributeModifier var6 = var5.getValue();
                        AttributeModifier var7 = new AttributeModifier(
                            var6.getName(), var3.getAttributeModifierValue(var1.getAmplifier(), var6), var6.getOperation()
                        );
                        var0.add(new Pair<>(var5.getKey(), var7));
                    }
                }

                if (var1.getAmplifier() > 0) {
                    var2 = Component.translatable("potion.withAmplifier", var2, Component.translatable("potion.potency." + var1.getAmplifier()));
                }

                if (!var1.endsWithin(20)) {
                    var2 = Component.translatable("potion.withDuration", var2, MobEffectUtil.formatDuration(var1, param2));
                }

                param1.add(var2.withStyle(var3.getCategory().getTooltipFormatting()));
            }
        }

        if (!var0.isEmpty()) {
            param1.add(CommonComponents.EMPTY);
            param1.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for(Pair<Attribute, AttributeModifier> var8 : var0) {
                AttributeModifier var9 = var8.getSecond();
                double var10 = var9.getAmount();
                double var12;
                if (var9.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && var9.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    var12 = var9.getAmount();
                } else {
                    var12 = var9.getAmount() * 100.0;
                }

                if (var10 > 0.0) {
                    param1.add(
                        Component.translatable(
                                "attribute.modifier.plus." + var9.getOperation().toValue(),
                                ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(var12),
                                Component.translatable(var8.getFirst().getDescriptionId())
                            )
                            .withStyle(ChatFormatting.BLUE)
                    );
                } else if (var10 < 0.0) {
                    var12 *= -1.0;
                    param1.add(
                        Component.translatable(
                                "attribute.modifier.take." + var9.getOperation().toValue(),
                                ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(var12),
                                Component.translatable(var8.getFirst().getDescriptionId())
                            )
                            .withStyle(ChatFormatting.RED)
                    );
                }
            }
        }

    }
}
