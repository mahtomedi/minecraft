package net.minecraft.world.phys;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AABB {
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AABB(double param0, double param1, double param2, double param3, double param4, double param5) {
        this.minX = Math.min(param0, param3);
        this.minY = Math.min(param1, param4);
        this.minZ = Math.min(param2, param5);
        this.maxX = Math.max(param0, param3);
        this.maxY = Math.max(param1, param4);
        this.maxZ = Math.max(param2, param5);
    }

    public AABB(BlockPos param0) {
        this(
            (double)param0.getX(),
            (double)param0.getY(),
            (double)param0.getZ(),
            (double)(param0.getX() + 1),
            (double)(param0.getY() + 1),
            (double)(param0.getZ() + 1)
        );
    }

    public AABB(BlockPos param0, BlockPos param1) {
        this((double)param0.getX(), (double)param0.getY(), (double)param0.getZ(), (double)param1.getX(), (double)param1.getY(), (double)param1.getZ());
    }

    public AABB(Vec3 param0, Vec3 param1) {
        this(param0.x, param0.y, param0.z, param1.x, param1.y, param1.z);
    }

    public static AABB of(BoundingBox param0) {
        return new AABB((double)param0.x0, (double)param0.y0, (double)param0.z0, (double)(param0.x1 + 1), (double)(param0.y1 + 1), (double)(param0.z1 + 1));
    }

    public static AABB unitCubeFromLowerCorner(Vec3 param0) {
        return new AABB(param0.x, param0.y, param0.z, param0.x + 1.0, param0.y + 1.0, param0.z + 1.0);
    }

    public double min(Direction.Axis param0) {
        return param0.choose(this.minX, this.minY, this.minZ);
    }

    public double max(Direction.Axis param0) {
        return param0.choose(this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof AABB)) {
            return false;
        } else {
            AABB var0 = (AABB)param0;
            if (Double.compare(var0.minX, this.minX) != 0) {
                return false;
            } else if (Double.compare(var0.minY, this.minY) != 0) {
                return false;
            } else if (Double.compare(var0.minZ, this.minZ) != 0) {
                return false;
            } else if (Double.compare(var0.maxX, this.maxX) != 0) {
                return false;
            } else if (Double.compare(var0.maxY, this.maxY) != 0) {
                return false;
            } else {
                return Double.compare(var0.maxZ, this.maxZ) == 0;
            }
        }
    }

    @Override
    public int hashCode() {
        long var0 = Double.doubleToLongBits(this.minX);
        int var1 = (int)(var0 ^ var0 >>> 32);
        var0 = Double.doubleToLongBits(this.minY);
        var1 = 31 * var1 + (int)(var0 ^ var0 >>> 32);
        var0 = Double.doubleToLongBits(this.minZ);
        var1 = 31 * var1 + (int)(var0 ^ var0 >>> 32);
        var0 = Double.doubleToLongBits(this.maxX);
        var1 = 31 * var1 + (int)(var0 ^ var0 >>> 32);
        var0 = Double.doubleToLongBits(this.maxY);
        var1 = 31 * var1 + (int)(var0 ^ var0 >>> 32);
        var0 = Double.doubleToLongBits(this.maxZ);
        return 31 * var1 + (int)(var0 ^ var0 >>> 32);
    }

    public AABB contract(double param0, double param1, double param2) {
        double var0 = this.minX;
        double var1 = this.minY;
        double var2 = this.minZ;
        double var3 = this.maxX;
        double var4 = this.maxY;
        double var5 = this.maxZ;
        if (param0 < 0.0) {
            var0 -= param0;
        } else if (param0 > 0.0) {
            var3 -= param0;
        }

        if (param1 < 0.0) {
            var1 -= param1;
        } else if (param1 > 0.0) {
            var4 -= param1;
        }

        if (param2 < 0.0) {
            var2 -= param2;
        } else if (param2 > 0.0) {
            var5 -= param2;
        }

        return new AABB(var0, var1, var2, var3, var4, var5);
    }

    public AABB expandTowards(Vec3 param0) {
        return this.expandTowards(param0.x, param0.y, param0.z);
    }

    public AABB expandTowards(double param0, double param1, double param2) {
        double var0 = this.minX;
        double var1 = this.minY;
        double var2 = this.minZ;
        double var3 = this.maxX;
        double var4 = this.maxY;
        double var5 = this.maxZ;
        if (param0 < 0.0) {
            var0 += param0;
        } else if (param0 > 0.0) {
            var3 += param0;
        }

        if (param1 < 0.0) {
            var1 += param1;
        } else if (param1 > 0.0) {
            var4 += param1;
        }

        if (param2 < 0.0) {
            var2 += param2;
        } else if (param2 > 0.0) {
            var5 += param2;
        }

        return new AABB(var0, var1, var2, var3, var4, var5);
    }

    public AABB inflate(double param0, double param1, double param2) {
        double var0 = this.minX - param0;
        double var1 = this.minY - param1;
        double var2 = this.minZ - param2;
        double var3 = this.maxX + param0;
        double var4 = this.maxY + param1;
        double var5 = this.maxZ + param2;
        return new AABB(var0, var1, var2, var3, var4, var5);
    }

    public AABB inflate(double param0) {
        return this.inflate(param0, param0, param0);
    }

    public AABB intersect(AABB param0) {
        double var0 = Math.max(this.minX, param0.minX);
        double var1 = Math.max(this.minY, param0.minY);
        double var2 = Math.max(this.minZ, param0.minZ);
        double var3 = Math.min(this.maxX, param0.maxX);
        double var4 = Math.min(this.maxY, param0.maxY);
        double var5 = Math.min(this.maxZ, param0.maxZ);
        return new AABB(var0, var1, var2, var3, var4, var5);
    }

    public AABB minmax(AABB param0) {
        double var0 = Math.min(this.minX, param0.minX);
        double var1 = Math.min(this.minY, param0.minY);
        double var2 = Math.min(this.minZ, param0.minZ);
        double var3 = Math.max(this.maxX, param0.maxX);
        double var4 = Math.max(this.maxY, param0.maxY);
        double var5 = Math.max(this.maxZ, param0.maxZ);
        return new AABB(var0, var1, var2, var3, var4, var5);
    }

    public AABB move(double param0, double param1, double param2) {
        return new AABB(this.minX + param0, this.minY + param1, this.minZ + param2, this.maxX + param0, this.maxY + param1, this.maxZ + param2);
    }

    public AABB move(BlockPos param0) {
        return new AABB(
            this.minX + (double)param0.getX(),
            this.minY + (double)param0.getY(),
            this.minZ + (double)param0.getZ(),
            this.maxX + (double)param0.getX(),
            this.maxY + (double)param0.getY(),
            this.maxZ + (double)param0.getZ()
        );
    }

    public AABB move(Vec3 param0) {
        return this.move(param0.x, param0.y, param0.z);
    }

    public boolean intersects(AABB param0) {
        return this.intersects(param0.minX, param0.minY, param0.minZ, param0.maxX, param0.maxY, param0.maxZ);
    }

    public boolean intersects(double param0, double param1, double param2, double param3, double param4, double param5) {
        return this.minX < param3 && this.maxX > param0 && this.minY < param4 && this.maxY > param1 && this.minZ < param5 && this.maxZ > param2;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean intersects(Vec3 param0, Vec3 param1) {
        return this.intersects(
            Math.min(param0.x, param1.x),
            Math.min(param0.y, param1.y),
            Math.min(param0.z, param1.z),
            Math.max(param0.x, param1.x),
            Math.max(param0.y, param1.y),
            Math.max(param0.z, param1.z)
        );
    }

    public boolean contains(Vec3 param0) {
        return this.contains(param0.x, param0.y, param0.z);
    }

    public boolean contains(double param0, double param1, double param2) {
        return param0 >= this.minX && param0 < this.maxX && param1 >= this.minY && param1 < this.maxY && param2 >= this.minZ && param2 < this.maxZ;
    }

    public double getSize() {
        double var0 = this.getXsize();
        double var1 = this.getYsize();
        double var2 = this.getZsize();
        return (var0 + var1 + var2) / 3.0;
    }

    public double getXsize() {
        return this.maxX - this.minX;
    }

    public double getYsize() {
        return this.maxY - this.minY;
    }

    public double getZsize() {
        return this.maxZ - this.minZ;
    }

    public AABB deflate(double param0) {
        return this.inflate(-param0);
    }

    public Optional<Vec3> clip(Vec3 param0, Vec3 param1) {
        double[] var0 = new double[]{1.0};
        double var1 = param1.x - param0.x;
        double var2 = param1.y - param0.y;
        double var3 = param1.z - param0.z;
        Direction var4 = getDirection(this, param0, var0, null, var1, var2, var3);
        if (var4 == null) {
            return Optional.empty();
        } else {
            double var5 = var0[0];
            return Optional.of(param0.add(var5 * var1, var5 * var2, var5 * var3));
        }
    }

    @Nullable
    public static BlockHitResult clip(Iterable<AABB> param0, Vec3 param1, Vec3 param2, BlockPos param3) {
        double[] var0 = new double[]{1.0};
        Direction var1 = null;
        double var2 = param2.x - param1.x;
        double var3 = param2.y - param1.y;
        double var4 = param2.z - param1.z;

        for(AABB var5 : param0) {
            var1 = getDirection(var5.move(param3), param1, var0, var1, var2, var3, var4);
        }

        if (var1 == null) {
            return null;
        } else {
            double var6 = var0[0];
            return new BlockHitResult(param1.add(var6 * var2, var6 * var3, var6 * var4), var1, param3, false);
        }
    }

    @Nullable
    private static Direction getDirection(AABB param0, Vec3 param1, double[] param2, @Nullable Direction param3, double param4, double param5, double param6) {
        if (param4 > 1.0E-7) {
            param3 = clipPoint(
                param2,
                param3,
                param4,
                param5,
                param6,
                param0.minX,
                param0.minY,
                param0.maxY,
                param0.minZ,
                param0.maxZ,
                Direction.WEST,
                param1.x,
                param1.y,
                param1.z
            );
        } else if (param4 < -1.0E-7) {
            param3 = clipPoint(
                param2,
                param3,
                param4,
                param5,
                param6,
                param0.maxX,
                param0.minY,
                param0.maxY,
                param0.minZ,
                param0.maxZ,
                Direction.EAST,
                param1.x,
                param1.y,
                param1.z
            );
        }

        if (param5 > 1.0E-7) {
            param3 = clipPoint(
                param2,
                param3,
                param5,
                param6,
                param4,
                param0.minY,
                param0.minZ,
                param0.maxZ,
                param0.minX,
                param0.maxX,
                Direction.DOWN,
                param1.y,
                param1.z,
                param1.x
            );
        } else if (param5 < -1.0E-7) {
            param3 = clipPoint(
                param2,
                param3,
                param5,
                param6,
                param4,
                param0.maxY,
                param0.minZ,
                param0.maxZ,
                param0.minX,
                param0.maxX,
                Direction.UP,
                param1.y,
                param1.z,
                param1.x
            );
        }

        if (param6 > 1.0E-7) {
            param3 = clipPoint(
                param2,
                param3,
                param6,
                param4,
                param5,
                param0.minZ,
                param0.minX,
                param0.maxX,
                param0.minY,
                param0.maxY,
                Direction.NORTH,
                param1.z,
                param1.x,
                param1.y
            );
        } else if (param6 < -1.0E-7) {
            param3 = clipPoint(
                param2,
                param3,
                param6,
                param4,
                param5,
                param0.maxZ,
                param0.minX,
                param0.maxX,
                param0.minY,
                param0.maxY,
                Direction.SOUTH,
                param1.z,
                param1.x,
                param1.y
            );
        }

        return param3;
    }

    @Nullable
    private static Direction clipPoint(
        double[] param0,
        @Nullable Direction param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        double param7,
        double param8,
        double param9,
        Direction param10,
        double param11,
        double param12,
        double param13
    ) {
        double var0 = (param5 - param11) / param2;
        double var1 = param12 + var0 * param3;
        double var2 = param13 + var0 * param4;
        if (0.0 < var0 && var0 < param0[0] && param6 - 1.0E-7 < var1 && var1 < param7 + 1.0E-7 && param8 - 1.0E-7 < var2 && var2 < param9 + 1.0E-7) {
            param0[0] = var0;
            return param10;
        } else {
            return param1;
        }
    }

    @Override
    public String toString() {
        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasNaN() {
        return Double.isNaN(this.minX)
            || Double.isNaN(this.minY)
            || Double.isNaN(this.minZ)
            || Double.isNaN(this.maxX)
            || Double.isNaN(this.maxY)
            || Double.isNaN(this.maxZ);
    }

    public Vec3 getCenter() {
        return new Vec3(Mth.lerp(0.5, this.minX, this.maxX), Mth.lerp(0.5, this.minY, this.maxY), Mth.lerp(0.5, this.minZ, this.maxZ));
    }
}
