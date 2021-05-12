package net.minecraft.server;

import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class ChainedJsonException extends IOException {
    private final List<ChainedJsonException.Entry> entries = Lists.newArrayList();
    private final String message;

    public ChainedJsonException(String param0) {
        this.entries.add(new ChainedJsonException.Entry());
        this.message = param0;
    }

    public ChainedJsonException(String param0, Throwable param1) {
        super(param1);
        this.entries.add(new ChainedJsonException.Entry());
        this.message = param0;
    }

    public void prependJsonKey(String param0) {
        this.entries.get(0).addJsonKey(param0);
    }

    public void setFilenameAndFlush(String param0) {
        this.entries.get(0).filename = param0;
        this.entries.add(0, new ChainedJsonException.Entry());
    }

    @Override
    public String getMessage() {
        return "Invalid " + this.entries.get(this.entries.size() - 1) + ": " + this.message;
    }

    public static ChainedJsonException forException(Exception param0) {
        if (param0 instanceof ChainedJsonException) {
            return (ChainedJsonException)param0;
        } else {
            String var0 = param0.getMessage();
            if (param0 instanceof FileNotFoundException) {
                var0 = "File not found";
            }

            return new ChainedJsonException(var0, param0);
        }
    }

    public static class Entry {
        @Nullable
        String filename;
        private final List<String> jsonKeys = Lists.newArrayList();

        Entry() {
        }

        void addJsonKey(String param0) {
            this.jsonKeys.add(0, param0);
        }

        @Nullable
        public String getFilename() {
            return this.filename;
        }

        public String getJsonKeys() {
            return StringUtils.join((Iterable<?>)this.jsonKeys, "->");
        }

        @Override
        public String toString() {
            if (this.filename != null) {
                return this.jsonKeys.isEmpty() ? this.filename : this.filename + " " + this.getJsonKeys();
            } else {
                return this.jsonKeys.isEmpty() ? "(Unknown file)" : "(Unknown file) " + this.getJsonKeys();
            }
        }
    }
}
