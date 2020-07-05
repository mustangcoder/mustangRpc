package org.mustangcoder.rpc.provider;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mustangcoder.rpc.protocol.RpcInvokeProtocol;
import org.mustangcoder.rpc.register.LocaleRegister;

import java.lang.reflect.Method;

public class ProviderHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        if (msg instanceof RpcInvokeProtocol) {
            RpcInvokeProtocol request = (RpcInvokeProtocol) msg;
            Object realService = LocaleRegister.getInstance().getProvider(request.getClassName());
            Method realMethod = realService.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            result = realMethod.invoke(realService, request.getParams());
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
