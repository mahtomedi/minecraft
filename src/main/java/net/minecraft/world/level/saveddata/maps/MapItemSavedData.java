package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Dynamic;
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
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapItemSavedData extends SavedData {
    private static final Logger LOGGER = LogManager.getLogger();
    public int x;
    public int z;
    public ResourceKey<DimensionType> dimension;
    public boolean trackingPosition;
    public boolean unlimitedTracking;
    public byte scale;
    public byte[] colors = new byte[16384];
    public boolean locked;
    public final List<MapItemSavedData.HoldingPlayer> carriedBy = Lists.newArrayList();
    private final Map<Player, MapItemSavedData.HoldingPlayer> carriedByPlayers = Maps.newHashMap();
    private final Map<String, MapBanner> bannerMarkers = Maps.newHashMap();
    public final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
    private final Map<String, MapFrame> frameMarkers = Maps.newHashMap();

    public MapItemSavedData(String param0) {
        super(param0);
    }

    public void setProperties(int param0, int param1, int param2, boolean param3, boolean param4, ResourceKey<DimensionType> param5) {
        this.scale = (byte)param2;
        this.setOrigin((double)param0, (double)param1, this.scale);
        this.dimension = param5;
        this.trackingPosition = param3;
        this.unlimitedTracking = param4;
        this.setDirty();
    }

    public void setOrigin(double param0, double param1, int param2) {
        int var0 = 128 * (1 << param2);
        int var1 = Mth.floor((param0 + 64.0) / (double)var0);
        int var2 = Mth.floor((param1 + 64.0) / (double)var0);
        this.x = var1 * var0 + var0 / 2 - 64;
        this.z = var2 * var0 + var0 / 2 - 64;
    }

    @Override
    public void load(CompoundTag param0) {
        this.dimension = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, param0.get("dimension")))
            .resultOrPartial(LOGGER::error)
            .orElseThrow(() -> new IllegalArgumentException("Invalid map dimension: " + param0.get("dimension")));
        this.x = param0.getInt("xCenter");
        this.z = param0.getInt("zCenter");
        this.scale = (byte)Mth.clamp(param0.getByte("scale"), 0, 4);
        this.trackingPosition = !param0.contains("trackingPosition", 1) || param0.getBoolean("trackingPosition");
        this.unlimitedTracking = param0.getBoolean("unlimitedTracking");
        this.locked = param0.getBoolean("locked");
        this.colors = param0.getByteArray("colors");
        if (this.colors.length != 16384) {
            this.colors = new byte[16384];
        }

        ListTag var0 = param0.getList("banners", 10);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            MapBanner var2 = MapBanner.load(var0.getCompound(var1));
            this.bannerMarkers.put(var2.getId(), var2);
            this.addDecoration(var2.getDecoration(), null, var2.getId(), (double)var2.getPos().getX(), (double)var2.getPos().getZ(), 180.0, var2.getName());
        }

        ListTag var3 = param0.getList("frames", 10);

        for(int var4 = 0; var4 < var3.size(); ++var4) {
            MapFrame var5 = MapFrame.load(var3.getCompound(var4));
            this.frameMarkers.put(var5.getId(), var5);
            this.addDecoration(
                MapDecoration.Type.FRAME,
                null,
                "frame-" + var5.getEntityId(),
                (double)var5.getPos().getX(),
                (double)var5.getPos().getZ(),
                (double)var5.getRotation(),
                null
            );
        }

    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        ResourceLocation.CODEC
            .encodeStart(NbtOps.INSTANCE, this.dimension.location())
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("dimension", param1));
        param0.putInt("xCenter", this.x);
        param0.putInt("zCenter", this.z);
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

    public void lockData(MapItemSavedData param0) {
        this.locked = true;
        this.x = param0.x;
        this.z = param0.z;
        this.bannerMarkers.putAll(param0.bannerMarkers);
        this.decorations.putAll(param0.decorations);
        System.arraycopy(param0.colors, 0, this.colors, 0, param0.colors.length);
        this.setDirty();
    }

    public void tickCarriedBy(Player param0, ItemStack param1) {
        if (!this.carriedByPlayers.containsKey(param0)) {
            MapItemSavedData.HoldingPlayer var0 = new MapItemSavedData.HoldingPlayer(param0);
            this.carriedByPlayers.put(param0, var0);
            this.carriedBy.add(var0);
        }

        if (!param0.inventory.contains(param1)) {
            this.decorations.remove(param0.getName().getString());
        }

        for(int var1 = 0; var1 < this.carriedBy.size(); ++var1) {
            MapItemSavedData.HoldingPlayer var2 = this.carriedBy.get(var1);
            String var3 = var2.player.getName().getString();
            if (!var2.player.removed && (var2.player.inventory.contains(param1) || param1.isFramed())) {
                if (!param1.isFramed() && var2.player.level.dimension() == this.dimension && this.trackingPosition) {
                    this.addDecoration(
                        MapDecoration.Type.PLAYER, var2.player.level, var3, var2.player.getX(), var2.player.getZ(), (double)var2.player.yRot, null
                    );
                }
            } else {
                this.carriedByPlayers.remove(var2.player);
                this.carriedBy.remove(var2);
                this.decorations.remove(var3);
            }
        }

        if (param1.isFramed() && this.trackingPosition) {
            ItemFrame var4 = param1.getFrame();
            BlockPos var5 = var4.getPos();
            MapFrame var6 = this.frameMarkers.get(MapFrame.frameId(var5));
            if (var6 != null && var4.getId() != var6.getEntityId() && this.frameMarkers.containsKey(var6.getId())) {
                this.decorations.remove("frame-" + var6.getEntityId());
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
        float var1 = (float)(param3 - (double)this.x) / (float)var0;
        float var2 = (float)(param4 - (double)this.z) / (float)var0;
        byte var3 = (byte)((int)((double)(var1 * 2.0F) + 0.5));
        byte var4 = (byte)((int)((double)(var2 * 2.0F) + 0.5));
        int var5 = 63;
        byte var6;
        if (var1 >= -63.0F && var2 >= -63.0F && var1 <= 63.0F && var2 <= 63.0F) {
            param5 += param5 < 0.0 ? -8.0 : 8.0;
            var6 = (byte)((int)(param5 * 16.0 / 360.0));
            if (this.dimension == DimensionType.NETHER_LOCATION && param1 != null) {
                int var7 = (int)(param1.getLevelData().getDayTime() / 10L);
                var6 = (byte)(var7 * var7 * 34187121 + var7 * 121 >> 15 & 15);
            }
        } else {
            if (param0 != MapDecoration.Type.PLAYER) {
                this.decorations.remove(param2);
                return;
            }

            int var8 = 320;
            if (Math.abs(var1) < 320.0F && Math.abs(var2) < 320.0F) {
                param0 = MapDecoration.Type.PLAYER_OFF_MAP;
            } else {
                if (!this.unlimitedTracking) {
                    this.decorations.remove(param2);
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

        this.decorations.put(param2, new MapDecoration(param0, var3, var4, var6, param6));
    }

    @Nullable
    public Packet<?> getUpdatePacket(ItemStack param0, BlockGetter param1, Player param2) {
        MapItemSavedData.HoldingPlayer var0 = this.carriedByPlayers.get(param2);
        return var0 == null ? null : var0.nextUpdatePacket(param0);
    }

    public void setDirty(int param0, int param1) {
        this.setDirty();

        for(MapItemSavedData.HoldingPlayer var0 : this.carriedBy) {
            var0.markDirty(param0, param1);
        }

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

    public void toggleBanner(LevelAccessor param0, BlockPos param1) {
        float var0 = (float)param1.getX() + 0.5F;
        float var1 = (float)param1.getZ() + 0.5F;
        int var2 = 1 << this.scale;
        float var3 = (var0 - (float)this.x) / (float)var2;
        float var4 = (var1 - (float)this.z) / (float)var2;
        int var5 = 63;
        boolean var6 = false;
        if (var3 >= -63.0F && var4 >= -63.0F && var3 <= 63.0F && var4 <= 63.0F) {
            MapBanner var7 = MapBanner.fromWorld(param0, param1);
            if (var7 == null) {
                return;
            }

            boolean var8 = true;
            if (this.bannerMarkers.containsKey(var7.getId()) && this.bannerMarkers.get(var7.getId()).equals(var7)) {
                this.bannerMarkers.remove(var7.getId());
                this.decorations.remove(var7.getId());
                var8 = false;
                var6 = true;
            }

            if (var8) {
                this.bannerMarkers.put(var7.getId(), var7);
                this.addDecoration(var7.getDecoration(), param0, var7.getId(), (double)var0, (double)var1, 180.0, var7.getName());
                var6 = true;
            }

            if (var6) {
                this.setDirty();
            }
        }

    }

    public void checkBanners(BlockGetter param0, int param1, int param2) {
        Iterator<MapBanner> var0 = this.bannerMarkers.values().iterator();

        while(var0.hasNext()) {
            MapBanner var1 = var0.next();
            if (var1.getPos().getX() == param1 && var1.getPos().getZ() == param2) {
                MapBanner var2 = MapBanner.fromWorld(param0, var1.getPos());
                if (!var1.equals(var2)) {
                    var0.remove();
                    this.decorations.remove(var1.getId());
                }
            }
        }

    }

    public void removedFromFrame(BlockPos param0, int param1) {
        this.decorations.remove("frame-" + param1);
        this.frameMarkers.remove(MapFrame.frameId(param0));
    }

    public class HoldingPlayer {
        public final Player player;
        private boolean dirtyData = true;
        private int minDirtyX;
        private int minDirtyY;
        private int maxDirtyX = 127;
        private int maxDirtyY = 127;
        private int tick;
        public int step;

        public HoldingPlayer(Player param1) {
            this.player = param1;
        }

        @Nullable
        public Packet<?> nextUpdatePacket(ItemStack param0) {
            if (this.dirtyData) {
                this.dirtyData = false;
                return new ClientboundMapItemDataPacket(
                    MapItem.getMapId(param0),
                    MapItemSavedData.this.scale,
                    MapItemSavedData.this.trackingPosition,
                    MapItemSavedData.this.locked,
                    MapItemSavedData.this.decorations.values(),
                    MapItemSavedData.this.colors,
                    this.minDirtyX,
                    this.minDirtyY,
                    this.maxDirtyX + 1 - this.minDirtyX,
                    this.maxDirtyY + 1 - this.minDirtyY
                );
            } else {
                return this.tick++ % 5 == 0
                    ? new ClientboundMapItemDataPacket(
                        MapItem.getMapId(param0),
                        MapItemSavedData.this.scale,
                        MapItemSavedData.this.trackingPosition,
                        MapItemSavedData.this.locked,
                        MapItemSavedData.this.decorations.values(),
                        MapItemSavedData.this.colors,
                        0,
                        0,
                        0,
                        0
                    )
                    : null;
            }
        }

        public void markDirty(int param0, int param1) {
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
    }
}
