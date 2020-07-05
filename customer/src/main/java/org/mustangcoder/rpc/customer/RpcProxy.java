package org.mustangcoder.rpc.customer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.mustangcoder.rpc.common.PropUtil;
import org.mustangcoder.rpc.protocol.RpcInvokeProtocol;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcProxy implements InvocationHandler {

    private static final RpcProxy INSTANCE = new RpcProxy();

    public static RpcProxy getInstance() {
        return INSTANCE;
    }

    public <T> T create(Class<?> cls) {
        Class<?>[] interfaces = cls.isInterface() ? new Class[]{cls} : cls.getInterfaces();
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcProxyHandler rpcProxyHandler = new RpcProxyHandler();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            RpcInvokeProtocol request = new RpcInvokeProtocol();
            request.setClassName(method.getDeclaringClass().getName());
            request.setMethodName(method.getName());
            request.setParams(args);
            request.setParamTypes(method.getParameterTypes());

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline channelPipeline = ch.pipeline();
                            channelPipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
                            channelPipeline.addLast(new ObjectEncoder());
                            channelPipeline.addLast(rpcProxyHandler);
                        }
                    });
            String host = PropUtil.getProp("rpc.provider.server.host");
            String port = PropUtil.getProp("rpc.provider.server.port");
            ChannelFuture channelFuture = bootstrap.connect(host, Integer.parseInt(port)).sync();
            channelFuture.channel().writeAndFlush(request).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            eventLoopGroup.shutdownGracefully();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
        return rpcProxyHandler.getResponse();
    }
}
