package net.minecraft.server.packs.repository;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.server.packs.FileResourcePack;
import net.minecraft.server.packs.FolderResourcePack;
import net.minecraft.server.packs.Pack;

public class FolderRepositorySource implements RepositorySource {
    private static final FileFilter RESOURCEPACK_FILTER = param0 -> {
        boolean var0 = param0.isFile() && param0.getName().endsWith(".zip");
        boolean var1 = param0.isDirectory() && new File(param0, "pack.mcmeta").isFile();
        return var0 || var1;
    };
    private final File folder;

    public FolderRepositorySource(File param0) {
        this.folder = param0;
    }

    @Override
    public <T extends UnopenedPack> void loadPacks(Map<String, T> param0, UnopenedPack.UnopenedPackConstructor<T> param1) {
        if (!this.folder.isDirectory()) {
            this.folder.mkdirs();
        }

        File[] var0 = this.folder.listFiles(RESOURCEPACK_FILTER);
        if (var0 != null) {
            for(File var1 : var0) {
                String var2 = "file/" + var1.getName();
                T var3 = UnopenedPack.create(var2, false, this.createSupplier(var1), param1, UnopenedPack.Position.TOP);
                if (var3 != null) {
                    param0.put(var2, var3);
                }
            }

        }
    }

    private Supplier<Pack> createSupplier(File param0) {
        return param0.isDirectory() ? () -> new FolderResourcePack(param0) : () -> new FileResourcePack(param0);
    }
}
