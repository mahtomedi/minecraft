package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandSourceStack implements SharedSuggestionProvider {
    public static final SimpleCommandExceptionType ERROR_NOT_PLAYER = new SimpleCommandExceptionType(Component.translatable("permissions.requires.player"));
    public static final SimpleCommandExceptionType ERROR_NOT_ENTITY = new SimpleCommandExceptionType(Component.translatable("permissions.requires.entity"));
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
    @Nullable
    private final ResultConsumer<CommandSourceStack> consumer;
    private final EntityAnchorArgument.Anchor anchor;
    private final Vec2 rotation;
    private final CommandSigningContext signingContext;

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
        }, EntityAnchorArgument.Anchor.FEET, CommandSigningContext.NONE);
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
        @Nullable ResultConsumer<CommandSourceStack> param10,
        EntityAnchorArgument.Anchor param11,
        CommandSigningContext param12
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
        this.signingContext = param12;
    }

    public CommandSourceStack withSource(CommandSource param0) {
        return this.source == param0
            ? this
            : new CommandSourceStack(
                param0,
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
                this.anchor,
                this.signingContext
            );
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
                this.anchor,
                this.signingContext
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
                this.anchor,
                this.signingContext
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
                this.anchor,
                this.signingContext
            );
    }

    public CommandSourceStack withCallback(ResultConsumer<CommandSourceStack> param0) {
        return Objects.equals(this.consumer, param0)
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
                this.anchor,
                this.signingContext
            );
    }

    public CommandSourceStack withCallback(ResultConsumer<CommandSourceStack> param0, BinaryOperator<ResultConsumer<CommandSourceStack>> param1) {
        ResultConsumer<CommandSourceStack> var0 = param1.apply(this.consumer, param0);
        return this.withCallback(var0);
    }

    public CommandSourceStack withSuppressedOutput() {
        return !this.silent && !this.source.alwaysAccepts()
            ? new CommandSourceStack(
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
                this.anchor,
                this.signingContext
            )
            : this;
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
                this.anchor,
                this.signingContext
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
                this.anchor,
                this.signingContext
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
                param0,
                this.signingContext
            );
    }

    public CommandSourceStack withLevel(ServerLevel param0) {
        if (param0 == this.level) {
            return this;
        } else {
            double var0 = DimensionType.getTeleportationScale(this.level.dimensionType(), param0.dimensionType());
            Vec3 var1 = new Vec3(this.worldPosition.x * var0, this.worldPosition.y, this.worldPosition.z * var0);
            return new CommandSourceStack(
                this.source,
                var1,
                this.rotation,
                param0,
                this.permissionLevel,
                this.textName,
                this.displayName,
                this.server,
                this.entity,
                this.silent,
                this.consumer,
                this.anchor,
                this.signingContext
            );
        }
    }

    public CommandSourceStack facing(Entity param0, EntityAnchorArgument.Anchor param1) {
        return this.facing(param1.apply(param0));
    }

    public CommandSourceStack facing(Vec3 param0) {
        Vec3 var0 = this.anchor.apply(this);
        double var1 = param0.x - var0.x;
        double var2 = param0.y - var0.y;
        double var3 = param0.z - var0.z;
        double var4 = Math.sqrt(var1 * var1 + var3 * var3);
        float var5 = Mth.wrapDegrees((float)(-(Mth.atan2(var2, var4) * 180.0F / (float)Math.PI)));
        float var6 = Mth.wrapDegrees((float)(Mth.atan2(var3, var1) * 180.0F / (float)Math.PI) - 90.0F);
        return this.withRotation(new Vec2(var5, var6));
    }

    public CommandSourceStack withSigningContext(CommandSigningContext param0) {
        return param0 == this.signingContext
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
                this.anchor,
                param0
            );
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public String getTextName() {
        return this.textName;
    }

    public ChatSender asChatSender() {
        return this.entity != null ? this.entity.asChatSender() : ChatSender.system(this.getDisplayName());
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
        Entity var2 = this.entity;
        if (var2 instanceof ServerPlayer) {
            return (ServerPlayer)var2;
        } else {
            throw ERROR_NOT_PLAYER.create();
        }
    }

    public boolean isPlayer() {
        return this.entity instanceof ServerPlayer;
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

    public CommandSigningContext getSigningContext() {
        return this.signingContext;
    }

    public void sendSuccess(Component param0, boolean param1) {
        if (this.source.acceptsSuccess() && !this.silent) {
            this.source.sendSystemMessage(param0);
        }

        if (param1 && this.source.shouldInformAdmins() && !this.silent) {
            this.broadcastToAdmins(param0);
        }

    }

    private void broadcastToAdmins(Component param0) {
        Component var0 = Component.translatable("chat.type.admin", this.getDisplayName(), param0).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        if (this.server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
            for(ServerPlayer var1 : this.server.getPlayerList().getPlayers()) {
                if (var1 != this.source && this.server.getPlayerList().isOp(var1.getGameProfile())) {
                    var1.sendSystemMessage(var0);
                }
            }
        }

        if (this.source != this.server && this.server.getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
            this.server.sendSystemMessage(var0);
        }

    }

    public void sendFailure(Component param0) {
        if (this.source.acceptsFailure() && !this.silent) {
            this.source.sendSystemMessage(Component.empty().append(param0).withStyle(ChatFormatting.RED));
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
    public CompletableFuture<Suggestions> customSuggestion(CommandContext<?> param0) {
        return Suggestions.empty();
    }

    @Override
    public CompletableFuture<Suggestions> suggestRegistryElements(
        ResourceKey<? extends Registry<?>> param0, SharedSuggestionProvider.ElementSuggestionType param1, SuggestionsBuilder param2, CommandContext<?> param3
    ) {
        return this.registryAccess().registry(param0).map(param2x -> {
            this.suggestRegistryElements(param2x, param1, param2);
            return param2.buildFuture();
        }).orElseGet(Suggestions::empty);
    }

    @Override
    public Set<ResourceKey<Level>> levels() {
        return this.server.levelKeys();
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.server.registryAccess();
    }
}
