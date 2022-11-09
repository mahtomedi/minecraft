package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class MapItemSavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAP_SIZE = 128;
    private static final int HALF_MAP_SIZE = 64;
    public static final int MAX_SCALE = 4;
    public static final int TRACKED_DECORATION_LIMIT = 256;
    public final int centerX;
    public final int centerZ;
    public final ResourceKey<Level> dimension;
    private final boolean trackingPosition;
    private final boolean unlimitedTracking;
    public final byte scale;
    public byte[] colors = new byte[16384];
    public final boolean locked;
    private final List<MapItemSavedData.HoldingPlayer> carriedBy = Lists.newArrayList();
    private final Map<Player, MapItemSavedData.HoldingPlayer> carriedByPlayers = Maps.newHashMap();
    private final Map<String, MapBanner> bannerMarkers = Maps.newHashMap();
    final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
    private final Map<String, MapFrame> frameMarkers = Maps.newHashMap();
    private int trackedDecorationCount;

    private MapItemSavedData(int param0, int param1, byte param2, boolean param3, boolean param4, boolean param5, ResourceKey<Level> param6) {
        this.scale = param2;
        this.centerX = param0;
        this.centerZ = param1;
        this.dimension = param6;
        this.trackingPosition = param3;
        this.unlimitedTracking = param4;
        this.locked = param5;
        this.setDirty();
    }

    public static MapItemSavedData createFresh(double param0, double param1, byte param2, boolean param3, boolean param4, ResourceKey<Level> param5) {
        int var0 = 128 * (1 << param2);
        int var1 = Mth.floor((param0 + 64.0) / (double)var0);
        int var2 = Mth.floor((param1 + 64.0) / (double)var0);
        int var3 = var1 * var0 + var0 / 2 - 64;
        int var4 = var2 * var0 + var0 / 2 - 64;
        return new MapItemSavedData(var3, var4, param2, param3, param4, false, param5);
    }

    public static MapItemSavedData createForClient(byte param0, boolean param1, ResourceKey<Level> param2) {
        return new MapItemSavedData(0, 0, param0, false, false, param1, param2);
    }

    public static MapItemSavedData load(CompoundTag param0) {
        ResourceKey<Level> var0 = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, param0.get("dimension")))
            .resultOrPartial(LOGGER::error)
            .orElseThrow(() -> new IllegalArgumentException("Invalid map dimension: " + param0.get("dimension")));
        int var1 = param0.getInt("xCenter");
        int var2 = param0.getInt("zCenter");
        byte var3 = (byte)Mth.clamp(param0.getByte("scale"), 0, 4);
        boolean var4 = !param0.contains("trackingPosition", 1) || param0.getBoolean("trackingPosition");
        boolean var5 = param0.getBoolean("unlimitedTracking");
        boolean var6 = param0.getBoolean("locked");
        MapItemSavedData var7 = new MapItemSavedData(var1, var2, var3, var4, var5, var6, var0);
        byte[] var8 = param0.getByteArray("colors");
        if (var8.length == 16384) {
            var7.colors = var8;
        }

        ListTag var9 = param0.getList("banners", 10);

        for(int var10 = 0; var10 < var9.size(); ++var10) {
            MapBanner var11 = MapBanner.load(var9.getCompound(var10));
            var7.bannerMarkers.put(var11.getId(), var11);
            var7.addDecoration(var11.getDecoration(), null, var11.getId(), (double)var11.getPos().getX(), (double)var11.getPos().getZ(), 180.0, var11.getName());
        }

        ListTag var12 = param0.getList("frames", 10);

        for(int var13 = 0; var13 < var12.size(); ++var13) {
            MapFrame var14 = MapFrame.load(var12.getCompound(var13));
            var7.frameMarkers.put(var14.getId(), var14);
            var7.addDecoration(
                MapDecoration.Type.FRAME,
                null,
                "frame-" + var14.getEntityId(),
                (double)var14.getPos().getX(),
                (double)var14.getPos().getZ(),
                (double)var14.getRotation(),
                null
            );
        }

        return var7;
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        ResourceLocation.CODEC
            .encodeStart(NbtOps.INSTANCE, this.dimension.location())
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("dimension", param1));
        param0.putInt("xCenter", this.centerX);
        param0.putInt("zCenter", this.centerZ);
        param0.putByte("scale", this.scale);
        param0.putByteArray("colors", this.colors);
        param0.putBoolean("trackingPosition", this.trackingPosition);
        param0.putBoolean("unlimitedTracking", this.unlimitedTracking);
        param0.putBoolean("locked", this.locked);
        ListTag var0 = new ListTag();

        for(MapBanner var1 : this.bannerMarkers.values()) {
            var0.add(var1.save());
        }

        param0.put("banners", var0);
        ListTag var2 = new ListTag();

        for(MapFrame var3 : this.frameMarkers.values()) {
            var2.add(var3.save());
        }

        param0.put("frames", var2);
        return param0;
    }

    public MapItemSavedData locked() {
        MapItemSavedData var0 = new MapItemSavedData(
            this.centerX, this.centerZ, this.scale, this.trackingPosition, this.unlimitedTracking, true, this.dimension
        );
        var0.bannerMarkers.putAll(this.bannerMarkers);
        var0.decorations.putAll(this.decorations);
        var0.trackedDecorationCount = this.trackedDecorationCount;
        System.arraycopy(this.colors, 0, var0.colors, 0, this.colors.length);
        var0.setDirty();
        return var0;
    }

    public MapItemSavedData scaled(int param0) {
        return createFresh(
            (double)this.centerX,
            (double)this.centerZ,
            (byte)Mth.clamp(this.scale + param0, 0, 4),
            this.trackingPosition,
            this.unlimitedTracking,
            this.dimension
        );
    }

    public void tickCarriedBy(Player param0, ItemStack param1) {
        if (!this.carriedByPlayers.containsKey(param0)) {
            MapItemSavedData.HoldingPlayer var0 = new MapItemSavedData.HoldingPlayer(param0);
            this.carriedByPlayers.put(param0, var0);
            this.carriedBy.add(var0);
        }

        if (!param0.getInventory().contains(param1)) {
            this.removeDecoration(param0.getName().getString());
        }

        for(int var1 = 0; var1 < this.carriedBy.size(); ++var1) {
            MapItemSavedData.HoldingPlayer var2 = this.carriedBy.get(var1);
            String var3 = var2.player.getName().getString();
            if (!var2.player.isRemoved() && (var2.player.getInventory().contains(param1) || param1.isFramed())) {
                if (!param1.isFramed() && var2.player.level.dimension() == this.dimension && this.trackingPosition) {
                    this.addDecoration(
                        MapDecoration.Type.PLAYER, var2.player.level, var3, var2.player.getX(), var2.player.getZ(), (double)var2.player.getYRot(), null
                    );
                }
            } else {
                this.carriedByPlayers.remove(var2.player);
                this.carriedBy.remove(var2);
                this.removeDecoration(var3);
            }
        }

        if (param1.isFramed() && this.trackingPosition) {
            ItemFrame var4 = param1.getFrame();
            BlockPos var5 = var4.getPos();
            MapFrame var6 = this.frameMarkers.get(MapFrame.frameId(var5));
            if (var6 != null && var4.getId() != var6.getEntityId() && this.frameMarkers.containsKey(var6.getId())) {
                this.removeDecoration("frame-" + var6.getEntityId());
            }

            MapFrame var7 = new MapFrame(var5, var4.getDirection().get2DDataValue() * 90, var4.getId());
            this.addDecoration(
                MapDecoration.Type.FRAME,
                param0.level,
                "frame-" + var4.getId(),
                (double)var5.getX(),
                (double)var5.getZ(),
                (double)(var4.getDirection().get2DDataValue() * 90),
                null
            );
            this.frameMarkers.put(var7.getId(), var7);
        }

        CompoundTag var8 = param1.getTag();
        if (var8 != null && var8.contains("Decorations", 9)) {
            ListTag var9 = var8.getList("Decorations", 10);

            for(int var10 = 0; var10 < var9.size(); ++var10) {
                CompoundTag var11 = var9.getCompound(var10);
                if (!this.decorations.containsKey(var11.getString("id"))) {
                    this.addDecoration(
                        MapDecoration.Type.byIcon(var11.getByte("type")),
                        param0.level,
                        var11.getString("id"),
                        var11.getDouble("x"),
                        var11.getDouble("z"),
                        var11.getDouble("rot"),
                        null
                    );
                }
            }
        }

    }

    private void removeDecoration(String param0) {
        MapDecoration var0 = this.decorations.remove(param0);
        if (var0 != null && var0.getType().shouldTrackCount()) {
            --this.trackedDecorationCount;
        }

        this.setDecorationsDirty();
    }

    public static void addTargetDecoration(ItemStack param0, BlockPos param1, String param2, MapDecoration.Type param3) {
        ListTag var0;
        if (param0.hasTag() && param0.getTag().contains("Decorations", 9)) {
            var0 = param0.getTag().getList("Decorations", 10);
        } else {
            var0 = new ListTag();
            param0.addTagElement("Decorations", var0);
        }

        CompoundTag var2 = new CompoundTag();
        var2.putByte("type", param3.getIcon());
        var2.putString("id", param2);
        var2.putDouble("x", (double)param1.getX());
        var2.putDouble("z", (double)param1.getZ());
        var2.putDouble("rot", 180.0);
        var0.add(var2);
        if (param3.hasMapColor()) {
            CompoundTag var3 = param0.getOrCreateTagElement("display");
            var3.putInt("MapColor", param3.getMapColor());
        }

    }

    private void addDecoration(
        MapDecoration.Type param0, @Nullable LevelAccessor param1, String param2, double param3, double param4, double param5, @Nullable Component param6
    ) {
        int var0 = 1 << this.scale;
        float var1 = (float)(param3 - (double)this.centerX) / (float)var0;
        float var2 = (float)(param4 - (double)this.centerZ) / (float)var0;
        byte var3 = (byte)((int)((double)(var1 * 2.0F) + 0.5));
        byte var4 = (byte)((int)((double)(var2 * 2.0F) + 0.5));
        int var5 = 63;
        byte var6;
        if (var1 >= -63.0F && var2 >= -63.0F && var1 <= 63.0F && var2 <= 63.0F) {
            param5 += param5 < 0.0 ? -8.0 : 8.0;
            var6 = (byte)((int)(param5 * 16.0 / 360.0));
            if (this.dimension == Level.NETHER && param1 != null) {
                int var7 = (int)(param1.getLevelData().getDayTime() / 10L);
                var6 = (byte)(var7 * var7 * 34187121 + var7 * 121 >> 15 & 15);
            }
        } else {
            if (param0 != MapDecoration.Type.PLAYER) {
                this.removeDecoration(param2);
                return;
            }

            int var8 = 320;
            if (Math.abs(var1) < 320.0F && Math.abs(var2) < 320.0F) {
                param0 = MapDecoration.Type.PLAYER_OFF_MAP;
            } else {
                if (!this.unlimitedTracking) {
                    this.removeDecoration(param2);
                    return;
                }

                param0 = MapDecoration.Type.PLAYER_OFF_LIMITS;
            }

            var6 = 0;
            if (var1 <= -63.0F) {
                var3 = -128;
            }

            if (var2 <= -63.0F) {
                var4 = -128;
            }

            if (var1 >= 63.0F) {
                var3 = 127;
            }

            if (var2 >= 63.0F) {
                var4 = 127;
            }
        }

        MapDecoration var11 = new MapDecoration(param0, var3, var4, var6, param6);
        MapDecoration var12 = this.decorations.put(param2, var11);
        if (!var11.equals(var12)) {
            if (var12 != null && var12.getType().shouldTrackCount()) {
                --this.trackedDecorationCount;
            }

            if (param0.shouldTrackCount()) {
                ++this.trackedDecorationCount;
            }

            this.setDecorationsDirty();
        }

    }

    @Nullable
    public Packet<?> getUpdatePacket(int param0, Player param1) {
        MapItemSavedData.HoldingPlayer var0 = this.carriedByPlayers.get(param1);
        return var0 == null ? null : var0.nextUpdatePacket(param0);
    }

    private void setColorsDirty(int param0, int param1) {
        this.setDirty();

        for(MapItemSavedData.HoldingPlayer var0 : this.carriedBy) {
            var0.markColorsDirty(param0, param1);
        }

    }

    private void setDecorationsDirty() {
        this.setDirty();
        this.carriedBy.forEach(MapItemSavedData.HoldingPlayer::markDecorationsDirty);
    }

    public MapItemSavedData.HoldingPlayer getHoldingPlayer(Player param0) {
        MapItemSavedData.HoldingPlayer var0 = this.carriedByPlayers.get(param0);
        if (var0 == null) {
            var0 = new MapItemSavedData.HoldingPlayer(param0);
            this.carriedByPlayers.put(param0, var0);
            this.carriedBy.add(var0);
        }

        return var0;
    }

    public boolean toggleBanner(LevelAccessor param0, BlockPos param1) {
        double var0 = (double)param1.getX() + 0.5;
        double var1 = (double)param1.getZ() + 0.5;
        int var2 = 1 << this.scale;
        double var3 = (var0 - (double)this.centerX) / (double)var2;
        double var4 = (var1 - (double)this.centerZ) / (double)var2;
        int var5 = 63;
        if (var3 >= -63.0 && var4 >= -63.0 && var3 <= 63.0 && var4 <= 63.0) {
            MapBanner var6 = MapBanner.fromWorld(param0, param1);
            if (var6 == null) {
                return false;
            }

            if (this.bannerMarkers.remove(var6.getId(), var6)) {
                this.removeDecoration(var6.getId());
                return true;
            }

            if (!this.isTrackedCountOverLimit(256)) {
                this.bannerMarkers.put(var6.getId(), var6);
                this.addDecoration(var6.getDecoration(), param0, var6.getId(), var0, var1, 180.0, var6.getName());
                return true;
            }
        }

        return false;
    }

    public void checkBanners(BlockGetter param0, int param1, int param2) {
        Iterator<MapBanner> var0 = this.bannerMarkers.values().iterator();

        while(var0.hasNext()) {
            MapBanner var1 = var0.next();
            if (var1.getPos().getX() == param1 && var1.getPos().getZ() == param2) {
                MapBanner var2 = MapBanner.fromWorld(param0, var1.getPos());
                if (!var1.equals(var2)) {
                    var0.remove();
                    this.removeDecoration(var1.getId());
                }
            }
        }

    }

    public Collection<MapBanner> getBanners() {
        return this.bannerMarkers.values();
    }

    public void removedFromFrame(BlockPos param0, int param1) {
        this.removeDecoration("frame-" + param1);
        this.frameMarkers.remove(MapFrame.frameId(param0));
    }

    public boolean updateColor(int param0, int param1, byte param2) {
        byte var0 = this.colors[param0 + param1 * 128];
        if (var0 != param2) {
            this.setColor(param0, param1, param2);
            return true;
        } else {
            return false;
        }
    }

    public void setColor(int param0, int param1, byte param2) {
        this.colors[param0 + param1 * 128] = param2;
        this.setColorsDirty(param0, param1);
    }

    public boolean isExplorationMap() {
        for(MapDecoration var0 : this.decorations.values()) {
            if (var0.getType() == MapDecoration.Type.MANSION || var0.getType() == MapDecoration.Type.MONUMENT) {
                return true;
            }
        }

        return false;
    }

    public void addClientSideDecorations(List<MapDecoration> param0) {
        this.decorations.clear();
        this.trackedDecorationCount = 0;

        for(int var0 = 0; var0 < param0.size(); ++var0) {
            MapDecoration var1 = param0.get(var0);
            this.decorations.put("icon-" + var0, var1);
            if (var1.getType().shouldTrackCount()) {
                ++this.trackedDecorationCount;
            }
        }

    }

    public Iterable<MapDecoration> getDecorations() {
        return this.decorations.values();
    }

    public boolean isTrackedCountOverLimit(int param0) {
        return this.trackedDecorationCount >= param0;
    }

    public class HoldingPlayer {
        public final Player player;
        private boolean dirtyData = true;
        private int minDirtyX;
        private int minDirtyY;
        private int maxDirtyX = 127;
        private int maxDirtyY = 127;
        private boolean dirtyDecorations = true;
        private int tick;
        public int step;

        HoldingPlayer(Player param1) {
            this.player = param1;
        }

        private MapItemSavedData.MapPatch createPatch() {
            int var0 = this.minDirtyX;
            int var1 = this.minDirtyY;
            int var2 = this.maxDirtyX + 1 - this.minDirtyX;
            int var3 = this.maxDirtyY + 1 - this.minDirtyY;
            byte[] var4 = new byte[var2 * var3];

            for(int var5 = 0; var5 < var2; ++var5) {
                for(int var6 = 0; var6 < var3; ++var6) {
                    var4[var5 + var6 * var2] = MapItemSavedData.this.colors[var0 + var5 + (var1 + var6) * 128];
                }
            }

            return new MapItemSavedData.MapPatch(var0, var1, var2, var3, var4);
        }

        @Nullable
        Packet<?> nextUpdatePacket(int param0) {
            MapItemSavedData.MapPatch var0;
            if (this.dirtyData) {
                this.dirtyData = false;
                var0 = this.createPatch();
            } else {
                var0 = null;
            }

            Collection<MapDecoration> var2;
            if (this.dirtyDecorations && this.tick++ % 5 == 0) {
                this.dirtyDecorations = false;
                var2 = MapItemSavedData.this.decorations.values();
            } else {
                var2 = null;
            }

            return var2 == null && var0 == null
                ? null
                : new ClientboundMapItemDataPacket(param0, MapItemSavedData.this.scale, MapItemSavedData.this.locked, var2, var0);
        }

        void markColorsDirty(int param0, int param1) {
            if (this.dirtyData) {
                this.minDirtyX = Math.min(this.minDirtyX, param0);
                this.minDirtyY = Math.min(this.minDirtyY, param1);
                this.maxDirtyX = Math.max(this.maxDirtyX, param0);
                this.maxDirtyY = Math.max(this.maxDirtyY, param1);
            } else {
                this.dirtyData = true;
                this.minDirtyX = param0;
                this.minDirtyY = param1;
                this.maxDirtyX = param0;
                this.maxDirtyY = param1;
            }

        }

        private void markDecorationsDirty() {
            this.dirtyDecorations = true;
        }
    }

    public static class MapPatch {
        public final int startX;
        public final int startY;
        public final int width;
        public final int height;
        public final byte[] mapColors;

        public MapPatch(int param0, int param1, int param2, int param3, byte[] param4) {
            this.startX = param0;
            this.startY = param1;
            this.width = param2;
            this.height = param3;
            this.mapColors = param4;
        }

        public void applyToMap(MapItemSavedData param0) {
            for(int var0 = 0; var0 < this.width; ++var0) {
                for(int var1 = 0; var1 < this.height; ++var1) {
                    param0.setColor(this.startX + var0, this.startY + var1, this.mapColors[var0 + var1 * this.width]);
                }
            }

        }
    }
}
