package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandSourceStack implements SharedSuggestionProvider {
    public static final SimpleCommandExceptionType ERROR_NOT_PLAYER = new SimpleCommandExceptionType(new TranslatableComponent("permissions.requires.player"));
    public static final SimpleCommandExceptionType ERROR_NOT_ENTITY = new SimpleCommandExceptionType(new TranslatableComponent("permissions.requires.entity"));
    private final CommandSource source;
    private final Vec3 worldPosition;
    private final ServerLevel level;
    private final int permissionLevel;
    private final String textName;
    private final Component displayName;
    private final MinecraftServer server;
    private final boolean silent;
    @Nullable
    private final Entity entity;
    private final ResultConsumer<CommandSourceStack> consumer;
    private final EntityAnchorArgument.Anchor anchor;
    private final Vec2 rotation;

    public CommandSourceStack(
        CommandSource param0,
        Vec3 param1,
        Vec2 param2,
        ServerLevel param3,
        int param4,
        String param5,
        Component param6,
        MinecraftServer param7,
        @Nullable Entity param8
    ) {
        this(param0, param1, param2, param3, param4, param5, param6, param7, param8, false, (param0x, param1x, param2x) -> {
        }, EntityAnchorArgument.Anchor.FEET);
    }

    protected CommandSourceStack(
        CommandSource param0,
        Vec3 param1,
        Vec2 param2,
        ServerLevel param3,
        int param4,
        String param5,
        Component param6,
        MinecraftServer param7,
        @Nullable Entity param8,
        boolean param9,
        ResultConsumer<CommandSourceStack> param10,
        EntityAnchorArgument.Anchor param11
    ) {
        this.source = param0;
        this.worldPosition = param1;
        this.level = param3;
        this.silent = param9;
        this.entity = param8;
        this.permissionLevel = param4;
        this.textName = param5;
        this.displayName = param6;
        this.server = param7;
        this.consumer = param10;
        this.anchor = param11;
        this.rotation = param2;
    }

    public CommandSourceStack withEntity(Entity param0) {
        return this.entity == param0
            ? this
            : new CommandSourceStack(
                this.source,
                this.worldPosition,
                this.rotation,
                this.level,
                this.permissionLevel,
                param0.getName().getString(),
                param0.getDisplayName(),
                this.server,
                param0,
                this.silent,
                this.consumer,
                this.anchor
            );
    }

    public CommandSourceStack withPosition(Vec3 param0) {
        return this.worldPosition.equals(param0)
            ? this
            : new CommandSourceStack(
                this.source,
                param0,
                this.rotation,
                this.level,
                this.permissionLevel,
                this.textName,
                this.displayName,
                this.server,
                this.entity,
                this.silent,
                this.consumer,
                this.anchor
            );
    }

    public CommandSourceStack withRotation(Vec2 param0) {
        return this.rotation.equals(param0)
            ? this
            : new CommandSourceStack(
                this.source,
                this.worldPosition,
                param0,
                this.level,
                this.permissionLevel,
                this.textName,
                this.displayName,
                this.server,
                this.entity,
                this.silent,
                this.consumer,
                this.anchor
            );
    }

    public CommandSourceStack withCallback(ResultConsumer<CommandSourceStack> param0) {
        return this.consumer.equals(param0)
            ? this
            : new CommandSourceStack(
                this.source,
                this.worldPosition,
                this.rotation,
                this.level,
                this.permissionLevel,
                this.textName,
                this.displayName,
                this.server,
                this.entity,
                this.silent,
                param0,
                this.anchor
            );
    }

    public CommandSourceStack withCallback(ResultConsumer<CommandSourceStack> param0, BinaryOperator<ResultConsumer<CommandSourceStack>> param1) {
        ResultConsumer<CommandSourceStack> var0 = param1.apply(this.consumer, param0);
        return this.withCallback(var0);
    }

    public CommandSourceStack withSuppressedOutput() {
        return this.silent
            ? this
            : new CommandSourceStack(
                this.source,
                this.worldPosition,
                this.rotation,
                this.level,
                this.permissionLevel,
                this.textName,
                this.displayName,
                this.server,
                this.entity,
                true,
                this.consumer,
                this.anchor
            );
    }

    public CommandSourceStack withPermission(int param0) {
        return param0 == this.permissionLevel
            ? this
            : new CommandSourceStack(
                this.source,
                this.worldPosition,
                this.rotation,
                this.level,
                param0,
                this.textName,
                this.displayName,
                this.server,
                this.entity,
                this.silent,
                this.consumer,
                this.anchor
            );
    }

    public CommandSourceStack withMaximumPermission(int param0) {
        return param0 <= this.permissionLevel
            ? this
            : new CommandSourceStack(
                this.source,
                this.worldPosition,
                this.rotation,
                this.level,
                param0,
                this.textName,
                this.displayName,
                this.server,
                this.entity,
                this.silent,
                this.consumer,
                this.anchor
            );
    }

    public CommandSourceStack withAnchor(EntityAnchorArgument.Anchor param0) {
        return param0 == this.anchor
            ? this
            : new CommandSourceStack(
                this.source,
                this.worldPosition,
                this.rotation,
                this.level,
                this.permissionLevel,
                this.textName,
                this.displayName,
                this.server,
                this.entity,
                this.silent,
                this.consumer,
                param0
            );
    }

    public CommandSourceStack withLevel(ServerLevel param0) {
        return param0 == this.level
            ? this
            : new CommandSourceStack(
                this.source,
                this.worldPosition,
                this.rotation,
                param0,
                this.permissionLevel,
                this.textName,
                this.displayName,
                this.server,
                this.entity,
                this.silent,
                this.consumer,
                this.anchor
            );
    }

    public CommandSourceStack facing(Entity param0, EntityAnchorArgument.Anchor param1) throws CommandSyntaxException {
        return this.facing(param1.apply(param0));
    }

    public CommandSourceStack facing(Vec3 param0) throws CommandSyntaxException {
        Vec3 var0 = this.anchor.apply(this);
        double var1 = param0.x - var0.x;
        double var2 = param0.y - var0.y;
        double var3 = param0.z - var0.z;
        double var4 = (double)Mth.sqrt(var1 * var1 + var3 * var3);
        float var5 = Mth.wrapDegrees((float)(-(Mth.atan2(var2, var4) * 180.0F / (float)Math.PI)));
        float var6 = Mth.wrapDegrees((float)(Mth.atan2(var3, var1) * 180.0F / (float)Math.PI) - 90.0F);
        return this.withRotation(new Vec2(var5, var6));
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public String getTextName() {
        return this.textName;
    }

    @Override
    public boolean hasPermission(int param0) {
        return this.permissionLevel >= param0;
    }

    public Vec3 getPosition() {
        return this.worldPosition;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    public Entity getEntityOrException() throws CommandSyntaxException {
        if (this.entity == null) {
            throw ERROR_NOT_ENTITY.create();
        } else {
            return this.entity;
        }
    }

    public ServerPlayer getPlayerOrException() throws CommandSyntaxException {
        if (!(this.entity instanceof ServerPlayer)) {
            throw ERROR_NOT_PLAYER.create();
        } else {
            return (ServerPlayer)this.entity;
        }
    }

    public Vec2 getRotation() {
        return this.rotation;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public EntityAnchorArgument.Anchor getAnchor() {
        return this.anchor;
    }

    public void sendSuccess(Component param0, boolean param1) {
        if (this.source.acceptsSuccess() && !this.silent) {
            this.source.sendMessage(param0);
        }

        if (param1 && this.source.shouldInformAdmins() && !this.silent) {
            this.broadcastToAdmins(param0);
        }

    }

    private void broadcastToAdmins(Component param0) {
        Component var0 = new TranslatableComponent("chat.type.admin", this.getDisplayName(), param0)
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC});
        if (this.server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
            for(ServerPlayer var1 : this.server.getPlayerList().getPlayers()) {
                if (var1 != this.source && this.server.getPlayerList().isOp(var1.getGameProfile())) {
                    var1.sendMessage(var0);
                }
            }
        }

        if (this.source != this.server && this.server.getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
            this.server.sendMessage(var0);
        }

    }

    public void sendFailure(Component param0) {
        if (this.source.acceptsFailure() && !this.silent) {
            this.source.sendMessage(new TextComponent("").append(param0).withStyle(ChatFormatting.RED));
        }

    }

    public void onCommandComplete(CommandContext<CommandSourceStack> param0, boolean param1, int param2) {
        if (this.consumer != null) {
            this.consumer.onCommandComplete(param0, param1, param2);
        }

    }

    @Override
    public Collection<String> getOnlinePlayerNames() {
        return Lists.newArrayList(this.server.getPlayerNames());
    }

    @Override
    public Collection<String> getAllTeams() {
        return this.server.getScoreboard().getTeamNames();
    }

    @Override
    public Collection<ResourceLocation> getAvailableSoundEvents() {
        return Registry.SOUND_EVENT.keySet();
    }

    @Override
    public Stream<ResourceLocation> getRecipeNames() {
        return this.server.getRecipeManager().getRecipeIds();
    }

    @Override
    public CompletableFuture<Suggestions> customSuggestion(CommandContext<SharedSuggestionProvider> param0, SuggestionsBuilder param1) {
        return null;
    }
}
