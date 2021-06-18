package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundEditBookPacket implements Packet<ServerGamePacketListener> {
    public static final int MAX_BYTES_PER_CHAR = 4;
    private static final int TITLE_MAX_CHARS = 128;
    private static final int PAGE_MAX_CHARS = 8192;
    private final int slot;
    private final List<String> pages;
    private final Optional<String> title;

    public ServerboundEditBookPacket(int param0, List<String> param1, Optional<String> param2) {
        this.slot = param0;
        this.pages = ImmutableList.copyOf(param1);
        this.title = param2;
    }

    public ServerboundEditBookPacket(FriendlyByteBuf param0) {
        this.slot = param0.readVarInt();
        this.pages = param0.readList(param0x -> param0x.readUtf(8192));
        this.title = param0.readOptional(param0x -> param0x.readUtf(128));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.slot);
        param0.writeCollection(this.pages, (param0x, param1) -> param0x.writeUtf(param1, 8192));
        param0.writeOptional(this.title, (param0x, param1) -> param0x.writeUtf(param1, 128));
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleEditBook(this);
    }

    public List<String> getPages() {
        return this.pages;
    }

    public Optional<String> getTitle() {
        return this.title;
    }

    public int getSlot() {
        return this.slot;
    }
}
