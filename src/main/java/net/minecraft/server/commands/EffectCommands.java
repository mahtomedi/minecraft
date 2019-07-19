package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MobEffectArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EffectCommands {
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.effect.give.failed"));
    private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.effect.clear.everything.failed")
    );
    private static final SimpleCommandExceptionType ERROR_CLEAR_SPECIFIC_FAILED = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.effect.clear.specific.failed")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("effect")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("clear")
                        .then(
                            Commands.argument("targets", EntityArgument.entities())
                                .executes(param0x -> clearEffects(param0x.getSource(), EntityArgument.getEntities(param0x, "targets")))
                                .then(
                                    Commands.argument("effect", MobEffectArgument.effect())
                                        .executes(
                                            param0x -> clearEffect(
                                                    param0x.getSource(),
                                                    EntityArgument.getEntities(param0x, "targets"),
                                                    MobEffectArgument.getEffect(param0x, "effect")
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
                                    Commands.argument("effect", MobEffectArgument.effect())
                                        .executes(
                                            param0x -> giveEffect(
                                                    param0x.getSource(),
                                                    EntityArgument.getEntities(param0x, "targets"),
                                                    MobEffectArgument.getEffect(param0x, "effect"),
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
                                                            MobEffectArgument.getEffect(param0x, "effect"),
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
                                                                    MobEffectArgument.getEffect(param0x, "effect"),
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
                                                                            MobEffectArgument.getEffect(param0x, "effect"),
                                                                            IntegerArgumentType.getInteger(param0x, "seconds"),
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
        CommandSourceStack param0, Collection<? extends Entity> param1, MobEffect param2, @Nullable Integer param3, int param4, boolean param5
    ) throws CommandSyntaxException {
        int var0 = 0;
        int var1;
        if (param3 != null) {
            if (param2.isInstantenous()) {
                var1 = param3;
            } else {
                var1 = param3 * 20;
            }
        } else if (param2.isInstantenous()) {
            var1 = 1;
        } else {
            var1 = 600;
        }

        for(Entity var5 : param1) {
            if (var5 instanceof LivingEntity) {
                MobEffectInstance var6 = new MobEffectInstance(param2, var1, param4, false, param5);
                if (((LivingEntity)var5).addEffect(var6)) {
                    ++var0;
                }
            }
        }

        if (var0 == 0) {
            throw ERROR_GIVE_FAILED.create();
        } else {
            if (param1.size() == 1) {
                param0.sendSuccess(
                    new TranslatableComponent(
                        "commands.effect.give.success.single", param2.getDisplayName(), param1.iterator().next().getDisplayName(), var1 / 20
                    ),
                    true
                );
            } else {
                param0.sendSuccess(new TranslatableComponent("commands.effect.give.success.multiple", param2.getDisplayName(), param1.size(), var1 / 20), true);
            }

            return var0;
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
                param0.sendSuccess(
                    new TranslatableComponent("commands.effect.clear.everything.success.single", param1.iterator().next().getDisplayName()), true
                );
            } else {
                param0.sendSuccess(new TranslatableComponent("commands.effect.clear.everything.success.multiple", param1.size()), true);
            }

            return var0;
        }
    }

    private static int clearEffect(CommandSourceStack param0, Collection<? extends Entity> param1, MobEffect param2) throws CommandSyntaxException {
        int var0 = 0;

        for(Entity var1 : param1) {
            if (var1 instanceof LivingEntity && ((LivingEntity)var1).removeEffect(param2)) {
                ++var0;
            }
        }

        if (var0 == 0) {
            throw ERROR_CLEAR_SPECIFIC_FAILED.create();
        } else {
            if (param1.size() == 1) {
                param0.sendSuccess(
                    new TranslatableComponent(
                        "commands.effect.clear.specific.success.single", param2.getDisplayName(), param1.iterator().next().getDisplayName()
                    ),
                    true
                );
            } else {
                param0.sendSuccess(new TranslatableComponent("commands.effect.clear.specific.success.multiple", param2.getDisplayName(), param1.size()), true);
            }

            return var0;
        }
    }
}
