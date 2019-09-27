package net.minecraft.client.resources.model;

import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum BlockModelRotation implements ModelState {
    X0_Y0(0, 0),
    X0_Y90(0, 90),
    X0_Y180(0, 180),
    X0_Y270(0, 270),
    X90_Y0(90, 0),
    X90_Y90(90, 90),
    X90_Y180(90, 180),
    X90_Y270(90, 270),
    X180_Y0(180, 0),
    X180_Y90(180, 90),
    X180_Y180(180, 180),
    X180_Y270(180, 270),
    X270_Y0(270, 0),
    X270_Y90(270, 90),
    X270_Y180(270, 180),
    X270_Y270(270, 270);

    private static final Map<Integer, BlockModelRotation> BY_INDEX = Arrays.stream(values())
        .collect(Collectors.toMap(param0 -> param0.index, param0 -> param0));
    private final int index;
    private final Quaternion rotation;
    private final int xSteps;
    private final int ySteps;

    private static int getIndex(int param0, int param1) {
        return param0 * 360 + param1;
    }

    private BlockModelRotation(int param0, int param1) {
        this.index = getIndex(param0, param1);
        Quaternion param2 = new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float)(-param1), true);
        param2.mul(new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), (float)(-param0), true));
        this.rotation = param2;
        this.xSteps = Mth.abs(param0 / 90);
        this.ySteps = Mth.abs(param1 / 90);
    }

    @Override
    public Transformation getRotation() {
        return new Transformation(null, this.rotation, null, null);
    }

    public static BlockModelRotation by(int param0, int param1) {
        return BY_INDEX.get(getIndex(Mth.positiveModulo(param0, 360), Mth.positiveModulo(param1, 360)));
    }
}
