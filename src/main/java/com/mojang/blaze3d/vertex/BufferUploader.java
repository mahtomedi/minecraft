package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BufferUploader {
    public void end(BufferBuilder param0) {
        if (param0.getVertexCount() > 0) {
            VertexFormat var0 = param0.getVertexFormat();
            int var1 = var0.getVertexSize();
            ByteBuffer var2 = param0.getBuffer();
            List<VertexFormatElement> var3 = var0.getElements();

            for(int var4 = 0; var4 < var3.size(); ++var4) {
                VertexFormatElement var5 = var3.get(var4);
                VertexFormatElement.Usage var6 = var5.getUsage();
                int var7 = var5.getType().getGlType();
                int var8 = var5.getIndex();
                ((Buffer)var2).position(var0.getOffset(var4));
                switch(var6) {
                    case POSITION:
                        RenderSystem.vertexPointer(var5.getCount(), var7, var1, var2);
                        RenderSystem.enableClientState(32884);
                        break;
                    case UV:
                        RenderSystem.glClientActiveTexture(33984 + var8);
                        RenderSystem.texCoordPointer(var5.getCount(), var7, var1, var2);
                        RenderSystem.enableClientState(32888);
                        RenderSystem.glClientActiveTexture(33984);
                        break;
                    case COLOR:
                        RenderSystem.colorPointer(var5.getCount(), var7, var1, var2);
                        RenderSystem.enableClientState(32886);
                        break;
                    case NORMAL:
                        RenderSystem.normalPointer(var7, var1, var2);
                        RenderSystem.enableClientState(32885);
                }
            }

            RenderSystem.drawArrays(param0.getDrawMode(), 0, param0.getVertexCount());
            int var9 = 0;

            for(int var10 = var3.size(); var9 < var10; ++var9) {
                VertexFormatElement var11 = var3.get(var9);
                VertexFormatElement.Usage var12 = var11.getUsage();
                int var13 = var11.getIndex();
                switch(var12) {
                    case POSITION:
                        RenderSystem.disableClientState(32884);
                        break;
                    case UV:
                        RenderSystem.glClientActiveTexture(33984 + var13);
                        RenderSystem.disableClientState(32888);
                        RenderSystem.glClientActiveTexture(33984);
                        break;
                    case COLOR:
                        RenderSystem.disableClientState(32886);
                        RenderSystem.clearCurrentColor();
                        break;
                    case NORMAL:
                        RenderSystem.disableClientState(32885);
                }
            }
        }

        param0.clear();
    }
}
