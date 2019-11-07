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
    public File getRootFile(String param0) {
        return new File(this.assetsDirectory, param0);
    }

    @Override
    public Collection<ResourceLocation> getFiles(String param0, String param1, int param2, Predicate<String> param3) {
        Path var0 = this.assetsDirectory.toPath().resolve(param1);

        try (Stream<Path> var1 = Files.walk(var0.resolve(param0), param2)) {
            return var1.filter(param0x -> Files.isRegularFile(param0x))
                .filter(param0x -> !param0x.endsWith(".mcmeta"))
                .filter(param1x -> param3.test(param1x.getFileName().toString()))
                .map(param2x -> new ResourceLocation(param1, var0.relativize(param2x).toString().replaceAll("\\\\", "/")))
                .collect(Collectors.toList());
        } catch (NoSuchFileException var21) {
        } catch (IOException var22) {
            LOGGER.warn("Unable to getFiles on {}", param0, var22);
        }

        return Collections.emptyList();
    }
}
