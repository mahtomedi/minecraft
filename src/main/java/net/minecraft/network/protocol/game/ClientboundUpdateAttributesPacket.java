package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ClientboundUpdateAttributesPacket implements Packet<ClientGamePacketListener> {
    private final int entityId;
    private final List<ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes;

    public ClientboundUpdateAttributesPacket(int param0, Collection<AttributeInstance> param1) {
        this.entityId = param0;
        this.attributes = Lists.newArrayList();

        for(AttributeInstance var0 : param1) {
            this.attributes.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(var0.getAttribute(), var0.getBaseValue(), var0.getModifiers()));
        }

    }

    public ClientboundUpdateAttributesPacket(FriendlyByteBuf param0) {
        this.entityId = param0.readVarInt();
        this.attributes = param0.readList(
            param0x -> {
                ResourceLocation var0 = param0x.readResourceLocation();
                Attribute var1x = BuiltInRegistries.ATTRIBUTE.get(var0);
                double var2 = param0x.readDouble();
                List<AttributeModifier> var3 = param0x.readList(
                    param0xx -> new AttributeModifier(
                            param0xx.readUUID(),
                            "Unknown synced attribute modifier",
                            param0xx.readDouble(),
                            AttributeModifier.Operation.fromValue(param0xx.readByte())
                        )
                );
                return new ClientboundUpdateAttributesPacket.AttributeSnapshot(var1x, var2, var3);
            }
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entityId);
        param0.writeCollection(this.attributes, (param0x, param1) -> {
            param0x.writeResourceLocation(BuiltInRegistries.ATTRIBUTE.getKey(param1.getAttribute()));
            param0x.writeDouble(param1.getBase());
            param0x.writeCollection(param1.getModifiers(), (param0xx, param1x) -> {
                param0xx.writeUUID(param1x.getId());
                param0xx.writeDouble(param1x.getAmount());
                param0xx.writeByte(param1x.getOperation().toValue());
            });
        });
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateAttributes(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public List<ClientboundUpdateAttributesPacket.AttributeSnapshot> getValues() {
        return this.attributes;
    }

    public static class AttributeSnapshot {
        private final Attribute attribute;
        private final double base;
        private final Collection<AttributeModifier> modifiers;

        public AttributeSnapshot(Attribute param0, double param1, Collection<AttributeModifier> param2) {
            this.attribute = param0;
            this.base = param1;
            this.modifiers = param2;
        }

        public Attribute getAttribute() {
            return this.attribute;
        }

        public double getBase() {
            return this.base;
        }

        public Collection<AttributeModifier> getModifiers() {
            return this.modifiers;
        }
    }
}
