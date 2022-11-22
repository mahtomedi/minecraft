package net.minecraft.world.entity.ai.village.poi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.VisibleForDebug;

public class PoiRecord {
    private final BlockPos pos;
    private final Holder<PoiType> poiType;
    private int freeTickets;
    private final Runnable setDirty;

    public static Codec<PoiRecord> codec(Runnable param0) {
        return RecordCodecBuilder.create(
            param1 -> param1.group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(param0x -> param0x.pos),
                        RegistryFixedCodec.create(Registries.POINT_OF_INTEREST_TYPE).fieldOf("type").forGetter(param0x -> param0x.poiType),
                        Codec.INT.fieldOf("free_tickets").orElse(0).forGetter(param0x -> param0x.freeTickets),
                        RecordCodecBuilder.point(param0)
                    )
                    .apply(param1, PoiRecord::new)
        );
    }

    private PoiRecord(BlockPos param0, Holder<PoiType> param1, int param2, Runnable param3) {
        this.pos = param0.immutable();
        this.poiType = param1;
        this.freeTickets = param2;
        this.setDirty = param3;
    }

    public PoiRecord(BlockPos param0, Holder<PoiType> param1, Runnable param2) {
        this(param0, param1, ((PoiType)param1.value()).maxTickets(), param2);
    }

    @Deprecated
    @VisibleForDebug
    public int getFreeTickets() {
        return this.freeTickets;
    }

    protected boolean acquireTicket() {
        if (this.freeTickets <= 0) {
            return false;
        } else {
            --this.freeTickets;
            this.setDirty.run();
            return true;
        }
    }

    protected boolean releaseTicket() {
        if (this.freeTickets >= ((PoiType)this.poiType.value()).maxTickets()) {
            return false;
        } else {
            ++this.freeTickets;
            this.setDirty.run();
            return true;
        }
    }

    public boolean hasSpace() {
        return this.freeTickets > 0;
    }

    public boolean isOccupied() {
        return this.freeTickets != ((PoiType)this.poiType.value()).maxTickets();
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Holder<PoiType> getPoiType() {
        return this.poiType;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 != null && this.getClass() == param0.getClass() ? Objects.equals(this.pos, ((PoiRecord)param0).pos) : false;
        }
    }

    @Override
    public int hashCode() {
        return this.pos.hashCode();
    }
}
