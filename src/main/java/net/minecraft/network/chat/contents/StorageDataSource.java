package net.minecraft.network.chat.contents;

import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record StorageDataSource(ResourceLocation id) implements DataSource {
    @Override
    public Stream<CompoundTag> getData(CommandSourceStack param0) {
        CompoundTag var0 = param0.getServer().getCommandStorage().get(this.id);
        return Stream.of(var0);
    }

    @Override
    public String toString() {
        return "storage=" + this.id;
    }
}
