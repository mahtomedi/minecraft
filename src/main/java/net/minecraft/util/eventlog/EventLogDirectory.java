package net.minecraft.util.eventlog;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class EventLogDirectory {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int COMPRESS_BUFFER_SIZE = 4096;
    private static final String COMPRESSED_EXTENSION = ".gz";
    private final Path root;
    private final String extension;

    private EventLogDirectory(Path param0, String param1) {
        this.root = param0;
        this.extension = param1;
    }

    public static EventLogDirectory open(Path param0, String param1) throws IOException {
        Files.createDirectories(param0);
        return new EventLogDirectory(param0, param1);
    }

    public EventLogDirectory.FileList listFiles() throws IOException {
        EventLogDirectory.FileList var2;
        try (Stream<Path> var0 = Files.list(this.root)) {
            var2 = new EventLogDirectory.FileList(var0.filter(param0 -> Files.isRegularFile(param0)).map(this::parseFile).filter(Objects::nonNull).toList());
        }

        return var2;
    }

    @Nullable
    private EventLogDirectory.File parseFile(Path param0) {
        String var0x = param0.getFileName().toString();
        int var1 = var0x.indexOf(46);
        if (var1 == -1) {
            return null;
        } else {
            EventLogDirectory.FileId var2 = EventLogDirectory.FileId.parse(var0x.substring(0, var1));
            if (var2 != null) {
                String var3 = var0x.substring(var1);
                if (var3.equals(this.extension)) {
                    return new EventLogDirectory.RawFile(param0, var2);
                }

                if (var3.equals(this.extension + ".gz")) {
                    return new EventLogDirectory.CompressedFile(param0, var2);
                }
            }

            return null;
        }
    }

    static void tryCompress(Path param0, Path param1) throws IOException {
        if (Files.exists(param1)) {
            throw new IOException("Compressed target file already exists: " + param1);
        } else {
            try (FileChannel var0 = FileChannel.open(param0, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
                FileLock var1 = var0.tryLock();
                if (var1 == null) {
                    throw new IOException("Raw log file is already locked, cannot compress: " + param0);
                }

                writeCompressed(var0, param1);
                var0.truncate(0L);
            }

            Files.delete(param0);
        }
    }

    private static void writeCompressed(ReadableByteChannel param0, Path param1) throws IOException {
        try (OutputStream var0 = new GZIPOutputStream(Files.newOutputStream(param1))) {
            byte[] var1 = new byte[4096];
            ByteBuffer var2 = ByteBuffer.wrap(var1);

            while(param0.read(var2) >= 0) {
                var2.flip();
                var0.write(var1, 0, var2.limit());
                var2.clear();
            }
        }

    }

    public EventLogDirectory.RawFile createNewFile(LocalDate param0) throws IOException {
        int var0 = 1;
        Set<EventLogDirectory.FileId> var1 = this.listFiles().ids();

        EventLogDirectory.FileId var2;
        do {
            var2 = new EventLogDirectory.FileId(param0, var0++);
        } while(var1.contains(var2));

        EventLogDirectory.RawFile var3 = new EventLogDirectory.RawFile(this.root.resolve(var2.toFileName(this.extension)), var2);
        Files.createFile(var3.path());
        return var3;
    }

    public static record CompressedFile(Path path, EventLogDirectory.FileId id) implements EventLogDirectory.File {
        @Nullable
        @Override
        public Reader openReader() throws IOException {
            return !Files.exists(this.path) ? null : new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(this.path))));
        }

        @Override
        public EventLogDirectory.CompressedFile compress() {
            return this;
        }
    }

    public interface File {
        Path path();

        EventLogDirectory.FileId id();

        @Nullable
        Reader openReader() throws IOException;

        EventLogDirectory.CompressedFile compress() throws IOException;
    }

    public static record FileId(LocalDate date, int index) {
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

        @Nullable
        public static EventLogDirectory.FileId parse(String param0) {
            int var0 = param0.indexOf("-");
            if (var0 == -1) {
                return null;
            } else {
                String var1 = param0.substring(0, var0);
                String var2 = param0.substring(var0 + 1);

                try {
                    return new EventLogDirectory.FileId(LocalDate.parse(var1, DATE_FORMATTER), Integer.parseInt(var2));
                } catch (DateTimeParseException | NumberFormatException var5) {
                    return null;
                }
            }
        }

        public String toString() {
            return DATE_FORMATTER.format(this.date) + "-" + this.index;
        }

        public String toFileName(String param0) {
            return this + param0;
        }
    }

    public static class FileList implements Iterable<EventLogDirectory.File> {
        private final List<EventLogDirectory.File> files;

        FileList(List<EventLogDirectory.File> param0) {
            this.files = new ArrayList<>(param0);
        }

        public EventLogDirectory.FileList prune(LocalDate param0, int param1) {
            this.files.removeIf(param2 -> {
                EventLogDirectory.FileId var0 = param2.id();
                LocalDate var1x = var0.date().plusDays((long)param1);
                if (!param0.isBefore(var1x)) {
                    try {
                        Files.delete(param2.path());
                        return true;
                    } catch (IOException var6) {
                        EventLogDirectory.LOGGER.warn("Failed to delete expired event log file: {}", param2.path(), var6);
                    }
                }

                return false;
            });
            return this;
        }

        public EventLogDirectory.FileList compressAll() {
            ListIterator<EventLogDirectory.File> var0 = this.files.listIterator();

            while(var0.hasNext()) {
                EventLogDirectory.File var1 = var0.next();

                try {
                    var0.set(var1.compress());
                } catch (IOException var4) {
                    EventLogDirectory.LOGGER.warn("Failed to compress event log file: {}", var1.path(), var4);
                }
            }

            return this;
        }

        @Override
        public Iterator<EventLogDirectory.File> iterator() {
            return this.files.iterator();
        }

        public Stream<EventLogDirectory.File> stream() {
            return this.files.stream();
        }

        public Set<EventLogDirectory.FileId> ids() {
            return this.files.stream().map(EventLogDirectory.File::id).collect(Collectors.toSet());
        }
    }

    public static record RawFile(Path path, EventLogDirectory.FileId id) implements EventLogDirectory.File {
        public FileChannel openChannel() throws IOException {
            return FileChannel.open(this.path, StandardOpenOption.WRITE, StandardOpenOption.READ);
        }

        @Nullable
        @Override
        public Reader openReader() throws IOException {
            return Files.exists(this.path) ? Files.newBufferedReader(this.path) : null;
        }

        @Override
        public EventLogDirectory.CompressedFile compress() throws IOException {
            Path var0 = this.path.resolveSibling(this.path.getFileName().toString() + ".gz");
            EventLogDirectory.tryCompress(this.path, var0);
            return new EventLogDirectory.CompressedFile(var0, this.id);
        }
    }
}
