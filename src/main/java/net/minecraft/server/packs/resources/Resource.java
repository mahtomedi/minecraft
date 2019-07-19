package net.minecraft.server.packs.resources;

import java.io.Closeable;
import java.io.InputStream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface Resource extends Closeable {
    @OnlyIn(Dist.CLIENT)
    ResourceLocation getLocation();

    InputStream getInputStream();

    @Nullable
    @OnlyIn(Dist.CLIENT)
    <T> T getMetadata(MetadataSectionSerializer<T> var1);

    String getSourceName();
}
