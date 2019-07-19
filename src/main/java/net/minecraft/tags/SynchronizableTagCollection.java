package net.minecraft.tags;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SynchronizableTagCollection<T> extends TagCollection<T> {
    private final Registry<T> registry;

    public SynchronizableTagCollection(Registry<T> param0, String param1, String param2) {
        super(param0::getOptional, param1, false, param2);
        this.registry = param0;
    }

    public void serializeToNetwork(FriendlyByteBuf param0) {
        Map<ResourceLocation, Tag<T>> var0 = this.getAllTags();
        param0.writeVarInt(var0.size());

        for(Entry<ResourceLocation, Tag<T>> var1 : var0.entrySet()) {
            param0.writeResourceLocation(var1.getKey());
            param0.writeVarInt(var1.getValue().getValues().size());

            for(T var2 : var1.getValue().getValues()) {
                param0.writeVarInt(this.registry.getId(var2));
            }
        }

    }

    public void loadFromNetwork(FriendlyByteBuf param0) {
        Map<ResourceLocation, Tag<T>> var0 = Maps.newHashMap();
        int var1 = param0.readVarInt();

        for(int var2 = 0; var2 < var1; ++var2) {
            ResourceLocation var3 = param0.readResourceLocation();
            int var4 = param0.readVarInt();
            Tag.Builder<T> var5 = Tag.Builder.tag();

            for(int var6 = 0; var6 < var4; ++var6) {
                var5.add(this.registry.byId(param0.readVarInt()));
            }

            var0.put(var3, var5.build(var3));
        }

        this.replace(var0);
    }
}
