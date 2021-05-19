package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class PlaySoundCommand {
    private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType(new TranslatableComponent("commands.playsound.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> var0 = Commands.argument("sound", ResourceLocationArgument.id())
            .suggests(SuggestionProviders.AVAILABLE_SOUNDS);

        for(SoundSource var1 : SoundSource.values()) {
            var0.then(source(var1));
        }

        param0.register(Commands.literal("playsound").requires(param0x -> param0x.hasPermission(2)).then(var0));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> source(SoundSource param0) {
        return Commands.literal(param0.getName())
            .then(
                Commands.argument("targets", EntityArgument.players())
                    .executes(
                        param1 -> playSound(
                                param1.getSource(),
                                EntityArgument.getPlayers(param1, "targets"),
                                ResourceLocationArgument.getId(param1, "sound"),
                                param0,
                                param1.getSource().getPosition(),
                                1.0F,
                                1.0F,
                                0.0F
                            )
                    )
                    .then(
                        Commands.argument("pos", Vec3Argument.vec3())
                            .executes(
                                param1 -> playSound(
                                        param1.getSource(),
                                        EntityArgument.getPlayers(param1, "targets"),
                                        ResourceLocationArgument.getId(param1, "sound"),
                                        param0,
                                        Vec3Argument.getVec3(param1, "pos"),
                                        1.0F,
                                        1.0F,
                                        0.0F
                                    )
                            )
                            .then(
                                Commands.argument("volume", FloatArgumentType.floatArg(0.0F))
                                    .executes(
                                        param1 -> playSound(
                                                param1.getSource(),
                                                EntityArgument.getPlayers(param1, "targets"),
                                                ResourceLocationArgument.getId(param1, "sound"),
                                                param0,
                                                Vec3Argument.getVec3(param1, "pos"),
                                                param1.getArgument("volume", Float.class),
                                                1.0F,
                                                0.0F
                                            )
                                    )
                                    .then(
                                        Commands.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F))
                                            .executes(
                                                param1 -> playSound(
                                                        param1.getSource(),
                                                        EntityArgument.getPlayers(param1, "targets"),
                                                        ResourceLocationArgument.getId(param1, "sound"),
                                                        param0,
                                                        Vec3Argument.getVec3(param1, "pos"),
                                                        param1.getArgument("volume", Float.class),
                                                        param1.getArgument("pitch", Float.class),
                                                        0.0F
                                                    )
                                            )
                                            .then(
                                                Commands.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F))
                                                    .executes(
                                                        param1 -> playSound(
                                                                param1.getSource(),
                                                                EntityArgument.getPlayers(param1, "targets"),
                                                                ResourceLocationArgument.getId(param1, "sound"),
                                                                param0,
                                                                Vec3Argument.getVec3(param1, "pos"),
                                                                param1.getArgument("volume", Float.class),
                                                                param1.getArgument("pitch", Float.class),
                                                                param1.getArgument("minVolume", Float.class)
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
            );
    }

    private static int playSound(
        CommandSourceStack param0,
        Collection<ServerPlayer> param1,
        ResourceLocation param2,
        SoundSource param3,
        Vec3 param4,
        float param5,
        float param6,
        float param7
    ) throws CommandSyntaxException {
        double var0 = Math.pow(param5 > 1.0F ? (double)(param5 * 16.0F) : 16.0, 2.0);
        int var1 = 0;
        Iterator var11 = param1.iterator();

        while(true) {
            ServerPlayer var2;
            Vec3 var7;
            float var8;
            while(true) {
                if (!var11.hasNext()) {
                    if (var1 == 0) {
                        throw ERROR_TOO_FAR.create();
                    }

                    if (param1.size() == 1) {
                        param0.sendSuccess(
                            new TranslatableComponent("commands.playsound.success.single", param2, param1.iterator().next().getDisplayName()), true
                        );
                    } else {
                        param0.sendSuccess(new TranslatableComponent("commands.playsound.success.multiple", param2, param1.size()), true);
                    }

                    return var1;
                }

                var2 = (ServerPlayer)var11.next();
                double var3 = param4.x - var2.getX();
                double var4 = param4.y - var2.getY();
                double var5 = param4.z - var2.getZ();
                double var6 = var3 * var3 + var4 * var4 + var5 * var5;
                var7 = param4;
                var8 = param5;
                if (!(var6 > var0)) {
                    break;
                }

                if (!(param7 <= 0.0F)) {
                    double var9 = Math.sqrt(var6);
                    var7 = new Vec3(var2.getX() + var3 / var9 * 2.0, var2.getY() + var4 / var9 * 2.0, var2.getZ() + var5 / var9 * 2.0);
                    var8 = param7;
                    break;
                }
            }

            var2.connection.send(new ClientboundCustomSoundPacket(param2, param3, var7, var8, param6));
            ++var1;
        }
    }
}
