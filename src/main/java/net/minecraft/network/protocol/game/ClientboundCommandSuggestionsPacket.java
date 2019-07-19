package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundCommandSuggestionsPacket implements Packet<ClientGamePacketListener> {
    private int id;
    private Suggestions suggestions;

    public ClientboundCommandSuggestionsPacket() {
    }

    public ClientboundCommandSuggestionsPacket(int param0, Suggestions param1) {
        this.id = param0;
        this.suggestions = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
        int var0 = param0.readVarInt();
        int var1 = param0.readVarInt();
        StringRange var2 = StringRange.between(var0, var0 + var1);
        int var3 = param0.readVarInt();
        List<Suggestion> var4 = Lists.newArrayListWithCapacity(var3);

        for(int var5 = 0; var5 < var3; ++var5) {
            String var6 = param0.readUtf(32767);
            Component var7 = param0.readBoolean() ? param0.readComponent() : null;
            var4.add(new Suggestion(var2, var6, var7));
        }

        this.suggestions = new Suggestions(var2, var4);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.id);
        param0.writeVarInt(this.suggestions.getRange().getStart());
        param0.writeVarInt(this.suggestions.getRange().getLength());
        param0.writeVarInt(this.suggestions.getList().size());

        for(Suggestion var0 : this.suggestions.getList()) {
            param0.writeUtf(var0.getText());
            param0.writeBoolean(var0.getTooltip() != null);
            if (var0.getTooltip() != null) {
                param0.writeComponent(ComponentUtils.fromMessage(var0.getTooltip()));
            }
        }

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
