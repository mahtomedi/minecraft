package net.minecraft.stats;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerStatsCounter extends StatsCounter {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int STAT_REQUEST_COOLDOWN = 300;
    private final MinecraftServer server;
    private final File file;
    private final Set<Stat<?>> dirty = Sets.newHashSet();
    private int lastStatRequest = -300;

    public ServerStatsCounter(MinecraftServer param0, File param1) {
        this.server = param0;
        this.file = param1;
        if (param1.isFile()) {
            try {
                this.parseLocal(param0.getFixerUpper(), FileUtils.readFileToString(param1));
            } catch (IOException var4) {
                LOGGER.error("Couldn't read statistics file {}", param1, var4);
            } catch (JsonParseException var5) {
                LOGGER.error("Couldn't parse statistics file {}", param1, var5);
            }
        }

    }

    public void save() {
        try {
            FileUtils.writeStringToFile(this.file, this.toJson());
        } catch (IOException var2) {
            LOGGER.error("Couldn't save stats", (Throwable)var2);
        }

    }

    @Override
    public void setValue(Player param0, Stat<?> param1, int param2) {
        super.setValue(param0, param1, param2);
        this.dirty.add(param1);
    }

    private Set<Stat<?>> getDirty() {
        Set<Stat<?>> var0 = Sets.newHashSet(this.dirty);
        this.dirty.clear();
        return var0;
    }

    public void parseLocal(DataFixer param0, String param1) {
        try (JsonReader var0 = new JsonReader(new StringReader(param1))) {
            var0.setLenient(false);
            JsonElement var1 = Streams.parse(var0);
            if (var1.isJsonNull()) {
                LOGGER.error("Unable to parse Stat data from {}", this.file);
                return;
            }

            CompoundTag var2 = fromJson(var1.getAsJsonObject());
            if (!var2.contains("DataVersion", 99)) {
                var2.putInt("DataVersion", 1343);
            }

            var2 = NbtUtils.update(param0, DataFixTypes.STATS, var2, var2.getInt("DataVersion"));
            if (var2.contains("stats", 10)) {
                CompoundTag var3 = var2.getCompound("stats");

                for(String var4 : var3.getAllKeys()) {
                    if (var3.contains(var4, 10)) {
                        Util.ifElse(
                            Registry.STAT_TYPE.getOptional(new ResourceLocation(var4)),
                            param2 -> {
                                CompoundTag var0x = var3.getCompound(var4);
    
                                for(String var1x : var0x.getAllKeys()) {
                                    if (var0x.contains(var1x, 99)) {
                                        Util.ifElse(
                                            this.getStat(param2, var1x),
                                            param2x -> this.stats.put(param2x, var0x.getInt(var1x)),
                                            () -> LOGGER.warn("Invalid statistic in {}: Don't know what {} is", this.file, var1x)
                                        );
                                    } else {
                                        LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.file, var0x.get(var1x), var1x);
                                    }
                                }
    
                            },
                            () -> LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", this.file, var4)
                        );
                    }
                }
            }
        } catch (IOException | JsonParseException var21) {
            LOGGER.error("Unable to parse Stat data from {}", this.file, var21);
        }

    }

    private <T> Optional<Stat<T>> getStat(StatType<T> param0, String param1) {
        return Optional.ofNullable(ResourceLocation.tryParse(param1)).flatMap(param0.getRegistry()::getOptional).map(param0::get);
    }

    private static CompoundTag fromJson(JsonObject param0) {
        CompoundTag var0 = new CompoundTag();

        for(Entry<String, JsonElement> var1 : param0.entrySet()) {
            JsonElement var2 = var1.getValue();
            if (var2.isJsonObject()) {
                var0.put(var1.getKey(), fromJson(var2.getAsJsonObject()));
            } else if (var2.isJsonPrimitive()) {
                JsonPrimitive var3 = var2.getAsJsonPrimitive();
                if (var3.isNumber()) {
                    var0.putInt(var1.getKey(), var3.getAsInt());
                }
            }
        }

        return var0;
    }

    protected String toJson() {
        Map<StatType<?>, JsonObject> var0 = Maps.newHashMap();

        for(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Stat<?>> var1 : this.stats.object2IntEntrySet()) {
            Stat<?> var2 = var1.getKey();
            var0.computeIfAbsent(var2.getType(), param0 -> new JsonObject()).addProperty(getKey(var2).toString(), var1.getIntValue());
        }

        JsonObject var3 = new JsonObject();

        for(Entry<StatType<?>, JsonObject> var4 : var0.entrySet()) {
            var3.add(Registry.STAT_TYPE.getKey(var4.getKey()).toString(), var4.getValue());
        }

        JsonObject var5 = new JsonObject();
        var5.add("stats", var3);
        var5.addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        return var5.toString();
    }

    private static <T> ResourceLocation getKey(Stat<T> param0) {
        return param0.getType().getRegistry().getKey(param0.getValue());
    }

    public void markAllDirty() {
        this.dirty.addAll(this.stats.keySet());
    }

    public void sendStats(ServerPlayer param0) {
        int var0 = this.server.getTickCount();
        Object2IntMap<Stat<?>> var1 = new Object2IntOpenHashMap<>();
        if (var0 - this.lastStatRequest > 300) {
            this.lastStatRequest = var0;

            for(Stat<?> var2 : this.getDirty()) {
                var1.put(var2, this.getValue(var2));
            }
        }

        param0.connection.send(new ClientboundAwardStatsPacket(var1));
    }
}
