package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

public class CommandStorage {
    private static final String ID_PREFIX = "command_storage_";
    private final Map<String, CommandStorage.Container> namespaces = Maps.newHashMap();
    private final DimensionDataStorage storage;

    public CommandStorage(DimensionDataStorage param0) {
        this.storage = param0;
    }

    private CommandStorage.Container newStorage(String param0) {
        CommandStorage.Container var0 = new CommandStorage.Container();
        this.namespaces.put(param0, var0);
        return var0;
    }

    public CompoundTag get(ResourceLocation param0) {
        String var0 = param0.getNamespace();
        CommandStorage.Container var1 = this.storage.get(param1 -> this.newStorage(var0).load(param1), createId(var0));
        return var1 != null ? var1.get(param0.getPath()) : new CompoundTag();
    }

    public void set(ResourceLocation param0, CompoundTag param1) {
        String var0 = param0.getNamespace();
        this.storage.computeIfAbsent(param1x -> this.newStorage(var0).load(param1x), () -> this.newStorage(var0), createId(var0)).put(param0.getPath(), param1);
    }

    public Stream<ResourceLocation> keys() {
        return this.namespaces.entrySet().stream().flatMap(param0 -> param0.getValue().getKeys(param0.getKey()));
    }

    private static String createId(String param0) {
        return "command_storage_" + param0;
    }

    static class Container extends SavedData {
        private static final String TAG_CONTENTS = "contents";
        private final Map<String, CompoundTag> storage = Maps.newHashMap();

        private Container() {
        }

        private CommandStorage.Container load(CompoundTag param0) {
            CompoundTag var0 = param0.getCompound("contents");

            for(String var1 : var0.getAllKeys()) {
                this.storage.put(var1, var0.getCompound(var1));
            }

            return this;
        }

        @Override
        public CompoundTag save(CompoundTag param0) {
            CompoundTag var0 = new CompoundTag();
            this.storage.forEach((param1, param2) -> var0.put(param1, param2.copy()));
            param0.put("contents", var0);
            return param0;
        }

        public CompoundTag get(String param0) {
            CompoundTag var0 = this.storage.get(param0);
            return var0 != null ? var0 : new CompoundTag();
        }

        public void put(String param0, CompoundTag param1) {
            if (param1.isEmpty()) {
                this.storage.remove(param0);
            } else {
                this.storage.put(param0, param1);
            }

            this.setDirty();
        }

        public Stream<ResourceLocation> getKeys(String param0) {
            return this.storage.keySet().stream().map(param1 -> new ResourceLocation(param0, param1));
        }
    }
}
