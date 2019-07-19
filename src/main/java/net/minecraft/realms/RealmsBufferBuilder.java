package net.minecraft.realms;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsBufferBuilder {
    private BufferBuilder b;

    public RealmsBufferBuilder(BufferBuilder param0) {
        this.b = param0;
    }

    public RealmsBufferBuilder from(BufferBuilder param0) {
        this.b = param0;
        return this;
    }

    public void sortQuads(float param0, float param1, float param2) {
        this.b.sortQuads(param0, param1, param2);
    }

    public void fixupQuadColor(int param0) {
        this.b.fixupQuadColor(param0);
    }

    public ByteBuffer getBuffer() {
        return this.b.getBuffer();
    }

    public void postNormal(float param0, float param1, float param2) {
        this.b.postNormal(param0, param1, param2);
    }

    public int getDrawMode() {
        return this.b.getDrawMode();
    }

    public void offset(double param0, double param1, double param2) {
        this.b.offset(param0, param1, param2);
    }

    public void restoreState(BufferBuilder.State param0) {
        this.b.restoreState(param0);
    }

    public void endVertex() {
        this.b.endVertex();
    }

    public RealmsBufferBuilder normal(float param0, float param1, float param2) {
        return this.from(this.b.normal(param0, param1, param2));
    }

    public void end() {
        this.b.end();
    }

    public void begin(int param0, VertexFormat param1) {
        this.b.begin(param0, param1);
    }

    public RealmsBufferBuilder color(int param0, int param1, int param2, int param3) {
        return this.from(this.b.color(param0, param1, param2, param3));
    }

    public void faceTex2(int param0, int param1, int param2, int param3) {
        this.b.faceTex2(param0, param1, param2, param3);
    }

    public void postProcessFacePosition(double param0, double param1, double param2) {
        this.b.postProcessFacePosition(param0, param1, param2);
    }

    public void fixupVertexColor(float param0, float param1, float param2, int param3) {
        this.b.fixupVertexColor(param0, param1, param2, param3);
    }

    public RealmsBufferBuilder color(float param0, float param1, float param2, float param3) {
        return this.from(this.b.color(param0, param1, param2, param3));
    }

    public RealmsVertexFormat getVertexFormat() {
        return new RealmsVertexFormat(this.b.getVertexFormat());
    }

    public void faceTint(float param0, float param1, float param2, int param3) {
        this.b.faceTint(param0, param1, param2, param3);
    }

    public RealmsBufferBuilder tex2(int param0, int param1) {
        return this.from(this.b.uv2(param0, param1));
    }

    public void putBulkData(int[] param0) {
        this.b.putBulkData(param0);
    }

    public RealmsBufferBuilder tex(double param0, double param1) {
        return this.from(this.b.uv(param0, param1));
    }

    public int getVertexCount() {
        return this.b.getVertexCount();
    }

    public void clear() {
        this.b.clear();
    }

    public RealmsBufferBuilder vertex(double param0, double param1, double param2) {
        return this.from(this.b.vertex(param0, param1, param2));
    }

    public void fixupQuadColor(float param0, float param1, float param2) {
        this.b.fixupQuadColor(param0, param1, param2);
    }

    public void noColor() {
        this.b.noColor();
    }
}
