package net.minecraft.data.structures;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.Bootstrap;

public class SnbtDatafixer {
    public static void main(String[] param0) throws IOException {
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();

        for(String var0 : param0) {
            updateInDirectory(var0);
        }

    }

    private static void updateInDirectory(String param0) throws IOException {
        try (Stream<Path> var0 = Files.walk(Paths.get(param0))) {
            var0.filter(param0x -> param0x.toString().endsWith(".snbt")).forEach(param0x -> {
                try {
                    String var0x = Files.readString(param0x);
                    CompoundTag var1x = NbtUtils.snbtToStructure(var0x);
                    CompoundTag var2 = StructureUpdater.update(param0x.toString(), var1x);
                    NbtToSnbt.writeSnbt(CachedOutput.NO_CACHE, param0x, NbtUtils.structureToSnbt(var2));
                } catch (IOException | CommandSyntaxException var4) {
                    throw new RuntimeException(var4);
                }
            });
        }

    }
}
