package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Serializable;

public class PoiRecord implements Serializable {
    private final BlockPos pos;
    private final PoiType poiType;
    private int freeTickets;
    private final Runnable setDirty;

    private PoiRecord(BlockPos param0, PoiType param1, int param2, Runnable param3) {
        this.pos = param0.immutable();
        this.poiType = param1;
        this.freeTickets = param2;
        this.setDirty = param3;
    }

    public PoiRecord(BlockPos param0, PoiType param1, Runnable param2) {
        this(param0, param1, param1.getMaxTickets(), param2);
    }

    public <T> PoiRecord(Dynamic<T> param0, Runnable param1) {
        this(
            param0.get("pos").map(BlockPos::deserialize).orElse(new BlockPos(0, 0, 0)),
            Registry.POINT_OF_INTEREST_TYPE.get(new ResourceLocation(param0.get("type").asString(""))),
            param0.get("free_tickets").asInt(0),
            param1
        );
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return param0.createMap(
            ImmutableMap.of(
                param0.createString("pos"),
                this.pos.serialize(param0),
                param0.createString("type"),
                param0.createString(Registry.POINT_OF_INTEREST_TYPE.getKey(this.poiType).toString()),
                param0.createString("free_tickets"),
                param0.createInt(this.freeTickets)
            )
        );
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
        if (this.freeTickets >= this.poiType.getMaxTickets()) {
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
        return this.freeTickets != this.poiType.getMaxTickets();
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public PoiType getPoiType() {
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
