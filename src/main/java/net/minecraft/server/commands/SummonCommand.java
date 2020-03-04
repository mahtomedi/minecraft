package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SummonCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.summon.failed"));
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.summon.invalidPosition")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("summon")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("entity", EntitySummonArgument.id())
                        .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                        .executes(
                            param0x -> spawnEntity(
                                    param0x.getSource(),
                                    EntitySummonArgument.getSummonableEntity(param0x, "entity"),
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
                                            EntitySummonArgument.getSummonableEntity(param0x, "entity"),
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
                                                    EntitySummonArgument.getSummonableEntity(param0x, "entity"),
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

    private static int spawnEntity(CommandSourceStack param0, ResourceLocation param1, Vec3 param2, CompoundTag param3, boolean param4) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param2);
        if (!Level.isInWorldBoundsHorizontal(var0)) {
            throw INVALID_POSITION.create();
        } else {
            CompoundTag var1 = param3.copy();
            var1.putString("id", param1.toString());
            if (EntityType.getKey(EntityType.LIGHTNING_BOLT).equals(param1)) {
                LightningBolt var2 = new LightningBolt(param0.getLevel(), param2.x, param2.y, param2.z, false);
                param0.getLevel().addGlobalEntity(var2);
                param0.sendSuccess(new TranslatableComponent("commands.summon.success", var2.getDisplayName()), true);
                return 1;
            } else {
                ServerLevel var3 = param0.getLevel();
                Entity var4 = EntityType.loadEntityRecursive(var1, var3, param2x -> {
                    param2x.moveTo(param2.x, param2.y, param2.z, param2x.yRot, param2x.xRot);
                    return !var3.addWithUUID(param2x) ? null : param2x;
                });
                if (var4 == null) {
                    throw ERROR_FAILED.create();
                } else {
                    if (param4 && var4 instanceof Mob) {
                        ((Mob)var4)
                            .finalizeSpawn(param0.getLevel(), param0.getLevel().getCurrentDifficultyAt(var4.blockPosition()), MobSpawnType.COMMAND, null, null);
                    }

                    param0.sendSuccess(new TranslatableComponent("commands.summon.success", var4.getDisplayName()), true);
                    return 1;
                }
            }
        }
    }
}
