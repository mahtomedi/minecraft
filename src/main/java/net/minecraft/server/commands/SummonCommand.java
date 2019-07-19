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
import net.minecraft.world.phys.Vec3;

public class SummonCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.summon.failed"));

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
        CompoundTag var0 = param3.copy();
        var0.putString("id", param1.toString());
        if (EntityType.getKey(EntityType.LIGHTNING_BOLT).equals(param1)) {
            LightningBolt var1 = new LightningBolt(param0.getLevel(), param2.x, param2.y, param2.z, false);
            param0.getLevel().addGlobalEntity(var1);
            param0.sendSuccess(new TranslatableComponent("commands.summon.success", var1.getDisplayName()), true);
            return 1;
        } else {
            ServerLevel var2 = param0.getLevel();
            Entity var3 = EntityType.loadEntityRecursive(var0, var2, param2x -> {
                param2x.moveTo(param2.x, param2.y, param2.z, param2x.yRot, param2x.xRot);
                return !var2.addWithUUID(param2x) ? null : param2x;
            });
            if (var3 == null) {
                throw ERROR_FAILED.create();
            } else {
                if (param4 && var3 instanceof Mob) {
                    ((Mob)var3)
                        .finalizeSpawn(param0.getLevel(), param0.getLevel().getCurrentDifficultyAt(new BlockPos(var3)), MobSpawnType.COMMAND, null, null);
                }

                param0.sendSuccess(new TranslatableComponent("commands.summon.success", var3.getDisplayName()), true);
                return 1;
            }
        }
    }
}
