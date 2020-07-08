package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionUtils {
    private static final MutableComponent NO_EFFECT = new TranslatableComponent("effect.none").withStyle(ChatFormatting.GRAY);

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
        ResourceLocation var0 = Registry.POTION.getKey(param1);
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

    @OnlyIn(Dist.CLIENT)
    public static void addPotionTooltip(ItemStack param0, List<Component> param1, float param2) {
        List<MobEffectInstance> var0 = getMobEffects(param0);
        List<Pair<Attribute, AttributeModifier>> var1 = Lists.newArrayList();
        if (var0.isEmpty()) {
            param1.add(NO_EFFECT);
        } else {
            for(MobEffectInstance var2 : var0) {
                MutableComponent var3 = new TranslatableComponent(var2.getDescriptionId());
                MobEffect var4 = var2.getEffect();
                Map<Attribute, AttributeModifier> var5 = var4.getAttributeModifiers();
                if (!var5.isEmpty()) {
                    for(Entry<Attribute, AttributeModifier> var6 : var5.entrySet()) {
                        AttributeModifier var7 = var6.getValue();
                        AttributeModifier var8 = new AttributeModifier(
                            var7.getName(), var4.getAttributeModifierValue(var2.getAmplifier(), var7), var7.getOperation()
                        );
                        var1.add(new Pair<>(var6.getKey(), var8));
                    }
                }

                if (var2.getAmplifier() > 0) {
                    var3 = new TranslatableComponent("potion.withAmplifier", var3, new TranslatableComponent("potion.potency." + var2.getAmplifier()));
                }

                if (var2.getDuration() > 20) {
                    var3 = new TranslatableComponent("potion.withDuration", var3, MobEffectUtil.formatDuration(var2, param2));
                }

                param1.add(var3.withStyle(var4.getCategory().getTooltipFormatting()));
            }
        }

        if (!var1.isEmpty()) {
            param1.add(TextComponent.EMPTY);
            param1.add(new TranslatableComponent("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for(Pair<Attribute, AttributeModifier> var9 : var1) {
                AttributeModifier var10 = var9.getSecond();
                double var11 = var10.getAmount();
                double var13;
                if (var10.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && var10.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    var13 = var10.getAmount();
                } else {
                    var13 = var10.getAmount() * 100.0;
                }

                if (var11 > 0.0) {
                    param1.add(
                        new TranslatableComponent(
                                "attribute.modifier.plus." + var10.getOperation().toValue(),
                                ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(var13),
                                new TranslatableComponent(var9.getFirst().getDescriptionId())
                            )
                            .withStyle(ChatFormatting.BLUE)
                    );
                } else if (var11 < 0.0) {
                    var13 *= -1.0;
                    param1.add(
                        new TranslatableComponent(
                                "attribute.modifier.take." + var10.getOperation().toValue(),
                                ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(var13),
                                new TranslatableComponent(var9.getFirst().getDescriptionId())
                            )
                            .withStyle(ChatFormatting.RED)
                    );
                }
            }
        }

    }
}
