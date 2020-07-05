package org.mustangcoder.rpc.provider.boot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.mustangcoder.rpc.common.PropUtil;
import org.mustangcoder.rpc.provider.ProviderHandler;
import org.mustangcoder.rpc.provider.annotation.RpcProvider;
import org.mustangcoder.rpc.register.LocaleRegister;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocalBoot {

    private static final List<String> CLASS_NAME_LIST = new ArrayList<>();

    private static void scanClass(String packageName) {
        packageName = packageName == null ? "" : packageName;
        URL url = LocalBoot.class.getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (packageName.length() == 0) {
                    scanClass(file.getName());
                } else {
                    scanClass(packageName + "." + file.getName());
                }
            } else {
                if (file.getName().endsWith(".class")) {
                    CLASS_NAME_LIST.add(packageName + "." + file.getName().replace(".class", ""));
                }
            }
        }
    }

    private static void registerProvider() {
        for (String className : CLASS_NAME_LIST) {
            try {
                Class<?> cls = Class.forName(className);
                Annotation[] annotationList = cls.getAnnotations();
                for (Annotation an : annotationList) {
                    if (an.annotationType() == RpcProvider.class) {
                        String interfaceName = cls.getInterfaces()[0].getName();
                        LocaleRegister.getInstance().register(interfaceName, cls.newInstance());
                    }
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        try {
            scanClass("");
            registerProvider();
            int port = Integer.parseInt(PropUtil.getProp("rpc.provider.server.port"));
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ObjectEncoder());
                            socketChannel.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE,
                                    ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
                            socketChannel.pipeline().addLast(new ProviderHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
            System.out.println("Mustang Rpc Local Provider started! port:" + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
