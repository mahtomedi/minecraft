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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NbtIo {
    public static CompoundTag readCompressed(InputStream param0) throws IOException {
        CompoundTag var3;
        try (DataInputStream var0 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(param0)))) {
            var3 = read(var0, NbtAccounter.UNLIMITED);
        }

        return var3;
    }

    public static void writeCompressed(CompoundTag param0, OutputStream param1) throws IOException {
        try (DataOutputStream var0 = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(param1)))) {
            write(param0, var0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static void safeWrite(CompoundTag param0, File param1) throws IOException {
        File var0 = new File(param1.getAbsolutePath() + "_tmp");
        if (var0.exists()) {
            var0.delete();
        }

        write(param0, var0);
        if (param1.exists()) {
            param1.delete();
        }

        if (param1.exists()) {
            throw new IOException("Failed to delete " + param1);
        } else {
            var0.renameTo(param1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void write(CompoundTag param0, File param1) throws IOException {
        DataOutputStream var0 = new DataOutputStream(new FileOutputStream(param1));

        try {
            write(param0, var0);
        } finally {
            var0.close();
        }

    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static CompoundTag read(File param0) throws IOException {
        if (!param0.exists()) {
            return null;
        } else {
            DataInputStream var0 = new DataInputStream(new FileInputStream(param0));

            CompoundTag var2;
            try {
                var2 = read(var0, NbtAccounter.UNLIMITED);
            } finally {
                var0.close();
            }

            return var2;
        }
    }

    public static CompoundTag read(DataInputStream param0) throws IOException {
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
            return new EndTag();
        } else {
            param0.readUTF();
            Tag var1 = Tag.newTag(var0);

            try {
                var1.load(param0, param1, param2);
                return var1;
            } catch (IOException var8) {
                CrashReport var3 = CrashReport.forThrowable(var8, "Loading NBT data");
                CrashReportCategory var4 = var3.addCategory("NBT Tag");
                var4.setDetail("Tag type", var0);
                throw new ReportedException(var3);
            }
        }
    }
}
