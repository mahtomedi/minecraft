package com.mojang.math;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.util.StringRepresentable;
import org.joml.Matrix3f;

public enum OctahedralGroup implements StringRepresentable {
    IDENTITY("identity", SymmetricGroup3.P123, false, false, false),
    ROT_180_FACE_XY("rot_180_face_xy", SymmetricGroup3.P123, true, true, false),
    ROT_180_FACE_XZ("rot_180_face_xz", SymmetricGroup3.P123, true, false, true),
    ROT_180_FACE_YZ("rot_180_face_yz", SymmetricGroup3.P123, false, true, true),
    ROT_120_NNN("rot_120_nnn", SymmetricGroup3.P231, false, false, false),
    ROT_120_NNP("rot_120_nnp", SymmetricGroup3.P312, true, false, true),
    ROT_120_NPN("rot_120_npn", SymmetricGroup3.P312, false, true, true),
    ROT_120_NPP("rot_120_npp", SymmetricGroup3.P231, true, false, true),
    ROT_120_PNN("rot_120_pnn", SymmetricGroup3.P312, true, true, false),
    ROT_120_PNP("rot_120_pnp", SymmetricGroup3.P231, true, true, false),
    ROT_120_PPN("rot_120_ppn", SymmetricGroup3.P231, false, true, true),
    ROT_120_PPP("rot_120_ppp", SymmetricGroup3.P312, false, false, false),
    ROT_180_EDGE_XY_NEG("rot_180_edge_xy_neg", SymmetricGroup3.P213, true, true, true),
    ROT_180_EDGE_XY_POS("rot_180_edge_xy_pos", SymmetricGroup3.P213, false, false, true),
    ROT_180_EDGE_XZ_NEG("rot_180_edge_xz_neg", SymmetricGroup3.P321, true, true, true),
    ROT_180_EDGE_XZ_POS("rot_180_edge_xz_pos", SymmetricGroup3.P321, false, true, false),
    ROT_180_EDGE_YZ_NEG("rot_180_edge_yz_neg", SymmetricGroup3.P132, true, true, true),
    ROT_180_EDGE_YZ_POS("rot_180_edge_yz_pos", SymmetricGroup3.P132, true, false, false),
    ROT_90_X_NEG("rot_90_x_neg", SymmetricGroup3.P132, false, false, true),
    ROT_90_X_POS("rot_90_x_pos", SymmetricGroup3.P132, false, true, false),
    ROT_90_Y_NEG("rot_90_y_neg", SymmetricGroup3.P321, true, false, false),
    ROT_90_Y_POS("rot_90_y_pos", SymmetricGroup3.P321, false, false, true),
    ROT_90_Z_NEG("rot_90_z_neg", SymmetricGroup3.P213, false, true, false),
    ROT_90_Z_POS("rot_90_z_pos", SymmetricGroup3.P213, true, false, false),
    INVERSION("inversion", SymmetricGroup3.P123, true, true, true),
    INVERT_X("invert_x", SymmetricGroup3.P123, true, false, false),
    INVERT_Y("invert_y", SymmetricGroup3.P123, false, true, false),
    INVERT_Z("invert_z", SymmetricGroup3.P123, false, false, true),
    ROT_60_REF_NNN("rot_60_ref_nnn", SymmetricGroup3.P312, true, true, true),
    ROT_60_REF_NNP("rot_60_ref_nnp", SymmetricGroup3.P231, true, false, false),
    ROT_60_REF_NPN("rot_60_ref_npn", SymmetricGroup3.P231, false, false, true),
    ROT_60_REF_NPP("rot_60_ref_npp", SymmetricGroup3.P312, false, false, true),
    ROT_60_REF_PNN("rot_60_ref_pnn", SymmetricGroup3.P231, false, true, false),
    ROT_60_REF_PNP("rot_60_ref_pnp", SymmetricGroup3.P312, true, false, false),
    ROT_60_REF_PPN("rot_60_ref_ppn", SymmetricGroup3.P312, false, true, false),
    ROT_60_REF_PPP("rot_60_ref_ppp", SymmetricGroup3.P231, true, true, true),
    SWAP_XY("swap_xy", SymmetricGroup3.P213, false, false, false),
    SWAP_YZ("swap_yz", SymmetricGroup3.P132, false, false, false),
    SWAP_XZ("swap_xz", SymmetricGroup3.P321, false, false, false),
    SWAP_NEG_XY("swap_neg_xy", SymmetricGroup3.P213, true, true, false),
    SWAP_NEG_YZ("swap_neg_yz", SymmetricGroup3.P132, false, true, true),
    SWAP_NEG_XZ("swap_neg_xz", SymmetricGroup3.P321, true, false, true),
    ROT_90_REF_X_NEG("rot_90_ref_x_neg", SymmetricGroup3.P132, true, false, true),
    ROT_90_REF_X_POS("rot_90_ref_x_pos", SymmetricGroup3.P132, true, true, false),
    ROT_90_REF_Y_NEG("rot_90_ref_y_neg", SymmetricGroup3.P321, true, true, false),
    ROT_90_REF_Y_POS("rot_90_ref_y_pos", SymmetricGroup3.P321, false, true, true),
    ROT_90_REF_Z_NEG("rot_90_ref_z_neg", SymmetricGroup3.P213, false, true, true),
    ROT_90_REF_Z_POS("rot_90_ref_z_pos", SymmetricGroup3.P213, true, false, true);

