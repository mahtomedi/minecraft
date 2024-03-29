package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class PlayerList {
    public static final File USERBANLIST_FILE = new File("banned-players.json");
    public static final File IPBANLIST_FILE = new File("banned-ips.json");
    public static final File OPLIST_FILE = new File("ops.json");
    public static final File WHITELIST_FILE = new File("whitelist.json");
    public static final Component CHAT_FILTERED_FULL = Component.translatable("chat.filtered_full");
    public static final Component DUPLICATE_LOGIN_DISCONNECT_MESSAGE = Component.translatable("multiplayer.disconnect.duplicate_login");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SEND_PLAYER_INFO_INTERVAL = 600;
    private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final MinecraftServer server;
    private final List<ServerPlayer> players = Lists.newArrayList();
    private final Map<UUID, ServerPlayer> playersByUUID = Maps.newHashMap();
    private final UserBanList bans = new UserBanList(USERBANLIST_FILE);
    private final IpBanList ipBans = new IpBanList(IPBANLIST_FILE);
    private final ServerOpList ops = new ServerOpList(OPLIST_FILE);
    private final UserWhiteList whitelist = new UserWhiteList(WHITELIST_FILE);
    private final Map<UUID, ServerStatsCounter> stats = Maps.newHashMap();
    private final Map<UUID, PlayerAdvancements> advancements = Maps.newHashMap();
    private final PlayerDataStorage playerIo;
    private boolean doWhiteList;
    private final LayeredRegistryAccess<RegistryLayer> registries;
    protected final int maxPlayers;
    private int viewDistance;
    private int simulationDistance;
    private boolean allowCheatsForAllPlayers;
    private static final boolean ALLOW_LOGOUTIVATOR = false;
    private int sendAllPlayerInfoIn;

    public PlayerList(MinecraftServer param0, LayeredRegistryAccess<RegistryLayer> param1, PlayerDataStorage param2, int param3) {
        this.server = param0;
        this.registries = param1;
        this.maxPlayers = param3;
        this.playerIo = param2;
    }

    public void placeNewPlayer(Connection param0, ServerPlayer param1, CommonListenerCookie param2) {
        GameProfile var0 = param1.getGameProfile();
        GameProfileCache var1 = this.server.getProfileCache();
        String var3;
        if (var1 != null) {
            Optional<GameProfile> var2 = var1.get(var0.getId());
            var3 = var2.map(GameProfile::getName).orElse(var0.getName());
            var1.add(var0);
        } else {
            var3 = var0.getName();
        }

        CompoundTag var5 = this.load(param1);
        ResourceKey<Level> var6 = var5 != null
            ? DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, var5.get("Dimension"))).resultOrPartial(LOGGER::error).orElse(Level.OVERWORLD)
            : Level.OVERWORLD;
        ServerLevel var7 = this.server.getLevel(var6);
        ServerLevel var8;
        if (var7 == null) {
            LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", var6);
            var8 = this.server.overworld();
        } else {
            var8 = var7;
        }

        param1.setServerLevel(var8);
        String var10 = param0.getLoggableAddress(this.server.logIPs());
        LOGGER.info(
            "{}[{}] logged in with entity id {} at ({}, {}, {})",
            param1.getName().getString(),
            var10,
            param1.getId(),
            param1.getX(),
            param1.getY(),
            param1.getZ()
        );
        LevelData var11 = var8.getLevelData();
        param1.loadGameTypes(var5);
        ServerGamePacketListenerImpl var12 = new ServerGamePacketListenerImpl(this.server, param0, param1, param2);
        GameRules var13 = var8.getGameRules();
        boolean var14 = var13.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
        boolean var15 = var13.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
        boolean var16 = var13.getBoolean(GameRules.RULE_LIMITED_CRAFTING);
        var12.send(
            new ClientboundLoginPacket(
                param1.getId(),
                var11.isHardcore(),
                this.server.levelKeys(),
                this.getMaxPlayers(),
                this.viewDistance,
                this.simulationDistance,
                var15,
                !var14,
                var16,
                param1.createCommonSpawnInfo(var8)
            )
        );
        var12.send(new ClientboundChangeDifficultyPacket(var11.getDifficulty(), var11.isDifficultyLocked()));
        var12.send(new ClientboundPlayerAbilitiesPacket(param1.getAbilities()));
        var12.send(new ClientboundSetCarriedItemPacket(param1.getInventory().selected));
        var12.send(new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes()));
        this.sendPlayerPermissionLevel(param1);
        param1.getStats().markAllDirty();
        param1.getRecipeBook().sendInitialRecipeBook(param1);
        this.updateEntireScoreboard(var8.getScoreboard(), param1);
        this.server.invalidateStatus();
        MutableComponent var17;
        if (param1.getGameProfile().getName().equalsIgnoreCase(var3)) {
            var17 = Component.translatable("multiplayer.player.joined", param1.getDisplayName());
        } else {
            var17 = Component.translatable("multiplayer.player.joined.renamed", param1.getDisplayName(), var3);
        }

        this.broadcastSystemMessage(var17.withStyle(ChatFormatting.YELLOW), false);
        var12.teleport(param1.getX(), param1.getY(), param1.getZ(), param1.getYRot(), param1.getXRot());
        ServerStatus var19 = this.server.getStatus();
        if (var19 != null) {
            param1.sendServerStatus(var19);
        }

        param1.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(this.players));
        this.players.add(param1);
        this.playersByUUID.put(param1.getUUID(), param1);
        this.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(param1)));
        this.sendLevelInfo(param1, var8);
        var8.addNewPlayer(param1);
        this.server.getCustomBossEvents().onPlayerConnect(param1);

        for(MobEffectInstance var20 : param1.getActiveEffects()) {
            var12.send(new ClientboundUpdateMobEffectPacket(param1.getId(), var20));
        }

        if (var5 != null && var5.contains("RootVehicle", 10)) {
            CompoundTag var21 = var5.getCompound("RootVehicle");
            Entity var22 = EntityType.loadEntityRecursive(var21.getCompound("Entity"), var8, param1x -> !var8.addWithUUID(param1x) ? null : param1x);
            if (var22 != null) {
                UUID var23;
                if (var21.hasUUID("Attach")) {
                    var23 = var21.getUUID("Attach");
                } else {
                    var23 = null;
                }

                if (var22.getUUID().equals(var23)) {
                    param1.startRiding(var22, true);
                } else {
                    for(Entity var25 : var22.getIndirectPassengers()) {
                        if (var25.getUUID().equals(var23)) {
                            param1.startRiding(var25, true);
                            break;
                        }
                    }
                }

                if (!param1.isPassenger()) {
                    LOGGER.warn("Couldn't reattach entity to player");
                    var22.discard();

                    for(Entity var26 : var22.getIndirectPassengers()) {
                        var26.discard();
                    }
                }
            }
        }

        param1.initInventoryMenu();
    }

    protected void updateEntireScoreboard(ServerScoreboard param0, ServerPlayer param1) {
        Set<Objective> var0 = Sets.newHashSet();

        for(PlayerTeam var1 : param0.getPlayerTeams()) {
            param1.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(var1, true));
        }

        for(DisplaySlot var2 : DisplaySlot.values()) {
            Objective var3 = param0.getDisplayObjective(var2);
            if (var3 != null && !var0.contains(var3)) {
                for(Packet<?> var5 : param0.getStartTrackingPackets(var3)) {
                    param1.connection.send(var5);
                }

                var0.add(var3);
            }
        }

    }

    public void addWorldborderListener(ServerLevel param0) {
        param0.getWorldBorder().addListener(new BorderChangeListener() {
            @Override
            public void onBorderSizeSet(WorldBorder param0, double param1) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderSizePacket(param0));
            }

            @Override
            public void onBorderSizeLerping(WorldBorder param0, double param1, double param2, long param3) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderLerpSizePacket(param0));
            }

            @Override
            public void onBorderCenterSet(WorldBorder param0, double param1, double param2) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderCenterPacket(param0));
            }

            @Override
            public void onBorderSetWarningTime(WorldBorder param0, int param1) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDelayPacket(param0));
            }

            @Override
            public void onBorderSetWarningBlocks(WorldBorder param0, int param1) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDistancePacket(param0));
            }

            @Override
            public void onBorderSetDamagePerBlock(WorldBorder param0, double param1) {
            }

            @Override
            public void onBorderSetDamageSafeZOne(WorldBorder param0, double param1) {
            }
        });
    }

    @Nullable
    public CompoundTag load(ServerPlayer param0) {
        CompoundTag var0 = this.server.getWorldData().getLoadedPlayerTag();
        CompoundTag var1;
        if (this.server.isSingleplayerOwner(param0.getGameProfile()) && var0 != null) {
            var1 = var0;
            param0.load(var0);
            LOGGER.debug("loading single player");
        } else {
            var1 = this.playerIo.load(param0);
        }

        return var1;
    }

    protected void save(ServerPlayer param0) {
        this.playerIo.save(param0);
        ServerStatsCounter var0 = this.stats.get(param0.getUUID());
        if (var0 != null) {
            var0.save();
        }

        PlayerAdvancements var1 = this.advancements.get(param0.getUUID());
        if (var1 != null) {
            var1.save();
        }

    }

    public void remove(ServerPlayer param0) {
        ServerLevel var0 = param0.serverLevel();
        param0.awardStat(Stats.LEAVE_GAME);
        this.save(param0);
        if (param0.isPassenger()) {
            Entity var1 = param0.getRootVehicle();
            if (var1.hasExactlyOnePlayerPassenger()) {
                LOGGER.debug("Removing player mount");
                param0.stopRiding();
                var1.getPassengersAndSelf().forEach(param0x -> param0x.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
            }
        }

        param0.unRide();
        var0.removePlayerImmediately(param0, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        param0.getAdvancements().stopListening();
        this.players.remove(param0);
        this.server.getCustomBossEvents().onPlayerDisconnect(param0);
        UUID var2 = param0.getUUID();
        ServerPlayer var3 = this.playersByUUID.get(var2);
        if (var3 == param0) {
            this.playersByUUID.remove(var2);
            this.stats.remove(var2);
            this.advancements.remove(var2);
        }

        this.broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(param0.getUUID())));
    }

    @Nullable
    public Component canPlayerLogin(SocketAddress param0, GameProfile param1) {
        if (this.bans.isBanned(param1)) {
            UserBanListEntry var0 = this.bans.get(param1);
            MutableComponent var1 = Component.translatable("multiplayer.disconnect.banned.reason", var0.getReason());
            if (var0.getExpires() != null) {
                var1.append(Component.translatable("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(var0.getExpires())));
            }

            return var1;
        } else if (!this.isWhiteListed(param1)) {
            return Component.translatable("multiplayer.disconnect.not_whitelisted");
        } else if (this.ipBans.isBanned(param0)) {
            IpBanListEntry var2 = this.ipBans.get(param0);
            MutableComponent var3 = Component.translatable("multiplayer.disconnect.banned_ip.reason", var2.getReason());
            if (var2.getExpires() != null) {
                var3.append(Component.translatable("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(var2.getExpires())));
            }

            return var3;
        } else {
            return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(param1)
                ? Component.translatable("multiplayer.disconnect.server_full")
                : null;
        }
    }

    public ServerPlayer getPlayerForLogin(GameProfile param0, ClientInformation param1) {
        return new ServerPlayer(this.server, this.server.overworld(), param0, param1);
    }

    public boolean disconnectAllPlayersWithProfile(GameProfile param0) {
        UUID var0 = param0.getId();
        Set<ServerPlayer> var1 = Sets.newIdentityHashSet();

        for(ServerPlayer var2 : this.players) {
            if (var2.getUUID().equals(var0)) {
                var1.add(var2);
            }
        }

        ServerPlayer var3 = this.playersByUUID.get(param0.getId());
        if (var3 != null) {
            var1.add(var3);
        }

        for(ServerPlayer var4 : var1) {
            var4.connection.disconnect(DUPLICATE_LOGIN_DISCONNECT_MESSAGE);
        }

        return !var1.isEmpty();
    }

    public ServerPlayer respawn(ServerPlayer param0, boolean param1) {
        this.players.remove(param0);
        param0.serverLevel().removePlayerImmediately(param0, Entity.RemovalReason.DISCARDED);
        BlockPos var0 = param0.getRespawnPosition();
        float var1 = param0.getRespawnAngle();
        boolean var2 = param0.isRespawnForced();
        ServerLevel var3 = this.server.getLevel(param0.getRespawnDimension());
        Optional<Vec3> var4;
        if (var3 != null && var0 != null) {
            var4 = Player.findRespawnPositionAndUseSpawnBlock(var3, var0, var1, var2, param1);
        } else {
            var4 = Optional.empty();
        }

        ServerLevel var6 = var3 != null && var4.isPresent() ? var3 : this.server.overworld();
        ServerPlayer var7 = new ServerPlayer(this.server, var6, param0.getGameProfile(), param0.clientInformation());
        var7.connection = param0.connection;
        var7.restoreFrom(param0, param1);
        var7.setId(param0.getId());
        var7.setMainArm(param0.getMainArm());

        for(String var8 : param0.getTags()) {
            var7.addTag(var8);
        }

        boolean var9 = false;
        if (var4.isPresent()) {
            BlockState var10 = var6.getBlockState(var0);
            boolean var11 = var10.is(Blocks.RESPAWN_ANCHOR);
            Vec3 var12 = var4.get();
            float var15;
            if (!var10.is(BlockTags.BEDS) && !var11) {
                var15 = var1;
            } else {
                Vec3 var13 = Vec3.atBottomCenterOf(var0).subtract(var12).normalize();
                var15 = (float)Mth.wrapDegrees(Mth.atan2(var13.z, var13.x) * 180.0F / (float)Math.PI - 90.0);
            }

            var7.moveTo(var12.x, var12.y, var12.z, var15, 0.0F);
            var7.setRespawnPosition(var6.dimension(), var0, var1, var2, false);
            var9 = !param1 && var11;
        } else if (var0 != null) {
            var7.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
        }

        while(!var6.noCollision(var7) && var7.getY() < (double)var6.getMaxBuildHeight()) {
            var7.setPos(var7.getX(), var7.getY() + 1.0, var7.getZ());
        }

        byte var16 = (byte)(param1 ? 1 : 0);
        ServerLevel var17 = var7.serverLevel();
        LevelData var18 = var17.getLevelData();
        var7.connection.send(new ClientboundRespawnPacket(var7.createCommonSpawnInfo(var17), var16));
        var7.connection.teleport(var7.getX(), var7.getY(), var7.getZ(), var7.getYRot(), var7.getXRot());
        var7.connection.send(new ClientboundSetDefaultSpawnPositionPacket(var6.getSharedSpawnPos(), var6.getSharedSpawnAngle()));
        var7.connection.send(new ClientboundChangeDifficultyPacket(var18.getDifficulty(), var18.isDifficultyLocked()));
        var7.connection.send(new ClientboundSetExperiencePacket(var7.experienceProgress, var7.totalExperience, var7.experienceLevel));
        this.sendLevelInfo(var7, var6);
        this.sendPlayerPermissionLevel(var7);
        var6.addRespawnedPlayer(var7);
        this.players.add(var7);
        this.playersByUUID.put(var7.getUUID(), var7);
        var7.initInventoryMenu();
        var7.setHealth(var7.getHealth());
        if (var9) {
            var7.connection
                .send(
                    new ClientboundSoundPacket(
                        SoundEvents.RESPAWN_ANCHOR_DEPLETE,
                        SoundSource.BLOCKS,
                        (double)var0.getX(),
                        (double)var0.getY(),
                        (double)var0.getZ(),
                        1.0F,
                        1.0F,
                        var6.getRandom().nextLong()
                    )
                );
        }

        return var7;
    }

    public void sendPlayerPermissionLevel(ServerPlayer param0) {
        GameProfile var0 = param0.getGameProfile();
        int var1 = this.server.getProfilePermissions(var0);
        this.sendPlayerPermissionLevel(param0, var1);
    }

    public void tick() {
        if (++this.sendAllPlayerInfoIn > 600) {
            this.broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY), this.players));
            this.sendAllPlayerInfoIn = 0;
        }

    }

    public void broadcastAll(Packet<?> param0) {
        for(ServerPlayer var0 : this.players) {
            var0.connection.send(param0);
        }

    }

    public void broadcastAll(Packet<?> param0, ResourceKey<Level> param1) {
        for(ServerPlayer var0 : this.players) {
            if (var0.level().dimension() == param1) {
                var0.connection.send(param0);
            }
        }

    }

    public void broadcastSystemToTeam(Player param0, Component param1) {
        Team var0 = param0.getTeam();
        if (var0 != null) {
            for(String var2 : var0.getPlayers()) {
                ServerPlayer var3 = this.getPlayerByName(var2);
                if (var3 != null && var3 != param0) {
                    var3.sendSystemMessage(param1);
                }
            }

        }
    }

    public void broadcastSystemToAllExceptTeam(Player param0, Component param1) {
        Team var0 = param0.getTeam();
        if (var0 == null) {
            this.broadcastSystemMessage(param1, false);
        } else {
            for(int var1 = 0; var1 < this.players.size(); ++var1) {
                ServerPlayer var2 = this.players.get(var1);
                if (var2.getTeam() != var0) {
                    var2.sendSystemMessage(param1);
                }
            }

        }
    }

    public String[] getPlayerNamesArray() {
        String[] var0 = new String[this.players.size()];

        for(int var1 = 0; var1 < this.players.size(); ++var1) {
            var0[var1] = this.players.get(var1).getGameProfile().getName();
        }

        return var0;
    }

    public UserBanList getBans() {
        return this.bans;
    }

    public IpBanList getIpBans() {
        return this.ipBans;
    }

    public void op(GameProfile param0) {
        this.ops.add(new ServerOpListEntry(param0, this.server.getOperatorUserPermissionLevel(), this.ops.canBypassPlayerLimit(param0)));
        ServerPlayer var0 = this.getPlayer(param0.getId());
        if (var0 != null) {
            this.sendPlayerPermissionLevel(var0);
        }

    }

    public void deop(GameProfile param0) {
        this.ops.remove(param0);
        ServerPlayer var0 = this.getPlayer(param0.getId());
        if (var0 != null) {
            this.sendPlayerPermissionLevel(var0);
        }

    }

    private void sendPlayerPermissionLevel(ServerPlayer param0, int param1) {
        if (param0.connection != null) {
            byte var0;
            if (param1 <= 0) {
                var0 = 24;
            } else if (param1 >= 4) {
                var0 = 28;
            } else {
                var0 = (byte)(24 + param1);
            }

            param0.connection.send(new ClientboundEntityEventPacket(param0, var0));
        }

        this.server.getCommands().sendCommands(param0);
    }

    public boolean isWhiteListed(GameProfile param0) {
        return !this.doWhiteList || this.ops.contains(param0) || this.whitelist.contains(param0);
    }

    public boolean isOp(GameProfile param0) {
        return this.ops.contains(param0)
            || this.server.isSingleplayerOwner(param0) && this.server.getWorldData().getAllowCommands()
            || this.allowCheatsForAllPlayers;
    }

    @Nullable
    public ServerPlayer getPlayerByName(String param0) {
        for(ServerPlayer var0 : this.players) {
            if (var0.getGameProfile().getName().equalsIgnoreCase(param0)) {
                return var0;
            }
        }

        return null;
    }

    public void broadcast(@Nullable Player param0, double param1, double param2, double param3, double param4, ResourceKey<Level> param5, Packet<?> param6) {
        for(int var0 = 0; var0 < this.players.size(); ++var0) {
            ServerPlayer var1 = this.players.get(var0);
            if (var1 != param0 && var1.level().dimension() == param5) {
                double var2 = param1 - var1.getX();
                double var3 = param2 - var1.getY();
                double var4 = param3 - var1.getZ();
                if (var2 * var2 + var3 * var3 + var4 * var4 < param4 * param4) {
                    var1.connection.send(param6);
                }
            }
        }

    }

    public void saveAll() {
        for(int var0 = 0; var0 < this.players.size(); ++var0) {
            this.save(this.players.get(var0));
        }

    }

    public UserWhiteList getWhiteList() {
        return this.whitelist;
    }

    public String[] getWhiteListNames() {
        return this.whitelist.getUserList();
    }

    public ServerOpList getOps() {
        return this.ops;
    }

    public String[] getOpNames() {
        return this.ops.getUserList();
    }

    public void reloadWhiteList() {
    }

    public void sendLevelInfo(ServerPlayer param0, ServerLevel param1) {
        WorldBorder var0 = this.server.overworld().getWorldBorder();
        param0.connection.send(new ClientboundInitializeBorderPacket(var0));
        param0.connection
            .send(new ClientboundSetTimePacket(param1.getGameTime(), param1.getDayTime(), param1.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
        param0.connection.send(new ClientboundSetDefaultSpawnPositionPacket(param1.getSharedSpawnPos(), param1.getSharedSpawnAngle()));
        if (param1.isRaining()) {
            param0.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
            param0.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, param1.getRainLevel(1.0F)));
            param0.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, param1.getThunderLevel(1.0F)));
        }

        param0.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START, 0.0F));
        this.server.tickRateManager().updateJoiningPlayer(param0);
    }

    public void sendAllPlayerInfo(ServerPlayer param0) {
        param0.inventoryMenu.sendAllDataToRemote();
        param0.resetSentInfo();
        param0.connection.send(new ClientboundSetCarriedItemPacket(param0.getInventory().selected));
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public boolean isUsingWhitelist() {
        return this.doWhiteList;
    }

    public void setUsingWhiteList(boolean param0) {
        this.doWhiteList = param0;
    }

    public List<ServerPlayer> getPlayersWithAddress(String param0) {
        List<ServerPlayer> var0 = Lists.newArrayList();

        for(ServerPlayer var1 : this.players) {
            if (var1.getIpAddress().equals(param0)) {
                var0.add(var1);
            }
        }

        return var0;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getSimulationDistance() {
        return this.simulationDistance;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    @Nullable
    public CompoundTag getSingleplayerData() {
        return null;
    }

    public void setAllowCheatsForAllPlayers(boolean param0) {
        this.allowCheatsForAllPlayers = param0;
    }

    public void removeAll() {
        for(int var0 = 0; var0 < this.players.size(); ++var0) {
            this.players.get(var0).connection.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
        }

    }

    public void broadcastSystemMessage(Component param0, boolean param1) {
        this.broadcastSystemMessage(param0, param1x -> param0, param1);
    }

    public void broadcastSystemMessage(Component param0, Function<ServerPlayer, Component> param1, boolean param2) {
        this.server.sendSystemMessage(param0);

        for(ServerPlayer var0 : this.players) {
            Component var1 = param1.apply(var0);
            if (var1 != null) {
                var0.sendSystemMessage(var1, param2);
            }
        }

    }

    public void broadcastChatMessage(PlayerChatMessage param0, CommandSourceStack param1, ChatType.Bound param2) {
        this.broadcastChatMessage(param0, param1::shouldFilterMessageTo, param1.getPlayer(), param2);
    }

    public void broadcastChatMessage(PlayerChatMessage param0, ServerPlayer param1, ChatType.Bound param2) {
        this.broadcastChatMessage(param0, param1::shouldFilterMessageTo, param1, param2);
    }

    private void broadcastChatMessage(PlayerChatMessage param0, Predicate<ServerPlayer> param1, @Nullable ServerPlayer param2, ChatType.Bound param3) {
        boolean var0 = this.verifyChatTrusted(param0);
        this.server.logChatMessage(param0.decoratedContent(), param3, var0 ? null : "Not Secure");
        OutgoingChatMessage var1 = OutgoingChatMessage.create(param0);
        boolean var2 = false;

        for(ServerPlayer var3 : this.players) {
            boolean var4 = param1.test(var3);
            var3.sendChatMessage(var1, var4, param3);
            var2 |= var4 && param0.isFullyFiltered();
        }

        if (var2 && param2 != null) {
            param2.sendSystemMessage(CHAT_FILTERED_FULL);
        }

    }

    private boolean verifyChatTrusted(PlayerChatMessage param0) {
        return param0.hasSignature() && !param0.hasExpiredServer(Instant.now());
    }

    public ServerStatsCounter getPlayerStats(Player param0) {
        UUID var0 = param0.getUUID();
        ServerStatsCounter var1 = this.stats.get(var0);
        if (var1 == null) {
            File var2 = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
            File var3 = new File(var2, var0 + ".json");
            if (!var3.exists()) {
                File var4 = new File(var2, param0.getName().getString() + ".json");
                Path var5 = var4.toPath();
                if (FileUtil.isPathNormalized(var5) && FileUtil.isPathPortable(var5) && var5.startsWith(var2.getPath()) && var4.isFile()) {
                    var4.renameTo(var3);
                }
            }

            var1 = new ServerStatsCounter(this.server, var3);
            this.stats.put(var0, var1);
        }

        return var1;
    }

    public PlayerAdvancements getPlayerAdvancements(ServerPlayer param0) {
        UUID var0 = param0.getUUID();
        PlayerAdvancements var1 = this.advancements.get(var0);
        if (var1 == null) {
            Path var2 = this.server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(var0 + ".json");
            var1 = new PlayerAdvancements(this.server.getFixerUpper(), this, this.server.getAdvancements(), var2, param0);
            this.advancements.put(var0, var1);
        }

        var1.setPlayer(param0);
        return var1;
    }

    public void setViewDistance(int param0) {
        this.viewDistance = param0;
        this.broadcastAll(new ClientboundSetChunkCacheRadiusPacket(param0));

        for(ServerLevel var0 : this.server.getAllLevels()) {
            if (var0 != null) {
                var0.getChunkSource().setViewDistance(param0);
            }
        }

    }

    public void setSimulationDistance(int param0) {
        this.simulationDistance = param0;
        this.broadcastAll(new ClientboundSetSimulationDistancePacket(param0));

        for(ServerLevel var0 : this.server.getAllLevels()) {
            if (var0 != null) {
                var0.getChunkSource().setSimulationDistance(param0);
            }
        }

    }

    public List<ServerPlayer> getPlayers() {
        return this.players;
    }

    @Nullable
    public ServerPlayer getPlayer(UUID param0) {
        return this.playersByUUID.get(param0);
    }

    public boolean canBypassPlayerLimit(GameProfile param0) {
        return false;
    }

    public void reloadResources() {
        for(PlayerAdvancements var0 : this.advancements.values()) {
            var0.reload(this.server.getAdvancements());
        }

        this.broadcastAll(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
        ClientboundUpdateRecipesPacket var1 = new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes());

        for(ServerPlayer var2 : this.players) {
            var2.connection.send(var1);
            var2.getRecipeBook().sendInitialRecipeBook(var2);
        }

    }

    public boolean isAllowCheatsForAllPlayers() {
        return this.allowCheatsForAllPlayers;
    }
}
