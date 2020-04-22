package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PlayerList {
    public static final File USERBANLIST_FILE = new File("banned-players.json");
    public static final File IPBANLIST_FILE = new File("banned-ips.json");
    public static final File OPLIST_FILE = new File("ops.json");
    public static final File WHITELIST_FILE = new File("whitelist.json");
    private static final Logger LOGGER = LogManager.getLogger();
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
    protected final int maxPlayers;
    private int viewDistance;
    private GameType overrideGameMode;
    private boolean allowCheatsForAllPlayers;
    private int sendAllPlayerInfoIn;

    public PlayerList(MinecraftServer param0, PlayerDataStorage param1, int param2) {
        this.server = param0;
        this.maxPlayers = param2;
        this.playerIo = param1;
        this.getBans().setEnabled(true);
        this.getIpBans().setEnabled(true);
    }

    public void placeNewPlayer(Connection param0, ServerPlayer param1) {
        GameProfile var0 = param1.getGameProfile();
        GameProfileCache var1 = this.server.getProfileCache();
        GameProfile var2 = var1.get(var0.getId());
        String var3 = var2 == null ? var0.getName() : var2.getName();
        var1.add(var0);
        CompoundTag var4 = this.load(param1);
        ServerLevel var5 = this.server.getLevel(param1.dimension);
        param1.setLevel(var5);
        param1.gameMode.setLevel((ServerLevel)param1.level);
        String var6 = "local";
        if (param0.getRemoteAddress() != null) {
            var6 = param0.getRemoteAddress().toString();
        }

        LOGGER.info(
            "{}[{}] logged in with entity id {} at ({}, {}, {})",
            param1.getName().getString(),
            var6,
            param1.getId(),
            param1.getX(),
            param1.getY(),
            param1.getZ()
        );
        LevelData var7 = var5.getLevelData();
        this.updatePlayerGameMode(param1, null, var5);
        ServerGamePacketListenerImpl var8 = new ServerGamePacketListenerImpl(this.server, param0, param1);
        GameRules var9 = var5.getGameRules();
        boolean var10 = var9.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
        boolean var11 = var9.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
        var8.send(
            new ClientboundLoginPacket(
                param1.getId(),
                param1.gameMode.getGameModeForPlayer(),
                LevelData.obfuscateSeed(var7.getSeed()),
                var7.isHardcore(),
                var5.dimension.getType(),
                this.getMaxPlayers(),
                var7.getGeneratorType(),
                this.viewDistance,
                var11,
                !var10
            )
        );
        var8.send(
            new ClientboundCustomPayloadPacket(
                ClientboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(this.getServer().getServerModName())
            )
        );
        var8.send(new ClientboundChangeDifficultyPacket(var7.getDifficulty(), var7.isDifficultyLocked()));
        var8.send(new ClientboundPlayerAbilitiesPacket(param1.abilities));
        var8.send(new ClientboundSetCarriedItemPacket(param1.inventory.selected));
        var8.send(new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes()));
        var8.send(new ClientboundUpdateTagsPacket(this.server.getTags()));
        this.sendPlayerPermissionLevel(param1);
        param1.getStats().markAllDirty();
        param1.getRecipeBook().sendInitialRecipeBook(param1);
        this.updateEntireScoreboard(var5.getScoreboard(), param1);
        this.server.invalidateStatus();
        MutableComponent var12;
        if (param1.getGameProfile().getName().equalsIgnoreCase(var3)) {
            var12 = new TranslatableComponent("multiplayer.player.joined", param1.getDisplayName());
        } else {
            var12 = new TranslatableComponent("multiplayer.player.joined.renamed", param1.getDisplayName(), var3);
        }

        this.broadcastMessage(var12.withStyle(ChatFormatting.YELLOW));
        var8.teleport(param1.getX(), param1.getY(), param1.getZ(), param1.yRot, param1.xRot);
        this.players.add(param1);
        this.playersByUUID.put(param1.getUUID(), param1);
        this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, param1));

        for(int var14 = 0; var14 < this.players.size(); ++var14) {
            param1.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this.players.get(var14)));
        }

        var5.addNewPlayer(param1);
        this.server.getCustomBossEvents().onPlayerConnect(param1);
        this.sendLevelInfo(param1, var5);
        if (!this.server.getResourcePack().isEmpty()) {
            param1.sendTexturePack(this.server.getResourcePack(), this.server.getResourcePackHash());
        }

        for(MobEffectInstance var15 : param1.getActiveEffects()) {
            var8.send(new ClientboundUpdateMobEffectPacket(param1.getId(), var15));
        }

        if (var4 != null && var4.contains("RootVehicle", 10)) {
            CompoundTag var16 = var4.getCompound("RootVehicle");
            Entity var17 = EntityType.loadEntityRecursive(var16.getCompound("Entity"), var5, param1x -> !var5.addWithUUID(param1x) ? null : param1x);
            if (var17 != null) {
                UUID var18;
                if (var16.hasUUID("Attach")) {
                    var18 = var16.getUUID("Attach");
                } else {
                    var18 = null;
                }

                if (var17.getUUID().equals(var18)) {
                    param1.startRiding(var17, true);
                } else {
                    for(Entity var20 : var17.getIndirectPassengers()) {
                        if (var20.getUUID().equals(var18)) {
                            param1.startRiding(var20, true);
                            break;
                        }
                    }
                }

                if (!param1.isPassenger()) {
                    LOGGER.warn("Couldn't reattach entity to player");
                    var5.despawn(var17);

                    for(Entity var21 : var17.getIndirectPassengers()) {
                        var5.despawn(var21);
                    }
                }
            }
        }

        param1.initMenu();
    }

    protected void updateEntireScoreboard(ServerScoreboard param0, ServerPlayer param1) {
        Set<Objective> var0 = Sets.newHashSet();

        for(PlayerTeam var1 : param0.getPlayerTeams()) {
            param1.connection.send(new ClientboundSetPlayerTeamPacket(var1, 0));
        }

        for(int var2 = 0; var2 < 19; ++var2) {
            Objective var3 = param0.getDisplayObjective(var2);
            if (var3 != null && !var0.contains(var3)) {
                for(Packet<?> var5 : param0.getStartTrackingPackets(var3)) {
                    param1.connection.send(var5);
                }

                var0.add(var3);
            }
        }

    }

    public void setLevel(ServerLevel param0) {
        param0.getWorldBorder().addListener(new BorderChangeListener() {
            @Override
            public void onBorderSizeSet(WorldBorder param0, double param1) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderPacket(param0, ClientboundSetBorderPacket.Type.SET_SIZE));
            }

            @Override
            public void onBorderSizeLerping(WorldBorder param0, double param1, double param2, long param3) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderPacket(param0, ClientboundSetBorderPacket.Type.LERP_SIZE));
            }

            @Override
            public void onBorderCenterSet(WorldBorder param0, double param1, double param2) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderPacket(param0, ClientboundSetBorderPacket.Type.SET_CENTER));
            }

            @Override
            public void onBorderSetWarningTime(WorldBorder param0, int param1) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderPacket(param0, ClientboundSetBorderPacket.Type.SET_WARNING_TIME));
            }

            @Override
            public void onBorderSetWarningBlocks(WorldBorder param0, int param1) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderPacket(param0, ClientboundSetBorderPacket.Type.SET_WARNING_BLOCKS));
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
        if (param0.getName().getString().equals(this.server.getSingleplayerName()) && var0 != null) {
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
        ServerLevel var0 = param0.getLevel();
        param0.awardStat(Stats.LEAVE_GAME);
        this.save(param0);
        if (param0.isPassenger()) {
            Entity var1 = param0.getRootVehicle();
            if (var1.hasOnePlayerPassenger()) {
                LOGGER.debug("Removing player mount");
                param0.stopRiding();
                var0.despawn(var1);
                var1.removed = true;

                for(Entity var2 : var1.getIndirectPassengers()) {
                    var0.despawn(var2);
                    var2.removed = true;
                }

                var0.getChunk(param0.xChunk, param0.zChunk).markUnsaved();
            }
        }

        param0.unRide();
        var0.removePlayerImmediately(param0);
        param0.getAdvancements().stopListening();
        this.players.remove(param0);
        this.server.getCustomBossEvents().onPlayerDisconnect(param0);
        UUID var3 = param0.getUUID();
        ServerPlayer var4 = this.playersByUUID.get(var3);
        if (var4 == param0) {
            this.playersByUUID.remove(var3);
            this.stats.remove(var3);
            this.advancements.remove(var3);
        }

        this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, param0));
    }

    @Nullable
    public Component canPlayerLogin(SocketAddress param0, GameProfile param1) {
        if (this.bans.isBanned(param1)) {
            UserBanListEntry var0 = this.bans.get(param1);
            MutableComponent var1 = new TranslatableComponent("multiplayer.disconnect.banned.reason", var0.getReason());
            if (var0.getExpires() != null) {
                var1.append(new TranslatableComponent("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(var0.getExpires())));
            }

            return var1;
        } else if (!this.isWhiteListed(param1)) {
            return new TranslatableComponent("multiplayer.disconnect.not_whitelisted");
        } else if (this.ipBans.isBanned(param0)) {
            IpBanListEntry var2 = this.ipBans.get(param0);
            MutableComponent var3 = new TranslatableComponent("multiplayer.disconnect.banned_ip.reason", var2.getReason());
            if (var2.getExpires() != null) {
                var3.append(new TranslatableComponent("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(var2.getExpires())));
            }

            return var3;
        } else {
            return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(param1)
                ? new TranslatableComponent("multiplayer.disconnect.server_full")
                : null;
        }
    }

    public ServerPlayer getPlayerForLogin(GameProfile param0) {
        UUID var0 = Player.createPlayerUUID(param0);
        List<ServerPlayer> var1 = Lists.newArrayList();

        for(int var2 = 0; var2 < this.players.size(); ++var2) {
            ServerPlayer var3 = this.players.get(var2);
            if (var3.getUUID().equals(var0)) {
                var1.add(var3);
            }
        }

        ServerPlayer var4 = this.playersByUUID.get(param0.getId());
        if (var4 != null && !var1.contains(var4)) {
            var1.add(var4);
        }

        for(ServerPlayer var5 : var1) {
            var5.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.duplicate_login"));
        }

        ServerPlayerGameMode var6;
        if (this.server.isDemo()) {
            var6 = new DemoMode(this.server.getLevel(DimensionType.OVERWORLD));
        } else {
            var6 = new ServerPlayerGameMode(this.server.getLevel(DimensionType.OVERWORLD));
        }

        return new ServerPlayer(this.server, this.server.getLevel(DimensionType.OVERWORLD), param0, var6);
    }

    public ServerPlayer respawn(ServerPlayer param0, boolean param1) {
        this.players.remove(param0);
        param0.getLevel().removePlayerImmediately(param0);
        BlockPos var0 = param0.getRespawnPosition();
        boolean var1 = param0.isRespawnForced();
        Optional<Vec3> var2;
        if (var0 != null) {
            var2 = Player.findRespawnPositionAndUseSpawnBlock(this.server.getLevel(param0.getRespawnDimension()), var0, var1, param1);
        } else {
            var2 = Optional.empty();
        }

        param0.dimension = var2.isPresent() ? param0.getRespawnDimension() : DimensionType.OVERWORLD;
        ServerPlayerGameMode var4;
        if (this.server.isDemo()) {
            var4 = new DemoMode(this.server.getLevel(param0.dimension));
        } else {
            var4 = new ServerPlayerGameMode(this.server.getLevel(param0.dimension));
        }

        ServerPlayer var6 = new ServerPlayer(this.server, this.server.getLevel(param0.dimension), param0.getGameProfile(), var4);
        var6.connection = param0.connection;
        var6.restoreFrom(param0, param1);
        var6.setId(param0.getId());
        var6.setMainArm(param0.getMainArm());

        for(String var7 : param0.getTags()) {
            var6.addTag(var7);
        }

        ServerLevel var8 = this.server.getLevel(param0.dimension);
        this.updatePlayerGameMode(var6, param0, var8);
        boolean var9 = false;
        if (var2.isPresent()) {
            Vec3 var10 = var2.get();
            var6.moveTo(var10.x, var10.y, var10.z, 0.0F, 0.0F);
            var6.setRespawnPosition(param0.dimension, var0, var1, false);
            var9 = !param1 && var8.getBlockState(var0).getBlock() instanceof RespawnAnchorBlock;
        } else if (var0 != null) {
            var6.connection.send(new ClientboundGameEventPacket(0, 0.0F));
        }

        while(!var8.noCollision(var6) && var6.getY() < 256.0) {
            var6.setPos(var6.getX(), var6.getY() + 1.0, var6.getZ());
        }

        LevelData var11 = var6.level.getLevelData();
        var6.connection
            .send(
                new ClientboundRespawnPacket(
                    var6.dimension, LevelData.obfuscateSeed(var11.getSeed()), var11.getGeneratorType(), var6.gameMode.getGameModeForPlayer()
                )
            );
        var6.connection.teleport(var6.getX(), var6.getY(), var6.getZ(), var6.yRot, var6.xRot);
        var6.connection.send(new ClientboundSetDefaultSpawnPositionPacket(var8.getSharedSpawnPos()));
        var6.connection.send(new ClientboundChangeDifficultyPacket(var11.getDifficulty(), var11.isDifficultyLocked()));
        var6.connection.send(new ClientboundSetExperiencePacket(var6.experienceProgress, var6.totalExperience, var6.experienceLevel));
        this.sendLevelInfo(var6, var8);
        this.sendPlayerPermissionLevel(var6);
        var8.addRespawnedPlayer(var6);
        this.players.add(var6);
        this.playersByUUID.put(var6.getUUID(), var6);
        var6.initMenu();
        var6.setHealth(var6.getHealth());
        if (var9) {
            var6.connection
                .send(
                    new ClientboundSoundPacket(
                        SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double)var0.getX(), (double)var0.getY(), (double)var0.getZ(), 1.0F, 1.0F
                    )
                );
        }

        return var6;
    }

    public void sendPlayerPermissionLevel(ServerPlayer param0) {
        GameProfile var0 = param0.getGameProfile();
        int var1 = this.server.getProfilePermissions(var0);
        this.sendPlayerPermissionLevel(param0, var1);
    }

    public void tick() {
        if (++this.sendAllPlayerInfoIn > 600) {
            this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY, this.players));
            this.sendAllPlayerInfoIn = 0;
        }

    }

    public void broadcastAll(Packet<?> param0) {
        for(int var0 = 0; var0 < this.players.size(); ++var0) {
            this.players.get(var0).connection.send(param0);
        }

    }

    public void broadcastAll(Packet<?> param0, DimensionType param1) {
        for(int var0 = 0; var0 < this.players.size(); ++var0) {
            ServerPlayer var1 = this.players.get(var0);
            if (var1.dimension == param1) {
                var1.connection.send(param0);
            }
        }

    }

    public void broadcastToTeam(Player param0, Component param1) {
        Team var0 = param0.getTeam();
        if (var0 != null) {
            for(String var2 : var0.getPlayers()) {
                ServerPlayer var3 = this.getPlayerByName(var2);
                if (var3 != null && var3 != param0) {
                    var3.sendMessage(param1);
                }
            }

        }
    }

    public void broadcastToAllExceptTeam(Player param0, Component param1) {
        Team var0 = param0.getTeam();
        if (var0 == null) {
            this.broadcastMessage(param1);
        } else {
            for(int var1 = 0; var1 < this.players.size(); ++var1) {
                ServerPlayer var2 = this.players.get(var1);
                if (var2.getTeam() != var0) {
                    var2.sendMessage(param1);
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
            || this.server.isSingleplayerOwner(param0) && this.server.getLevel(DimensionType.OVERWORLD).getLevelData().getAllowCommands()
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

    public void broadcast(@Nullable Player param0, double param1, double param2, double param3, double param4, DimensionType param5, Packet<?> param6) {
        for(int var0 = 0; var0 < this.players.size(); ++var0) {
            ServerPlayer var1 = this.players.get(var0);
            if (var1 != param0 && var1.dimension == param5) {
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
        WorldBorder var0 = this.server.getLevel(DimensionType.OVERWORLD).getWorldBorder();
        param0.connection.send(new ClientboundSetBorderPacket(var0, ClientboundSetBorderPacket.Type.INITIALIZE));
        param0.connection
            .send(new ClientboundSetTimePacket(param1.getGameTime(), param1.getDayTime(), param1.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
        param0.connection.send(new ClientboundSetDefaultSpawnPositionPacket(param1.getSharedSpawnPos()));
        if (param1.isRaining()) {
            param0.connection.send(new ClientboundGameEventPacket(1, 0.0F));
            param0.connection.send(new ClientboundGameEventPacket(7, param1.getRainLevel(1.0F)));
            param0.connection.send(new ClientboundGameEventPacket(8, param1.getThunderLevel(1.0F)));
        }

    }

    public void sendAllPlayerInfo(ServerPlayer param0) {
        param0.refreshContainer(param0.inventoryMenu);
        param0.resetSentInfo();
        param0.connection.send(new ClientboundSetCarriedItemPacket(param0.inventory.selected));
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

    public MinecraftServer getServer() {
        return this.server;
    }

    public CompoundTag getSingleplayerData() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public void setOverrideGameMode(GameType param0) {
        this.overrideGameMode = param0;
    }

    private void updatePlayerGameMode(ServerPlayer param0, ServerPlayer param1, LevelAccessor param2) {
        if (param1 != null) {
            param0.gameMode.setGameModeForPlayer(param1.gameMode.getGameModeForPlayer());
        } else if (this.overrideGameMode != null) {
            param0.gameMode.setGameModeForPlayer(this.overrideGameMode);
        }

        param0.gameMode.updateGameMode(param2.getLevelData().getGameType());
    }

    @OnlyIn(Dist.CLIENT)
    public void setAllowCheatsForAllPlayers(boolean param0) {
        this.allowCheatsForAllPlayers = param0;
    }

    public void removeAll() {
        for(int var0 = 0; var0 < this.players.size(); ++var0) {
            this.players.get(var0).connection.disconnect(new TranslatableComponent("multiplayer.disconnect.server_shutdown"));
        }

    }

    public void broadcastMessage(Component param0, boolean param1) {
        this.server.sendMessage(param0);
        ChatType var0 = param1 ? ChatType.SYSTEM : ChatType.CHAT;
        this.broadcastAll(new ClientboundChatPacket(param0, var0));
    }

    public void broadcastMessage(Component param0) {
        this.broadcastMessage(param0, true);
    }

    public ServerStatsCounter getPlayerStats(Player param0) {
        UUID var0 = param0.getUUID();
        ServerStatsCounter var1 = var0 == null ? null : this.stats.get(var0);
        if (var1 == null) {
            File var2 = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
            File var3 = new File(var2, var0 + ".json");
            if (!var3.exists()) {
                File var4 = new File(var2, param0.getName().getString() + ".json");
                if (var4.exists() && var4.isFile()) {
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
            File var2 = this.server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).toFile();
            File var3 = new File(var2, var0 + ".json");
            var1 = new PlayerAdvancements(this.server, var3, param0);
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
            var0.reload();
        }

        this.broadcastAll(new ClientboundUpdateTagsPacket(this.server.getTags()));
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
