package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundContainerClickPacket implements Packet<ServerGamePacketListener> {
    private final int containerId;
    private final int slotNum;
    private final int buttonNum;
    private final ClickType clickType;
    private final ItemStack carriedItem;
    private final Int2ObjectMap<ItemStack> changedSlots;

    @OnlyIn(Dist.CLIENT)
    public ServerboundContainerClickPacket(int param0, int param1, int param2, ClickType param3, ItemStack param4, Int2ObjectMap<ItemStack> param5) {
        this.containerId = param0;
        this.slotNum = param1;
        this.buttonNum = param2;
        this.clickType = param3;
        this.carriedItem = param4;
        this.changedSlots = Int2ObjectMaps.unmodifiable(param5);
    }

    public ServerboundContainerClickPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readByte();
        this.slotNum = param0.readShort();
        this.buttonNum = param0.readByte();
        this.clickType = param0.readEnum(ClickType.class);
        this.changedSlots = Int2ObjectMaps.unmodifiable(
            param0.readMap(Int2ObjectOpenHashMap::new, param0x -> Integer.valueOf(param0x.readShort()), FriendlyByteBuf::readItem)
        );
        this.carriedItem = param0.readItem();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeShort(this.slotNum);
        param0.writeByte(this.buttonNum);
        param0.writeEnum(this.clickType);
        param0.writeMap(this.changedSlots, FriendlyByteBuf::writeShort, FriendlyByteBuf::writeItem);
        param0.writeItem(this.carriedItem);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleContainerClick(this);
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

    public ItemStack getCarriedItem() {
        return this.carriedItem;
    }

    public Int2ObjectMap<ItemStack> getChangedSlots() {
        return this.changedSlots;
    }

    public ClickType getClickType() {
        return this.clickType;
    }
}
