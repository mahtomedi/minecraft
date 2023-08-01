package net.minecraft.client.multiplayer;

import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.List;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.server.network.LegacyProtocolUtils;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LegacyServerPinger extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Splitter SPLITTER = Splitter.on('\u0000').limit(6);
    private final ServerAddress address;
    private final LegacyServerPinger.Output output;

    public LegacyServerPinger(ServerAddress param0, LegacyServerPinger.Output param1) {
        this.address = param0;
        this.output = param1;
    }

    @Override
    public void channelActive(ChannelHandlerContext param0) throws Exception {
        super.channelActive(param0);
        ByteBuf var0 = param0.alloc().buffer();

        try {
            var0.writeByte(254);
            var0.writeByte(1);
            var0.writeByte(250);
            LegacyProtocolUtils.writeLegacyString(var0, "MC|PingHost");
            int var1 = var0.writerIndex();
            var0.writeShort(0);
            int var2 = var0.writerIndex();
            var0.writeByte(127);
            LegacyProtocolUtils.writeLegacyString(var0, this.address.getHost());
            var0.writeInt(this.address.getPort());
            int var3 = var0.writerIndex() - var2;
            var0.setShort(var1, var3);
            param0.channel().writeAndFlush(var0).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } catch (Exception var6) {
            var0.release();
            throw var6;
        }
    }

    protected void channelRead0(ChannelHandlerContext param0, ByteBuf param1) {
        short var0 = param1.readUnsignedByte();
        if (var0 == 255) {
            String var1 = LegacyProtocolUtils.readLegacyString(param1);
            List<String> var2 = SPLITTER.splitToList(var1);
            if ("\u00a71".equals(var2.get(0))) {
                int var3 = Mth.getInt(var2.get(1), 0);
                String var4 = var2.get(2);
                String var5 = var2.get(3);
                int var6 = Mth.getInt(var2.get(4), -1);
                int var7 = Mth.getInt(var2.get(5), -1);
                this.output.handleResponse(var3, var4, var5, var6, var7);
            }
        }

        param0.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext param0, Throwable param1) {
        param0.close();
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface Output {
        void handleResponse(int var1, String var2, String var3, int var4, int var5);
    }
}
