package net.minecraft.server.packs.resources;

public interface CloseableResourceManager extends AutoCloseable, ResourceManager {
    @Override
    void close();
}
