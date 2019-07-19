package net.minecraft.client.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DirectAssetIndex extends AssetIndex {
    private final File assetsDirectory;

    public DirectAssetIndex(File param0) {
        this.assetsDirectory = param0;
    }

    @Override
    public File getFile(ResourceLocation param0) {
        return new File(this.assetsDirectory, param0.toString().replace(':', '/'));
    }

    @Override
    public File getFile(String param0) {
        return new File(this.assetsDirectory, param0);
    }

    @Override
    public Collection<String> getFiles(String param0, int param1, Predicate<String> param2) {
        Path var0 = this.assetsDirectory.toPath().resolve("minecraft/");

        try (Stream<Path> var1 = Files.walk(var0.resolve(param0), param1)) {
            return var1.filter(param0x -> Files.isRegularFile(param0x))
                .filter(param0x -> !param0x.endsWith(".mcmeta"))
                .map(var0::relativize)
                .map(Object::toString)
                .map(param0x -> param0x.replaceAll("\\\\", "/"))
                .filter(param2)
                .collect(Collectors.toList());
        } catch (NoSuchFileException var20) {
        } catch (IOException var21) {
            LOGGER.warn("Unable to getFiles on {}", param0, var21);
        }

        return Collections.emptyList();
    }
}
