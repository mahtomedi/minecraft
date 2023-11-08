package net.minecraft.nbt;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.Util;
import net.minecraft.util.DelegateDataOutput;
import net.minecraft.util.FastBufferedInputStream;

public class NbtIo {
    private static final OpenOption[] SYNC_OUTPUT_OPTIONS = new OpenOption[]{
        StandardOpenOption.SYNC, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
    };

    public static CompoundTag readCompressed(Path param0, NbtAccounter param1) throws IOException {
        CompoundTag var3;
        try (InputStream var0 = Files.newInputStream(param0)) {
            var3 = readCompressed(var0, param1);
        }

        return var3;
    }

    private static DataInputStream createDecompressorStream(InputStream param0) throws IOException {
        return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(param0)));
    }

    private static DataOutputStream createCompressorStream(OutputStream param0) throws IOException {
        return new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(param0)));
    }

    public static CompoundTag readCompressed(InputStream param0, NbtAccounter param1) throws IOException {
        CompoundTag var3;
        try (DataInputStream var0 = createDecompressorStream(param0)) {
            var3 = read(var0, param1);
        }

        return var3;
    }

    public static void parseCompressed(Path param0, StreamTagVisitor param1, NbtAccounter param2) throws IOException {
        try (InputStream var0 = Files.newInputStream(param0)) {
            parseCompressed(var0, param1, param2);
        }

    }

    public static void parseCompressed(InputStream param0, StreamTagVisitor param1, NbtAccounter param2) throws IOException {
        try (DataInputStream var0 = createDecompressorStream(param0)) {
            parse(var0, param1, param2);
        }

    }

    public static byte[] writeToByteArrayCompressed(CompoundTag param0) throws IOException {
        ByteArrayOutputStream var0 = new ByteArrayOutputStream();

        try (DataOutputStream var1 = createCompressorStream(var0)) {
            write(param0, var1);
        }

        return var0.toByteArray();
    }

    public static byte[] writeToByteArray(CompoundTag param0) throws IOException {
        ByteArrayOutputStream var0 = new ByteArrayOutputStream();

        try (DataOutputStream var1 = new DataOutputStream(var0)) {
            write(param0, var1);
        }

        return var0.toByteArray();
    }

    public static void writeCompressed(CompoundTag param0, Path param1) throws IOException {
        try (
            OutputStream var0 = Files.newOutputStream(param1, SYNC_OUTPUT_OPTIONS);
            OutputStream var1 = new BufferedOutputStream(var0);
        ) {
            writeCompressed(param0, var1);
        }

    }

    public static void writeCompressed(CompoundTag param0, OutputStream param1) throws IOException {
        try (DataOutputStream var0 = createCompressorStream(param1)) {
            write(param0, var0);
        }

    }

    public static void write(CompoundTag param0, Path param1) throws IOException {
        try (
            OutputStream var0 = Files.newOutputStream(param1, SYNC_OUTPUT_OPTIONS);
            OutputStream var1 = new BufferedOutputStream(var0);
            DataOutputStream var2 = new DataOutputStream(var1);
        ) {
            write(param0, var2);
        }

    }

    @Nullable
    public static CompoundTag read(Path param0) throws IOException {
        if (!Files.exists(param0)) {
            return null;
        } else {
            CompoundTag var3;
            try (
                InputStream var0 = Files.newInputStream(param0);
                DataInputStream var1 = new DataInputStream(var0);
            ) {
                var3 = read(var1, NbtAccounter.unlimitedHeap());
            }

            return var3;
        }
    }

    public static CompoundTag read(DataInput param0) throws IOException {
        return read(param0, NbtAccounter.unlimitedHeap());
    }

    public static CompoundTag read(DataInput param0, NbtAccounter param1) throws IOException {
        Tag var0 = readUnnamedTag(param0, param1);
        if (var0 instanceof CompoundTag) {
            return (CompoundTag)var0;
        } else {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(CompoundTag param0, DataOutput param1) throws IOException {
        writeUnnamedTagWithFallback(param0, param1);
    }

    public static void parse(DataInput param0, StreamTagVisitor param1, NbtAccounter param2) throws IOException {
        TagType<?> var0 = TagTypes.getType(param0.readByte());
        if (var0 == EndTag.TYPE) {
            if (param1.visitRootEntry(EndTag.TYPE) == StreamTagVisitor.ValueResult.CONTINUE) {
                param1.visitEnd();
            }

        } else {
            switch(param1.visitRootEntry(var0)) {
                case HALT:
                default:
                    break;
                case BREAK:
                    StringTag.skipString(param0);
                    var0.skip(param0, param2);
                    break;
                case CONTINUE:
                    StringTag.skipString(param0);
                    var0.parse(param0, param1, param2);
            }

        }
    }

    public static Tag readAnyTag(DataInput param0, NbtAccounter param1) throws IOException {
        byte var0 = param0.readByte();
        return (Tag)(var0 == 0 ? EndTag.INSTANCE : readTagSafe(param0, param1, var0));
    }

    public static void writeAnyTag(Tag param0, DataOutput param1) throws IOException {
        param1.writeByte(param0.getId());
        if (param0.getId() != 0) {
            param0.write(param1);
        }
    }

    public static void writeUnnamedTag(Tag param0, DataOutput param1) throws IOException {
        param1.writeByte(param0.getId());
        if (param0.getId() != 0) {
            param1.writeUTF("");
            param0.write(param1);
        }
    }

    public static void writeUnnamedTagWithFallback(Tag param0, DataOutput param1) throws IOException {
        writeUnnamedTag(param0, new NbtIo.StringFallbackDataOutput(param1));
    }

    private static Tag readUnnamedTag(DataInput param0, NbtAccounter param1) throws IOException {
        byte var0 = param0.readByte();
        if (var0 == 0) {
            return EndTag.INSTANCE;
        } else {
            StringTag.skipString(param0);
            return readTagSafe(param0, param1, var0);
        }
    }

    private static Tag readTagSafe(DataInput param0, NbtAccounter param1, byte param2) {
        try {
            return TagTypes.getType(param2).load(param0, param1);
        } catch (IOException var6) {
            CrashReport var1 = CrashReport.forThrowable(var6, "Loading NBT data");
            CrashReportCategory var2 = var1.addCategory("NBT Tag");
            var2.setDetail("Tag type", param2);
            throw new ReportedNbtException(var1);
        }
    }

    public static class StringFallbackDataOutput extends DelegateDataOutput {
        public StringFallbackDataOutput(DataOutput param0) {
            super(param0);
        }

        @Override
        public void writeUTF(String param0) throws IOException {
            try {
                super.writeUTF(param0);
            } catch (UTFDataFormatException var3) {
                Util.logAndPauseIfInIde("Failed to write NBT String", var3);
                super.writeUTF("");
            }

        }
    }
}