    private final Matrix3f transformation;
    private final String name;
    @Nullable
    private Map<Direction, Direction> rotatedDirections;
    private final boolean invertX;
    private final boolean invertY;
    private final boolean invertZ;
    private final SymmetricGroup3 permutation;
    private static final OctahedralGroup[][] cayleyTable = Util.make(
        new OctahedralGroup[values().length][values().length],
        param0 -> {
            Map<Pair<SymmetricGroup3, BooleanList>, OctahedralGroup> var0 = Arrays.stream(values())
                .collect(Collectors.toMap(param0x -> Pair.of(param0x.permutation, param0x.packInversions()), param0x -> param0x));
    
            for(OctahedralGroup var1 : values()) {
                for(OctahedralGroup var2 : values()) {
                    BooleanList var3 = var1.packInversions();
                    BooleanList var4 = var2.packInversions();
                    SymmetricGroup3 var5 = var2.permutation.compose(var1.permutation);
                    BooleanArrayList var6 = new BooleanArrayList(3);
    
                    for(int var7 = 0; var7 < 3; ++var7) {
                        var6.add(var3.getBoolean(var7) ^ var4.getBoolean(var1.permutation.permutation(var7)));
                    }
    
                    param0[var1.ordinal()][var2.ordinal()] = var0.get(Pair.of(var5, var6));
                }
            }
    
        }
    );
    private static final OctahedralGroup[] inverseTable = Arrays.stream(values())
        .map(param0 -> Arrays.stream(values()).filter(param1 -> param0.compose(param1) == IDENTITY).findAny().get())
        .toArray(param0 -> new OctahedralGroup[param0]);

    private OctahedralGroup(String param0, SymmetricGroup3 param1, boolean param2, boolean param3, boolean param4) {
        this.name = param0;
        this.invertX = param2;
        this.invertY = param3;
        this.invertZ = param4;
        this.permutation = param1;
        this.transformation = new Matrix3f().scaling(param2 ? -1.0F : 1.0F, param3 ? -1.0F : 1.0F, param4 ? -1.0F : 1.0F);
        this.transformation.mul(param1.transformation());
    }

    private BooleanList packInversions() {
        return new BooleanArrayList(new boolean[]{this.invertX, this.invertY, this.invertZ});
    }

    public OctahedralGroup compose(OctahedralGroup param0) {
        return cayleyTable[this.ordinal()][param0.ordinal()];
    }

    public OctahedralGroup inverse() {
        return inverseTable[this.ordinal()];
    }

    public Matrix3f transformation() {
        return new Matrix3f(this.transformation);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Direction rotate(Direction param0) {
        if (this.rotatedDirections == null) {
            this.rotatedDirections = Maps.newEnumMap(Direction.class);

            for(Direction var0 : Direction.values()) {
                Direction.Axis var1 = var0.getAxis();
                Direction.AxisDirection var2 = var0.getAxisDirection();
                Direction.Axis var3 = Direction.Axis.values()[this.permutation.permutation(var1.ordinal())];
                Direction.AxisDirection var4 = this.inverts(var3) ? var2.opposite() : var2;
                Direction var5 = Direction.fromAxisAndDirection(var3, var4);
                this.rotatedDirections.put(var0, var5);
            }
        }

        return this.rotatedDirections.get(param0);
    }

    public boolean inverts(Direction.Axis param0) {
        switch(param0) {
            case X:
                return this.invertX;
            case Y:
                return this.invertY;
            case Z:
            default:
                return this.invertZ;
        }
    }

    public FrontAndTop rotate(FrontAndTop param0) {
        return FrontAndTop.fromFrontAndTop(this.rotate(param0.front()), this.rotate(param0.top()));
    }
}
