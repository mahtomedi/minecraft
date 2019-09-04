package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FallbackResourceManager implements ResourceManager {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final List<Pack> fallbacks = Lists.newArrayList();
    private final PackType type;

    public FallbackResourceManager(PackType param0) {
        this.type = param0;
    }

    @Override
    public void add(Pack param0) {
        this.fallbacks.add(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Set<String> getNamespaces() {
        return Collections.emptySet();
    }

    @Override
    public Resource getResource(ResourceLocation param0) throws IOException {
        this.validateLocation(param0);
        Pack var0 = null;
        ResourceLocation var1 = getMetadataLocation(param0);

        for(int var2 = this.fallbacks.size() - 1; var2 >= 0; --var2) {
            Pack var3 = this.fallbacks.get(var2);
            if (var0 == null && var3.hasResource(this.type, var1)) {
                var0 = var3;
            }

            if (var3.hasResource(this.type, param0)) {
                InputStream var4 = null;
                if (var0 != null) {
                    var4 = this.getWrappedResource(var1, var0);
                }

                return new SimpleResource(var3.getName(), param0, this.getWrappedResource(param0, var3), var4);
            }
        }

        throw new FileNotFoundException(param0.toString());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean hasResource(ResourceLocation param0) {
        if (!this.isValidLocation(param0)) {
            return false;
        } else {
            for(int var0 = this.fallbacks.size() - 1; var0 >= 0; --var0) {
                Pack var1 = this.fallbacks.get(var0);
                if (var1.hasResource(this.type, param0)) {
                    return true;
                }
            }

            return false;
        }
    }

    protected InputStream getWrappedResource(ResourceLocation param0, Pack param1) throws IOException {
        InputStream var0 = param1.getResource(this.type, param0);
        return (InputStream)(LOGGER.isDebugEnabled() ? new FallbackResourceManager.LeakedResourceWarningInputStream(var0, param0, param1.getName()) : var0);
    }

    private void validateLocation(ResourceLocation param0) throws IOException {
        if (!this.isValidLocation(param0)) {
            throw new IOException("Invalid relative path to resource: " + param0);
        }
    }

    private boolean isValidLocation(ResourceLocation param0) {
        return !param0.getPath().contains("..");
    }

    @Override
    public List<Resource> getResources(ResourceLocation param0) throws IOException {
        this.validateLocation(param0);
        List<Resource> var0 = Lists.newArrayList();
        ResourceLocation var1 = getMetadataLocation(param0);

        for(Pack var2 : this.fallbacks) {
            if (var2.hasResource(this.type, param0)) {
                InputStream var3 = var2.hasResource(this.type, var1) ? this.getWrappedResource(var1, var2) : null;
                var0.add(new SimpleResource(var2.getName(), param0, this.getWrappedResource(param0, var2), var3));
            }
        }

        if (var0.isEmpty()) {
            throw new FileNotFoundException(param0.toString());
        } else {
            return var0;
        }
    }

    @Override
    public Collection<ResourceLocation> listResources(String param0, Predicate<String> param1) {
        List<ResourceLocation> var0 = Lists.newArrayList();

        for(Pack var1 : this.fallbacks) {
            var0.addAll(var1.getResources(this.type, param0, Integer.MAX_VALUE, param1));
        }

        Collections.sort(var0);
        return var0;
    }

    static ResourceLocation getMetadataLocation(ResourceLocation param0) {
        return new ResourceLocation(param0.getNamespace(), param0.getPath() + ".mcmeta");
    }

    static class LeakedResourceWarningInputStream extends FilterInputStream {
        private final String message;
        private boolean closed;

        public LeakedResourceWarningInputStream(InputStream param0, ResourceLocation param1, String param2) {
            super(param0);
            ByteArrayOutputStream var0 = new ByteArrayOutputStream();
            new Exception().printStackTrace(new PrintStream(var0));
            this.message = "Leaked resource: '" + param1 + "' loaded from pack: '" + param2 + "'\n" + var0;
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        @Override
        protected void finalize() throws Throwable {
            if (!this.closed) {
                FallbackResourceManager.LOGGER.warn(this.message);
            }

            super.finalize();
        }
    }
}
