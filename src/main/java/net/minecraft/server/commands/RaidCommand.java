package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.phys.Vec3;

public class RaidCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("raid")
                .requires(param0x -> param0x.hasPermission(3))
                .then(
                    Commands.literal("start")
                        .then(
                            Commands.argument("omenlvl", IntegerArgumentType.integer(0))
                                .executes(param0x -> start(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "omenlvl")))
                        )
                )
                .then(Commands.literal("stop").executes(param0x -> stop(param0x.getSource())))
                .then(Commands.literal("check").executes(param0x -> check(param0x.getSource())))
                .then(
                    Commands.literal("sound")
                        .then(
                            Commands.argument("type", ComponentArgument.textComponent())
                                .executes(param0x -> playSound(param0x.getSource(), ComponentArgument.getComponent(param0x, "type")))
                        )
                )
                .then(Commands.literal("spawnleader").executes(param0x -> spawnLeader(param0x.getSource())))
                .then(
                    Commands.literal("setomen")
                        .then(
                            Commands.argument("level", IntegerArgumentType.integer(0))
                                .executes(param0x -> setBadOmenLevel(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "level")))
                        )
                )
                .then(Commands.literal("glow").executes(param0x -> glow(param0x.getSource())))
        );
    }

    private static int glow(CommandSourceStack param0) throws CommandSyntaxException {
        Raid var0 = getRaid(param0.getPlayerOrException());
        if (var0 != null) {
            for(Raider var2 : var0.getAllRaiders()) {
                var2.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1000, 1));
            }
        }

        return 1;
    }

    private static int setBadOmenLevel(CommandSourceStack param0, int param1) throws CommandSyntaxException {
        Raid var0 = getRaid(param0.getPlayerOrException());
        if (var0 != null) {
            int var1 = var0.getMaxBadOmenLevel();
            if (param1 > var1) {
                param0.sendFailure(Component.literal("Sorry, the max bad omen level you can set is " + var1));
            } else {
                int var2 = var0.getBadOmenLevel();
                var0.setBadOmenLevel(param1);
                param0.sendSuccess(Component.literal("Changed village's bad omen level from " + var2 + " to " + param1), false);
            }
        } else {
            param0.sendFailure(Component.literal("No raid found here"));
        }

        return 1;
    }

    private static int spawnLeader(CommandSourceStack param0) {
        param0.sendSuccess(Component.literal("Spawned a raid captain"), false);
        Raider var0 = EntityType.PILLAGER.create(param0.getLevel());
        if (var0 == null) {
            param0.sendFailure(Component.literal("Pillager failed to spawn"));
            return 0;
        } else {
            var0.setPatrolLeader(true);
            var0.setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance());
            var0.setPos(param0.getPosition().x, param0.getPosition().y, param0.getPosition().z);
            var0.finalizeSpawn(
                param0.getLevel(), param0.getLevel().getCurrentDifficultyAt(BlockPos.containing(param0.getPosition())), MobSpawnType.COMMAND, null, null
            );
            param0.getLevel().addFreshEntityWithPassengers(var0);
            return 1;
        }
    }

    private static int playSound(CommandSourceStack param0, @Nullable Component param1) {
        if (param1 != null && param1.getString().equals("local")) {
            ServerLevel var0 = param0.getLevel();
            Vec3 var1 = param0.getPosition().add(5.0, 0.0, 0.0);
            var0.playSeededSound(null, var1.x, var1.y, var1.z, SoundEvents.RAID_HORN, SoundSource.NEUTRAL, 2.0F, 1.0F, var0.random.nextLong());
        }

        return 1;
    }

    private static int start(CommandSourceStack param0, int param1) throws CommandSyntaxException {
        ServerPlayer var0 = param0.getPlayerOrException();
        BlockPos var1 = var0.blockPosition();
        if (var0.serverLevel().isRaided(var1)) {
            param0.sendFailure(Component.literal("Raid already started close by"));
            return -1;
        } else {
            Raids var2 = var0.serverLevel().getRaids();
            Raid var3 = var2.createOrExtendRaid(var0);
            if (var3 != null) {
                var3.setBadOmenLevel(param1);
                var2.setDirty();
                param0.sendSuccess(Component.literal("Created a raid in your local village"), false);
            } else {
                param0.sendFailure(Component.literal("Failed to create a raid in your local village"));
            }

            return 1;
        }
    }

    private static int stop(CommandSourceStack param0) throws CommandSyntaxException {
        ServerPlayer var0 = param0.getPlayerOrException();
        BlockPos var1 = var0.blockPosition();
        Raid var2 = var0.serverLevel().getRaidAt(var1);
        if (var2 != null) {
            var2.stop();
            param0.sendSuccess(Component.literal("Stopped raid"), false);
            return 1;
        } else {
            param0.sendFailure(Component.literal("No raid here"));
            return -1;
        }
    }

    private static int check(CommandSourceStack param0) throws CommandSyntaxException {
        Raid var0 = getRaid(param0.getPlayerOrException());
        if (var0 != null) {
            StringBuilder var1 = new StringBuilder();
            var1.append("Found a started raid! ");
            param0.sendSuccess(Component.literal(var1.toString()), false);
            var1 = new StringBuilder();
            var1.append("Num groups spawned: ");
            var1.append(var0.getGroupsSpawned());
            var1.append(" Bad omen level: ");
            var1.append(var0.getBadOmenLevel());
            var1.append(" Num mobs: ");
            var1.append(var0.getTotalRaidersAlive());
            var1.append(" Raid health: ");
            var1.append(var0.getHealthOfLivingRaiders());
            var1.append(" / ");
            var1.append(var0.getTotalHealth());
            param0.sendSuccess(Component.literal(var1.toString()), false);
            return 1;
        } else {
            param0.sendFailure(Component.literal("Found no started raids"));
            return 0;
        }
    }

    @Nullable
    private static Raid getRaid(ServerPlayer param0) {
        return param0.serverLevel().getRaidAt(param0.blockPosition());
    }
}
