package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EffectCommands {
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.effect.give.failed"));
    private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(
        Component.translatable("commands.effect.clear.everything.failed")
    );
    private static final SimpleCommandExceptionType ERROR_CLEAR_SPECIFIC_FAILED = new SimpleCommandExceptionType(
        Component.translatable("commands.effect.clear.specific.failed")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        param0.register(
            Commands.literal("effect")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("clear")
                        .executes(param0x -> clearEffects(param0x.getSource(), ImmutableList.of(param0x.getSource().getEntityOrException())))
                        .then(
                            Commands.argument("targets", EntityArgument.entities())
                                .executes(param0x -> clearEffects(param0x.getSource(), EntityArgument.getEntities(param0x, "targets")))
                                .then(
                                    Commands.argument("effect", ResourceArgument.resource(param1, Registries.MOB_EFFECT))
                                        .executes(
                                            param0x -> clearEffect(
                                                    param0x.getSource(),
                                                    EntityArgument.getEntities(param0x, "targets"),
                                                    ResourceArgument.getMobEffect(param0x, "effect")
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("give")
                        .then(
                            Commands.argument("targets", EntityArgument.entities())
                                .then(
                                    Commands.argument("effect", ResourceArgument.resource(param1, Registries.MOB_EFFECT))
                                        .executes(
                                            param0x -> giveEffect(
                                                    param0x.getSource(),
                                                    EntityArgument.getEntities(param0x, "targets"),
                                                    ResourceArgument.getMobEffect(param0x, "effect"),
                                                    null,
                                                    0,
                                                    true
                                                )
                                        )
                                        .then(
                                            Commands.argument("seconds", IntegerArgumentType.integer(1, 1000000))
                                                .executes(
                                                    param0x -> giveEffect(
                                                            param0x.getSource(),
                                                            EntityArgument.getEntities(param0x, "targets"),
                                                            ResourceArgument.getMobEffect(param0x, "effect"),
                                                            IntegerArgumentType.getInteger(param0x, "seconds"),
                                                            0,
                                                            true
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
                                                        .executes(
                                                            param0x -> giveEffect(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getEntities(param0x, "targets"),
                                                                    ResourceArgument.getMobEffect(param0x, "effect"),
                                                                    IntegerArgumentType.getInteger(param0x, "seconds"),
                                                                    IntegerArgumentType.getInteger(param0x, "amplifier"),
                                                                    true
                                                                )
                                                        )
                                                        .then(
                                                            Commands.argument("hideParticles", BoolArgumentType.bool())
                                                                .executes(
                                                                    param0x -> giveEffect(
                                                                            param0x.getSource(),
                                                                            EntityArgument.getEntities(param0x, "targets"),
                                                                            ResourceArgument.getMobEffect(param0x, "effect"),
                                                                            IntegerArgumentType.getInteger(param0x, "seconds"),
                                                                            IntegerArgumentType.getInteger(param0x, "amplifier"),
                                                                            !BoolArgumentType.getBool(param0x, "hideParticles")
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("infinite")
                                                .executes(
                                                    param0x -> giveEffect(
                                                            param0x.getSource(),
                                                            EntityArgument.getEntities(param0x, "targets"),
                                                            ResourceArgument.getMobEffect(param0x, "effect"),
                                                            -1,
                                                            0,
                                                            true
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
                                                        .executes(
                                                            param0x -> giveEffect(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getEntities(param0x, "targets"),
                                                                    ResourceArgument.getMobEffect(param0x, "effect"),
                                                                    -1,
                                                                    IntegerArgumentType.getInteger(param0x, "amplifier"),
                                                                    true
                                                                )
                                                        )
                                                        .then(
                                                            Commands.argument("hideParticles", BoolArgumentType.bool())
                                                                .executes(
                                                                    param0x -> giveEffect(
                                                                            param0x.getSource(),
                                                                            EntityArgument.getEntities(param0x, "targets"),
                                                                            ResourceArgument.getMobEffect(param0x, "effect"),
                                                                            -1,
                                                                            IntegerArgumentType.getInteger(param0x, "amplifier"),
                                                                            !BoolArgumentType.getBool(param0x, "hideParticles")
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int giveEffect(
        CommandSourceStack param0, Collection<? extends Entity> param1, Holder<MobEffect> param2, @Nullable Integer param3, int param4, boolean param5
    ) throws CommandSyntaxException {
        MobEffect var0 = param2.value();
        int var1 = 0;
        int var2;
        if (param3 != null) {
            if (var0.isInstantenous()) {
                var2 = param3;
            } else if (param3 == -1) {
                var2 = -1;
            } else {
                var2 = param3 * 20;
            }
        } else if (var0.isInstantenous()) {
            var2 = 1;
        } else {
            var2 = 600;
        }

        for(Entity var7 : param1) {
            if (var7 instanceof LivingEntity) {
                MobEffectInstance var8 = new MobEffectInstance(var0, var2, param4, false, param5);
                if (((LivingEntity)var7).addEffect(var8, param0.getEntity())) {
                    ++var1;
                }
            }
        }

        if (var1 == 0) {
            throw ERROR_GIVE_FAILED.create();
        } else {
            if (param1.size() == 1) {
                param0.sendSuccess(
                    Component.translatable("commands.effect.give.success.single", var0.getDisplayName(), param1.iterator().next().getDisplayName(), var2 / 20),
                    true
                );
            } else {
                param0.sendSuccess(Component.translatable("commands.effect.give.success.multiple", var0.getDisplayName(), param1.size(), var2 / 20), true);
            }

            return var1;
        }
    }

    private static int clearEffects(CommandSourceStack param0, Collection<? extends Entity> param1) throws CommandSyntaxException {
        int var0 = 0;

        for(Entity var1 : param1) {
            if (var1 instanceof LivingEntity && ((LivingEntity)var1).removeAllEffects()) {
                ++var0;
            }
        }

        if (var0 == 0) {
            throw ERROR_CLEAR_EVERYTHING_FAILED.create();
        } else {
            if (param1.size() == 1) {
                param0.sendSuccess(Component.translatable("commands.effect.clear.everything.success.single", param1.iterator().next().getDisplayName()), true);
            } else {
                param0.sendSuccess(Component.translatable("commands.effect.clear.everything.success.multiple", param1.size()), true);
            }

            return var0;
        }
    }

    private static int clearEffect(CommandSourceStack param0, Collection<? extends Entity> param1, Holder<MobEffect> param2) throws CommandSyntaxException {
        MobEffect var0 = param2.value();
        int var1 = 0;

        for(Entity var2 : param1) {
            if (var2 instanceof LivingEntity && ((LivingEntity)var2).removeEffect(var0)) {
                ++var1;
            }
        }

        if (var1 == 0) {
            throw ERROR_CLEAR_SPECIFIC_FAILED.create();
        } else {
            if (param1.size() == 1) {
                param0.sendSuccess(
                    Component.translatable("commands.effect.clear.specific.success.single", var0.getDisplayName(), param1.iterator().next().getDisplayName()),
                    true
                );
            } else {
                param0.sendSuccess(Component.translatable("commands.effect.clear.specific.success.multiple", var0.getDisplayName(), param1.size()), true);
            }

            return var1;
        }
    }
}
