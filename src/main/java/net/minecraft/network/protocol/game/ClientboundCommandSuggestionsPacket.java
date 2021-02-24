package net.minecraft.network.protocol.game;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundCommandSuggestionsPacket implements Packet<ClientGamePacketListener> {
    private final int id;
    private final Suggestions suggestions;

    public ClientboundCommandSuggestionsPacket(int param0, Suggestions param1) {
        this.id = param0;
        this.suggestions = param1;
    }

    public ClientboundCommandSuggestionsPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        int var0 = param0.readVarInt();
        int var1 = param0.readVarInt();
        StringRange var2 = StringRange.between(var0, var0 + var1);
        List<Suggestion> var3 = param0.readList(param1 -> {
            String var0x = param1.readUtf();
            Component var1x = param1.readBoolean() ? param1.readComponent() : null;
            return new Suggestion(var2, var0x, var1x);
        });
        this.suggestions = new Suggestions(var2, var3);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        param0.writeVarInt(this.suggestions.getRange().getStart());
        param0.writeVarInt(this.suggestions.getRange().getLength());
        param0.writeCollection(this.suggestions.getList(), (param0x, param1) -> {
            param0x.writeUtf(param1.getText());
            param0x.writeBoolean(param1.getTooltip() != null);
            if (param1.getTooltip() != null) {
                param0x.writeComponent(ComponentUtils.fromMessage(param1.getTooltip()));
            }

        });
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleCommandSuggestions(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public Suggestions getSuggestions() {
        return this.suggestions;
    }
}
