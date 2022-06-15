package net.minecraft.network.chat;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record PlayerChatMessage(Component signedContent, MessageSignature signature, Optional<Component> unsignedContent) {
    public static PlayerChatMessage signed(Component param0, MessageSignature param1) {
        return new PlayerChatMessage(param0, param1, Optional.empty());
    }

    public static PlayerChatMessage signed(String param0, MessageSignature param1) {
        return signed(Component.literal(param0), param1);
    }

    public static PlayerChatMessage signed(Component param0, Component param1, MessageSignature param2, boolean param3) {
        if (param0.equals(param1)) {
            return signed(param0, param2);
        } else {
            return !param3 ? signed(param0, param2).withUnsignedContent(param1) : signed(param1, param2);
        }
    }

    public static FilteredText<PlayerChatMessage> filteredSigned(
        FilteredText<Component> param0, FilteredText<Component> param1, MessageSignature param2, boolean param3
    ) {
        Component var0 = param0.raw();
        Component var1 = param1.raw();
        PlayerChatMessage var2 = signed(var0, var1, param2, param3);
        if (param1.isFiltered()) {
            PlayerChatMessage var3 = Util.mapNullable(param1.filtered(), PlayerChatMessage::unsigned);
            return new FilteredText<>(var2, var3);
        } else {
            return FilteredText.passThrough(var2);
        }
    }

    public static PlayerChatMessage unsigned(Component param0x) {
        return new PlayerChatMessage(param0x, MessageSignature.unsigned(), Optional.empty());
    }

    public PlayerChatMessage withUnsignedContent(Component param0) {
        return new PlayerChatMessage(this.signedContent, this.signature, Optional.of(param0));
    }

    public PlayerChatMessage removeUnsignedContent() {
        return this.unsignedContent.isPresent() ? new PlayerChatMessage(this.signedContent, this.signature, Optional.empty()) : this;
    }

    public boolean verify(ProfilePublicKey param0) {
        return this.signature.verify(param0.createSignatureValidator(), this.signedContent);
    }

    public boolean verify(ServerPlayer param0) {
        ProfilePublicKey var0 = param0.getProfilePublicKey();
        return var0 == null || this.verify(var0);
    }

    public boolean verify(CommandSourceStack param0) {
        ServerPlayer var0 = param0.getPlayer();
        return var0 == null || this.verify(var0);
    }

    public Component serverContent() {
        return this.unsignedContent.orElse(this.signedContent);
    }
}
