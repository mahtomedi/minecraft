package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StoredUserList<K, V extends StoredUserEntry<K>> {
    protected static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File file;
    private final Map<String, V> map = Maps.newHashMap();
    private boolean enabled = true;

    public StoredUserList(File param0) {
        this.file = param0;
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

    protected abstract StoredUserEntry<K> createEntry(JsonObject var1);

    public Collection<V> getEntries() {
        return this.map.values();
    }

    public void save() throws IOException {
        Collection<V> var0 = this.map.values();

        try (BufferedWriter var1 = Files.newWriter(this.file, StandardCharsets.UTF_8)) {
            GSON.toJson(var0, var1);
        }

    }

    public void load() throws IOException {
        if (this.file.exists()) {
            try (BufferedReader var0 = Files.newReader(this.file, StandardCharsets.UTF_8)) {
                JsonArray var1 = GSON.fromJson(var0, JsonArray.class);
                this.map.clear();

                for(JsonElement var2 : var1) {
                    JsonObject var3 = GsonHelper.convertToJsonObject(var2, "entry");
                    StoredUserEntry<K> var4 = this.createEntry(var3);
                    if (var4.getUser() != null) {
                        this.map.put(this.getKeyForUser(var4.getUser()), (V)var4);
                    }
                }
            }

        }
    }
}
