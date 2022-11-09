package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SummonCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed"));
    private static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed.uuid"));
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.summon.invalidPosition"));

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        param0.register(
            Commands.literal("summon")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("entity", ResourceArgument.resource(param1, Registries.ENTITY_TYPE))
                        .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                        .executes(
                            param0x -> spawnEntity(
                                    param0x.getSource(),
                                    ResourceArgument.getSummonableEntityType(param0x, "entity"),
                                    param0x.getSource().getPosition(),
                                    new CompoundTag(),
                                    true
                                )
                        )
                        .then(
                            Commands.argument("pos", Vec3Argument.vec3())
                                .executes(
                                    param0x -> spawnEntity(
                                            param0x.getSource(),
                                            ResourceArgument.getSummonableEntityType(param0x, "entity"),
                                            Vec3Argument.getVec3(param0x, "pos"),
                                            new CompoundTag(),
                                            true
                                        )
                                )
                                .then(
                                    Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                        .executes(
                                            param0x -> spawnEntity(
                                                    param0x.getSource(),
                                                    ResourceArgument.getSummonableEntityType(param0x, "entity"),
                                                    Vec3Argument.getVec3(param0x, "pos"),
                                                    CompoundTagArgument.getCompoundTag(param0x, "nbt"),
                                                    false
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int spawnEntity(CommandSourceStack param0, Holder.Reference<EntityType<?>> param1, Vec3 param2, CompoundTag param3, boolean param4) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param2);
        if (!Level.isInSpawnableBounds(var0)) {
            throw INVALID_POSITION.create();
        } else {
            CompoundTag var1 = param3.copy();
            var1.putString("id", param1.key().location().toString());
            ServerLevel var2 = param0.getLevel();
            Entity var3 = EntityType.loadEntityRecursive(var1, var2, param1x -> {
                param1x.moveTo(param2.x, param2.y, param2.z, param1x.getYRot(), param1x.getXRot());
                return param1x;
            });
            if (var3 == null) {
                throw ERROR_FAILED.create();
            } else {
                if (param4 && var3 instanceof Mob) {
                    ((Mob)var3)
                        .finalizeSpawn(param0.getLevel(), param0.getLevel().getCurrentDifficultyAt(var3.blockPosition()), MobSpawnType.COMMAND, null, null);
                }

                if (!var2.tryAddFreshEntityWithPassengers(var3)) {
                    throw ERROR_DUPLICATE_UUID.create();
                } else {
                    param0.sendSuccess(Component.translatable("commands.summon.success", var3.getDisplayName()), true);
                    return 1;
                }
            }
        }
    }
}
