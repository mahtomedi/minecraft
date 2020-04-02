package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundUpdateAttributesPacket implements Packet<ClientGamePacketListener> {
    private int entityId;
    private final List<ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes = Lists.newArrayList();

    public ClientboundUpdateAttributesPacket() {
    }

    public ClientboundUpdateAttributesPacket(int param0, Collection<AttributeInstance> param1) {
        this.entityId = param0;

        for(AttributeInstance var0 : param1) {
            this.attributes.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(var0.getAttribute(), var0.getBaseValue(), var0.getModifiers()));
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.entityId = param0.readVarInt();
        int var0 = param0.readInt();

        for(int var1 = 0; var1 < var0; ++var1) {
            ResourceLocation var2 = param0.readResourceLocation();
            Attribute var3 = Registry.ATTRIBUTES.get(var2);
            double var4 = param0.readDouble();
            List<AttributeModifier> var5 = Lists.newArrayList();
            int var6 = param0.readVarInt();

            for(int var7 = 0; var7 < var6; ++var7) {
                UUID var8 = param0.readUUID();
                var5.add(
                    new AttributeModifier(
                        var8, "Unknown synced attribute modifier", param0.readDouble(), AttributeModifier.Operation.fromValue(param0.readByte())
                    )
                );
            }

            this.attributes.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(var3, var4, var5));
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.entityId);
        param0.writeInt(this.attributes.size());

        for(ClientboundUpdateAttributesPacket.AttributeSnapshot var0 : this.attributes) {
            param0.writeResourceLocation(Registry.ATTRIBUTES.getKey(var0.getAttribute()));
            param0.writeDouble(var0.getBase());
            param0.writeVarInt(var0.getModifiers().size());

            for(AttributeModifier var1 : var0.getModifiers()) {
                param0.writeUUID(var1.getId());
                param0.writeDouble(var1.getAmount());
                param0.writeByte(var1.getOperation().toValue());
            }
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateAttributes(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }

    @OnlyIn(Dist.CLIENT)
    public List<ClientboundUpdateAttributesPacket.AttributeSnapshot> getValues() {
        return this.attributes;
    }

    public class AttributeSnapshot {
        private final Attribute attribute;
        private final double base;
        private final Collection<AttributeModifier> modifiers;

        public AttributeSnapshot(Attribute param1, double param2, Collection<AttributeModifier> param3) {
            this.attribute = param1;
            this.base = param2;
            this.modifiers = param3;
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
