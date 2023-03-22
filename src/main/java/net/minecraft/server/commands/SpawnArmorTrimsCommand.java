package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.Level;

public class SpawnArmorTrimsCommand {
    private static final Map<Pair<ArmorMaterial, EquipmentSlot>, Item> MATERIAL_AND_SLOT_TO_ITEM = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.HEAD), Items.CHAINMAIL_HELMET);
        param0.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.CHEST), Items.CHAINMAIL_CHESTPLATE);
        param0.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.LEGS), Items.CHAINMAIL_LEGGINGS);
        param0.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.FEET), Items.CHAINMAIL_BOOTS);
        param0.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.HEAD), Items.IRON_HELMET);
        param0.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.CHEST), Items.IRON_CHESTPLATE);
        param0.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.LEGS), Items.IRON_LEGGINGS);
        param0.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.FEET), Items.IRON_BOOTS);
        param0.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.HEAD), Items.GOLDEN_HELMET);
        param0.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.CHEST), Items.GOLDEN_CHESTPLATE);
        param0.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.LEGS), Items.GOLDEN_LEGGINGS);
        param0.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.FEET), Items.GOLDEN_BOOTS);
        param0.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.HEAD), Items.NETHERITE_HELMET);
        param0.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.CHEST), Items.NETHERITE_CHESTPLATE);
        param0.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.LEGS), Items.NETHERITE_LEGGINGS);
        param0.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.FEET), Items.NETHERITE_BOOTS);
        param0.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.HEAD), Items.DIAMOND_HELMET);
        param0.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.CHEST), Items.DIAMOND_CHESTPLATE);
        param0.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.LEGS), Items.DIAMOND_LEGGINGS);
        param0.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.FEET), Items.DIAMOND_BOOTS);
        param0.put(Pair.of(ArmorMaterials.TURTLE, EquipmentSlot.HEAD), Items.TURTLE_HELMET);
    });
    private static final List<ResourceKey<TrimPattern>> VANILLA_TRIM_PATTERNS = List.of(
        TrimPatterns.SENTRY,
        TrimPatterns.DUNE,
        TrimPatterns.COAST,
        TrimPatterns.WILD,
        TrimPatterns.WARD,
        TrimPatterns.EYE,
        TrimPatterns.VEX,
        TrimPatterns.TIDE,
        TrimPatterns.SNOUT,
        TrimPatterns.RIB,
        TrimPatterns.SPIRE,
        TrimPatterns.WAYFINDER,
        TrimPatterns.SHAPER,
        TrimPatterns.SILENCE,
        TrimPatterns.RAISER,
        TrimPatterns.HOST
    );
    private static final List<ResourceKey<TrimMaterial>> VANILLA_TRIM_MATERIALS = List.of(
        TrimMaterials.QUARTZ,
        TrimMaterials.IRON,
        TrimMaterials.NETHERITE,
        TrimMaterials.REDSTONE,
        TrimMaterials.COPPER,
        TrimMaterials.GOLD,
        TrimMaterials.EMERALD,
        TrimMaterials.DIAMOND,
        TrimMaterials.LAPIS,
        TrimMaterials.AMETHYST
    );
    private static final ToIntFunction<ResourceKey<TrimPattern>> TRIM_PATTERN_ORDER = Util.createIndexLookup(VANILLA_TRIM_PATTERNS);
    private static final ToIntFunction<ResourceKey<TrimMaterial>> TRIM_MATERIAL_ORDER = Util.createIndexLookup(VANILLA_TRIM_MATERIALS);

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("spawn_armor_trims")
                .requires(param0x -> param0x.hasPermission(2))
                .executes(param0x -> spawnArmorTrims(param0x.getSource(), param0x.getSource().getPlayerOrException()))
        );
    }

    private static int spawnArmorTrims(CommandSourceStack param0, Player param1) {
        Level var0 = param1.getLevel();
        NonNullList<ArmorTrim> var1 = NonNullList.create();
        Registry<TrimPattern> var2 = var0.registryAccess().registryOrThrow(Registries.TRIM_PATTERN);
        Registry<TrimMaterial> var3 = var0.registryAccess().registryOrThrow(Registries.TRIM_MATERIAL);
        var2.stream()
            .sorted(Comparator.comparing(param1x -> TRIM_PATTERN_ORDER.applyAsInt(var2.getResourceKey(param1x).orElse(null))))
            .forEachOrdered(
                param3 -> var3.stream()
                        .sorted(Comparator.comparing(param1x -> TRIM_MATERIAL_ORDER.applyAsInt(var3.getResourceKey(param1x).orElse(null))))
                        .forEachOrdered(param4 -> var1.add(new ArmorTrim(var3.wrapAsHolder(param4), var2.wrapAsHolder(param3))))
            );
        BlockPos var4 = param1.blockPosition().relative(param1.getDirection(), 5);
        int var5 = ArmorMaterials.values().length - 1;
        double var6 = 3.0;
        int var7 = 0;
        int var8 = 0;

        for(ArmorTrim var9 : var1) {
            for(ArmorMaterial var10 : ArmorMaterials.values()) {
                if (var10 != ArmorMaterials.LEATHER) {
                    double var11 = (double)var4.getX() + 0.5 - (double)(var7 % var3.size()) * 3.0;
                    double var12 = (double)var4.getY() + 0.5 + (double)(var8 % var5) * 3.0;
                    double var13 = (double)var4.getZ() + 0.5 + (double)(var7 / var3.size() * 10);
                    ArmorStand var14 = new ArmorStand(var0, var11, var12, var13);
                    var14.setYRot(180.0F);
                    var14.setNoGravity(true);

                    for(EquipmentSlot var15 : EquipmentSlot.values()) {
                        Item var16 = MATERIAL_AND_SLOT_TO_ITEM.get(Pair.of(var10, var15));
                        if (var16 != null) {
                            ItemStack var17 = new ItemStack(var16);
                            ArmorTrim.setTrim(var0.registryAccess(), var17, var9);
                            var14.setItemSlot(var15, var17);
                            if (var16 instanceof ArmorItem var18 && var18.getMaterial() == ArmorMaterials.TURTLE) {
                                var14.setCustomName(
                                    var9.pattern().value().copyWithStyle(var9.material()).copy().append(" ").append(var9.material().value().description())
                                );
                                var14.setCustomNameVisible(true);
                                continue;
                            }

                            var14.setInvisible(true);
                        }
                    }

                    var0.addFreshEntity(var14);
                    ++var8;
                }
            }

            ++var7;
        }

        param0.sendSuccess(Component.literal("Armorstands with trimmed armor spawned around you"), true);
        return 1;
    }
}
