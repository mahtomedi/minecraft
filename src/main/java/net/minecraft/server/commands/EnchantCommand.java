package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.enchant.failed.entity", param0)
    );
    private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.enchant.failed.itemless", param0)
    );
    private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.enchant.failed.incompatible", param0)
    );
    private static final Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("commands.enchant.failed.level", param0, param1)
    );
    private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType(Component.translatable("commands.enchant.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        param0.register(
            Commands.literal("enchant")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("targets", EntityArgument.entities())
                        .then(
                            Commands.argument("enchantment", ResourceArgument.resource(param1, Registry.ENCHANTMENT_REGISTRY))
                                .executes(
                                    param0x -> enchant(
                                            param0x.getSource(),
                                            EntityArgument.getEntities(param0x, "targets"),
                                            ResourceArgument.getEnchantment(param0x, "enchantment"),
                                            1
                                        )
                                )
                                .then(
                                    Commands.argument("level", IntegerArgumentType.integer(0))
                                        .executes(
                                            param0x -> enchant(
                                                    param0x.getSource(),
                                                    EntityArgument.getEntities(param0x, "targets"),
                                                    ResourceArgument.getEnchantment(param0x, "enchantment"),
                                                    IntegerArgumentType.getInteger(param0x, "level")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int enchant(CommandSourceStack param0, Collection<? extends Entity> param1, Holder<Enchantment> param2, int param3) throws CommandSyntaxException {
        Enchantment var0 = param2.value();
        if (param3 > var0.getMaxLevel()) {
            throw ERROR_LEVEL_TOO_HIGH.create(param3, var0.getMaxLevel());
        } else {
            int var1 = 0;

            for(Entity var2 : param1) {
                if (var2 instanceof LivingEntity var3) {
                    ItemStack var4 = var3.getMainHandItem();
                    if (!var4.isEmpty()) {
                        if (var0.canEnchant(var4) && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(var4).keySet(), var0)) {
                            var4.enchant(var0, param3);
                            ++var1;
                        } else if (param1.size() == 1) {
                            throw ERROR_INCOMPATIBLE.create(var4.getItem().getName(var4).getString());
                        }
                    } else if (param1.size() == 1) {
                        throw ERROR_NO_ITEM.create(var3.getName().getString());
                    }
                } else if (param1.size() == 1) {
                    throw ERROR_NOT_LIVING_ENTITY.create(var2.getName().getString());
                }
            }

            if (var1 == 0) {
                throw ERROR_NOTHING_HAPPENED.create();
            } else {
                if (param1.size() == 1) {
                    param0.sendSuccess(
                        Component.translatable("commands.enchant.success.single", var0.getFullname(param3), param1.iterator().next().getDisplayName()), true
                    );
                } else {
                    param0.sendSuccess(Component.translatable("commands.enchant.success.multiple", var0.getFullname(param3), param1.size()), true);
                }

                return var1;
            }
        }
    }
}
