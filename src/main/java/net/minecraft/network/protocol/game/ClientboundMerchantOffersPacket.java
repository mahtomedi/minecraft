package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientboundMerchantOffersPacket implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final MerchantOffers offers;
    private final int villagerLevel;
    private final int villagerXp;
    private final boolean showProgress;
    private final boolean canRestock;

    public ClientboundMerchantOffersPacket(int param0, MerchantOffers param1, int param2, int param3, boolean param4, boolean param5) {
        this.containerId = param0;
        this.offers = param1.copy();
        this.villagerLevel = param2;
        this.villagerXp = param3;
        this.showProgress = param4;
        this.canRestock = param5;
    }

    public ClientboundMerchantOffersPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readVarInt();
        this.offers = MerchantOffers.createFromStream(param0);
        this.villagerLevel = param0.readVarInt();
        this.villagerXp = param0.readVarInt();
        this.showProgress = param0.readBoolean();
        this.canRestock = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.containerId);
        this.offers.writeToStream(param0);
        param0.writeVarInt(this.villagerLevel);
        param0.writeVarInt(this.villagerXp);
        param0.writeBoolean(this.showProgress);
        param0.writeBoolean(this.canRestock);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleMerchantOffers(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public MerchantOffers getOffers() {
        return this.offers;
    }

    public int getVillagerLevel() {
        return this.villagerLevel;
    }

    public int getVillagerXp() {
        return this.villagerXp;
    }

    public boolean showProgress() {
        return this.showProgress;
    }

    public boolean canRestock() {
        return this.canRestock;
    }
}
