package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLoginPacket implements Packet<ClientGamePacketListener> {
    private int playerId;
    private long seed;
    private boolean hardcore;
    private GameType gameType;
    private Set<ResourceKey<Level>> levels;
    private RegistryAccess.RegistryHolder registryHolder;
    private ResourceKey<DimensionType> dimensionType;
    private ResourceKey<Level> dimension;
    private int maxPlayers;
    private int chunkRadius;
    private boolean reducedDebugInfo;
    private boolean showDeathScreen;
    private boolean isDebug;
    private boolean isFlat;

    public ClientboundLoginPacket() {
    }

    public ClientboundLoginPacket(
        int param0,
        GameType param1,
        long param2,
        boolean param3,
        Set<ResourceKey<Level>> param4,
        RegistryAccess.RegistryHolder param5,
        ResourceKey<DimensionType> param6,
        ResourceKey<Level> param7,
        int param8,
        int param9,
        boolean param10,
        boolean param11,
        boolean param12,
        boolean param13
    ) {
        this.playerId = param0;
        this.levels = param4;
        this.registryHolder = param5;
        this.dimensionType = param6;
        this.dimension = param7;
        this.seed = param2;
        this.gameType = param1;
        this.maxPlayers = param8;
        this.hardcore = param3;
        this.chunkRadius = param9;
        this.reducedDebugInfo = param10;
        this.showDeathScreen = param11;
        this.isDebug = param12;
        this.isFlat = param13;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.playerId = param0.readInt();
        int var0 = param0.readUnsignedByte();
        this.hardcore = (var0 & 8) == 8;
        var0 &= -9;
        this.gameType = GameType.byId(var0);
        int var1 = param0.readVarInt();
        this.levels = Sets.newHashSet();

        for(int var2 = 0; var2 < var1; ++var2) {
            this.levels.add(ResourceKey.create(Registry.DIMENSION_REGISTRY, param0.readResourceLocation()));
        }

        this.registryHolder = param0.readWithCodec(RegistryAccess.RegistryHolder.CODEC);
        this.dimensionType = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, param0.readResourceLocation());
        this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, param0.readResourceLocation());
        this.seed = param0.readLong();
        this.maxPlayers = param0.readUnsignedByte();
        this.chunkRadius = param0.readVarInt();
        this.reducedDebugInfo = param0.readBoolean();
        this.showDeathScreen = param0.readBoolean();
        this.isDebug = param0.readBoolean();
        this.isFlat = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeInt(this.playerId);
        int var0 = this.gameType.getId();
        if (this.hardcore) {
            var0 |= 8;
        }

        param0.writeByte(var0);
        param0.writeVarInt(this.levels.size());

        for(ResourceKey<Level> var1 : this.levels) {
            param0.writeResourceLocation(var1.location());
        }

        param0.writeWithCodec(RegistryAccess.RegistryHolder.CODEC, this.registryHolder);
        param0.writeResourceLocation(this.dimensionType.location());
        param0.writeResourceLocation(this.dimension.location());
        param0.writeLong(this.seed);
        param0.writeByte(this.maxPlayers);
        param0.writeVarInt(this.chunkRadius);
        param0.writeBoolean(this.reducedDebugInfo);
        param0.writeBoolean(this.showDeathScreen);
        param0.writeBoolean(this.isDebug);
        param0.writeBoolean(this.isFlat);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLogin(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getPlayerId() {
        return this.playerId;
    }

    @OnlyIn(Dist.CLIENT)
    public long getSeed() {
        return this.seed;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isHardcore() {
        return this.hardcore;
    }

    @OnlyIn(Dist.CLIENT)
    public GameType getGameType() {
        return this.gameType;
    }

    @OnlyIn(Dist.CLIENT)
    public Set<ResourceKey<Level>> levels() {
        return this.levels;
    }

    @OnlyIn(Dist.CLIENT)
    public RegistryAccess registryAccess() {
        return this.registryHolder;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceKey<DimensionType> getDimensionType() {
        return this.dimensionType;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    @OnlyIn(Dist.CLIENT)
    public int getChunkRadius() {
        return this.chunkRadius;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldShowDeathScreen() {
        return this.showDeathScreen;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isDebug() {
        return this.isDebug;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFlat() {
        return this.isFlat;
    }
}
