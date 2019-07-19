package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StoredUserList<K, V extends StoredUserEntry<K>> {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final Gson gson;
    private final File file;
    private final Map<String, V> map = Maps.newHashMap();
    private boolean enabled = true;
    private static final ParameterizedType USERLIST_ENTRY_TYPE = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{StoredUserEntry.class};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    };

    public StoredUserList(File param0) {
        this.file = param0;
        GsonBuilder var0 = new GsonBuilder().setPrettyPrinting();
        var0.registerTypeHierarchyAdapter(StoredUserEntry.class, new StoredUserList.Serializer());
        this.gson = var0.create();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean param0) {
        this.enabled = param0;
    }

    public File getFile() {
        return this.file;
    }

    public void add(V param0) {
        this.map.put(this.getKeyForUser(param0.getUser()), param0);

        try {
            this.save();
        } catch (IOException var3) {
            LOGGER.warn("Could not save the list after adding a user.", (Throwable)var3);
        }

    }

    @Nullable
    public V get(K param0) {
        this.removeExpired();
        return this.map.get(this.getKeyForUser(param0));
    }

    public void remove(K param0) {
        this.map.remove(this.getKeyForUser(param0));

        try {
            this.save();
        } catch (IOException var3) {
            LOGGER.warn("Could not save the list after removing a user.", (Throwable)var3);
        }

    }

    public void remove(StoredUserEntry<K> param0) {
        this.remove(param0.getUser());
    }

    public String[] getUserList() {
        return this.map.keySet().toArray(new String[this.map.size()]);
    }

    public boolean isEmpty() {
        return this.map.size() < 1;
    }

    protected String getKeyForUser(K param0) {
        return param0.toString();
    }

    protected boolean contains(K param0) {
        return this.map.containsKey(this.getKeyForUser(param0));
    }

    private void removeExpired() {
        List<K> var0 = Lists.newArrayList();

        for(V var1 : this.map.values()) {
            if (var1.hasExpired()) {
                var0.add(var1.getUser());
            }
        }

        for(K var2 : var0) {
            this.map.remove(this.getKeyForUser(var2));
        }

    }

    protected StoredUserEntry<K> createEntry(JsonObject param0) {
        return new StoredUserEntry<>((K)null, param0);
    }

    public Collection<V> getEntries() {
        return this.map.values();
    }

    public void save() throws IOException {
        Collection<V> var0 = this.map.values();
        String var1 = this.gson.toJson(var0);
        BufferedWriter var2 = null;

        try {
            var2 = Files.newWriter(this.file, StandardCharsets.UTF_8);
            var2.write(var1);
        } finally {
            IOUtils.closeQuietly((Writer)var2);
        }

    }

    public void load() throws FileNotFoundException {
        if (this.file.exists()) {
            BufferedReader var0 = null;

            try {
                var0 = Files.newReader(this.file, StandardCharsets.UTF_8);
                Collection<StoredUserEntry<K>> var1 = GsonHelper.fromJson(this.gson, var0, USERLIST_ENTRY_TYPE);
                if (var1 != null) {
                    this.map.clear();

                    for(StoredUserEntry<K> var2 : var1) {
                        if (var2.getUser() != null) {
                            this.map.put(this.getKeyForUser(var2.getUser()), (V)var2);
                        }
                    }
                }
            } finally {
                IOUtils.closeQuietly((Reader)var0);
            }

        }
    }

    class Serializer implements JsonDeserializer<StoredUserEntry<K>>, JsonSerializer<StoredUserEntry<K>> {
        private Serializer() {
        }

        public JsonElement serialize(StoredUserEntry<K> param0, Type param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();
            param0.serialize(var0);
            return var0;
        }

        public StoredUserEntry<K> deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            if (param0.isJsonObject()) {
                JsonObject var0 = param0.getAsJsonObject();
                return StoredUserList.this.createEntry(var0);
            } else {
                return null;
            }
        }
    }
}
