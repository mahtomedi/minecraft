package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.IntFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class ServerboundContainerClickPacket implements Packet<ServerGamePacketListener> {
    private static final int MAX_SLOT_COUNT = 128;
    private final int containerId;
    private final int stateId;
    private final int slotNum;
    private final int buttonNum;
    private final ClickType clickType;
    private final ItemStack carriedItem;
    private final Int2ObjectMap<ItemStack> changedSlots;

    public ServerboundContainerClickPacket(int param0, int param1, int param2, int param3, ClickType param4, ItemStack param5, Int2ObjectMap<ItemStack> param6) {
        this.containerId = param0;
        this.stateId = param1;
        this.slotNum = param2;
        this.buttonNum = param3;
        this.clickType = param4;
        this.carriedItem = param5;
        this.changedSlots = Int2ObjectMaps.unmodifiable(param6);
    }

    public ServerboundContainerClickPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readByte();
        this.stateId = param0.readVarInt();
        this.slotNum = param0.readShort();
        this.buttonNum = param0.readByte();
        this.clickType = param0.readEnum(ClickType.class);
        IntFunction<Int2ObjectOpenHashMap<ItemStack>> var0 = FriendlyByteBuf.limitValue(Int2ObjectOpenHashMap::new, 128);
        this.changedSlots = Int2ObjectMaps.unmodifiable(param0.readMap(var0, param0x -> Integer.valueOf(param0x.readShort()), FriendlyByteBuf::readItem));
        this.carriedItem = param0.readItem();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeVarInt(this.stateId);
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

    public int getStateId() {
        return this.stateId;
    }
}
