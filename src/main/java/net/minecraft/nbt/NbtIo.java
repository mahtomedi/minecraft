package net.minecraft.nbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class NbtIo {
    public static CompoundTag readCompressed(File param0) throws IOException {
        CompoundTag var3;
        try (InputStream var0 = new FileInputStream(param0)) {
            var3 = readCompressed(var0);
        }

        return var3;
    }

    public static CompoundTag readCompressed(InputStream param0) throws IOException {
        CompoundTag var3;
        try (DataInputStream var0 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(param0)))) {
            var3 = read(var0, NbtAccounter.UNLIMITED);
        }

        return var3;
    }

    public static void writeCompressed(CompoundTag param0, File param1) throws IOException {
        try (OutputStream var0 = new FileOutputStream(param1)) {
            writeCompressed(param0, var0);
        }

    }

    public static void writeCompressed(CompoundTag param0, OutputStream param1) throws IOException {
        try (DataOutputStream var0 = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(param1)))) {
            write(param0, var0);
        }

    }

    public static void write(CompoundTag param0, File param1) throws IOException {
        try (
            FileOutputStream var0 = new FileOutputStream(param1);
            DataOutputStream var1 = new DataOutputStream(var0);
        ) {
            write(param0, var1);
        }

    }

    @Nullable
    public static CompoundTag read(File param0) throws IOException {
        if (!param0.exists()) {
            return null;
        } else {
            CompoundTag var5;
            try (
                FileInputStream var0 = new FileInputStream(param0);
                DataInputStream var1 = new DataInputStream(var0);
            ) {
                var5 = read(var1, NbtAccounter.UNLIMITED);
            }

            return var5;
        }
    }

    public static CompoundTag read(DataInput param0) throws IOException {
        return read(param0, NbtAccounter.UNLIMITED);
    }

    public static CompoundTag read(DataInput param0, NbtAccounter param1) throws IOException {
        Tag var0 = readUnnamedTag(param0, 0, param1);
        if (var0 instanceof CompoundTag) {
            return (CompoundTag)var0;
        } else {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(CompoundTag param0, DataOutput param1) throws IOException {
        writeUnnamedTag(param0, param1);
    }

    private static void writeUnnamedTag(Tag param0, DataOutput param1) throws IOException {
        param1.writeByte(param0.getId());
        if (param0.getId() != 0) {
            param1.writeUTF("");
            param0.write(param1);
        }
    }

    private static Tag readUnnamedTag(DataInput param0, int param1, NbtAccounter param2) throws IOException {
        byte var0 = param0.readByte();
        if (var0 == 0) {
            return EndTag.INSTANCE;
        } else {
            param0.readUTF();

            try {
                return TagTypes.getType(var0).load(param0, param1, param2);
            } catch (IOException var7) {
                CrashReport var2 = CrashReport.forThrowable(var7, "Loading NBT data");
                CrashReportCategory var3 = var2.addCategory("NBT Tag");
                var3.setDetail("Tag type", var0);
                throw new ReportedException(var2);
            }
        }
    }
}
