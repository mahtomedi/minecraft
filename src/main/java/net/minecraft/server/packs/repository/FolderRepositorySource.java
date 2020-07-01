package net.minecraft.server.packs.repository;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;

public class FolderRepositorySource implements RepositorySource {
    private static final FileFilter RESOURCEPACK_FILTER = param0 -> {
        boolean var0 = param0.isFile() && param0.getName().endsWith(".zip");
        boolean var1 = param0.isDirectory() && new File(param0, "pack.mcmeta").isFile();
        return var0 || var1;
    };
    private final File folder;
    private final PackSource packSource;

    public FolderRepositorySource(File param0, PackSource param1) {
        this.folder = param0;
        this.packSource = param1;
    }

    @Override
    public void loadPacks(Consumer<Pack> param0, Pack.PackConstructor param1) {
        if (!this.folder.isDirectory()) {
            this.folder.mkdirs();
        }

        File[] var0 = this.folder.listFiles(RESOURCEPACK_FILTER);
        if (var0 != null) {
            for(File var1 : var0) {
                String var2 = "file/" + var1.getName();
                Pack var3 = Pack.create(var2, false, this.createSupplier(var1), param1, Pack.Position.TOP, this.packSource);
                if (var3 != null) {
                    param0.accept(var3);
                }
            }

        }
    }

    private Supplier<PackResources> createSupplier(File param0) {
        return param0.isDirectory() ? () -> new FolderPackResources(param0) : () -> new FilePackResources(param0);
    }
}
