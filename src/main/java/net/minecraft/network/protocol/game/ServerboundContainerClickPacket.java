package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundContainerClickPacket implements Packet<ServerGamePacketListener> {
    private int containerId;
    private int slotNum;
    private int buttonNum;
    private short uid;
    private ItemStack itemStack = ItemStack.EMPTY;
    private ClickType clickType;

    public ServerboundContainerClickPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundContainerClickPacket(int param0, int param1, int param2, ClickType param3, ItemStack param4, short param5) {
        this.containerId = param0;
        this.slotNum = param1;
        this.buttonNum = param2;
        this.itemStack = param4.copy();
        this.uid = param5;
        this.clickType = param3;
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleContainerClick(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.containerId = param0.readByte();
        this.slotNum = param0.readShort();
        this.buttonNum = param0.readByte();
        this.uid = param0.readShort();
        this.clickType = param0.readEnum(ClickType.class);
        this.itemStack = param0.readItem();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.containerId);
        param0.writeShort(this.slotNum);
        param0.writeByte(this.buttonNum);
        param0.writeShort(this.uid);
        param0.writeEnum(this.clickType);
        param0.writeItem(this.itemStack);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlotNum() {
        return this.slotNum;
    }

    public int getButtonNum() {
        return this.buttonNum;
    }

    public short getUid() {
        return this.uid;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    public ClickType getClickType() {
        return this.clickType;
    }
}
