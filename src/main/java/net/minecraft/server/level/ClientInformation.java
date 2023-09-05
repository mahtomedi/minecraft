package net.minecraft.server.level;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;

public record ClientInformation(
    String language,
    int viewDistance,
    ChatVisiblity chatVisibility,
    boolean chatColors,
    int modelCustomisation,
    HumanoidArm mainHand,
    boolean textFilteringEnabled,
    boolean allowsListing
) {
    public static final int MAX_LANGUAGE_LENGTH = 16;

    public ClientInformation(FriendlyByteBuf param0) {
        this(
            param0.readUtf(16),
            param0.readByte(),
            param0.readEnum(ChatVisiblity.class),
            param0.readBoolean(),
            param0.readUnsignedByte(),
            param0.readEnum(HumanoidArm.class),
            param0.readBoolean(),
            param0.readBoolean()
        );
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.language);
        param0.writeByte(this.viewDistance);
        param0.writeEnum(this.chatVisibility);
        param0.writeBoolean(this.chatColors);
        param0.writeByte(this.modelCustomisation);
        param0.writeEnum(this.mainHand);
        param0.writeBoolean(this.textFilteringEnabled);
        param0.writeBoolean(this.allowsListing);
    }

    public static ClientInformation createDefault() {
        return new ClientInformation("en_us", 2, ChatVisiblity.FULL, true, 0, Player.DEFAULT_MAIN_HAND, false, false);
    }
}
