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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PlaySoundCommand {
    private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType(Component.translatable("commands.playsound.failed"));

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
        Holder<SoundEvent> var0 = Holder.direct(SoundEvent.createVariableRangeEvent(param2));
        double var1 = (double)Mth.square(var0.value().getRange(param5));
        int var2 = 0;
        long var3 = param0.getLevel().getRandom().nextLong();
        Iterator var14 = param1.iterator();

        while(true) {
            ServerPlayer var4;
            Vec3 var9;
            float var10;
            while(true) {
                if (!var14.hasNext()) {
                    if (var2 == 0) {
                        throw ERROR_TOO_FAR.create();
                    }

                    if (param1.size() == 1) {
                        param0.sendSuccess(
                            () -> Component.translatable(
                                    "commands.playsound.success.single", Component.translationArg(param2), param1.iterator().next().getDisplayName()
                                ),
                            true
                        );
                    } else {
                        param0.sendSuccess(
                            () -> Component.translatable("commands.playsound.success.multiple", Component.translationArg(param2), param1.size()), true
                        );
                    }

                    return var2;
                }

                var4 = (ServerPlayer)var14.next();
                double var5 = param4.x - var4.getX();
                double var6 = param4.y - var4.getY();
                double var7 = param4.z - var4.getZ();
                double var8 = var5 * var5 + var6 * var6 + var7 * var7;
                var9 = param4;
                var10 = param5;
                if (!(var8 > var1)) {
                    break;
                }

                if (!(param7 <= 0.0F)) {
                    double var11 = Math.sqrt(var8);
                    var9 = new Vec3(var4.getX() + var5 / var11 * 2.0, var4.getY() + var6 / var11 * 2.0, var4.getZ() + var7 / var11 * 2.0);
                    var10 = param7;
                    break;
                }
            }

            var4.connection.send(new ClientboundSoundPacket(var0, param3, var9.x(), var9.y(), var9.z(), var10, param6, var3));
            ++var2;
        }
    }
}
