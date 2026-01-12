package com.shixun.simpleimserver.netty;

import com.shixun.simpleimserver.netty.handler.IMWebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class NettyServer implements CommandLineRunner {

    private static final int PORT = 8888; // WebSocket 端口

    @Autowired
    private IMWebSocketHandler imWebSocketHandler; // 注入刚才写的 Handler

    @Override
    public void run(String... args) throws Exception {
        // 新开一个线程启动 Netty，否则会阻塞 Spring 主线程
        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();

                                // 1. HTTP 编解码 (WebSocket 握手基于 HTTP)
                                pipeline.addLast(new HttpServerCodec());
                                // 2. 以块方式写入
                                pipeline.addLast(new ChunkedWriteHandler());
                                // 3. HTTP 消息聚合 (处理 FullHttpRequest)
                                pipeline.addLast(new HttpObjectAggregator(64 * 1024));

                                // 4. WebSocket 协议处理器
                                // 处理握手、Ping/Pong，路径为 ws://localhost:8888/im
                                pipeline.addLast(new WebSocketServerProtocolHandler("/im"));

                                // 5. 自定义业务逻辑
                                pipeline.addLast(imWebSocketHandler);
                            }
                        });

                System.out.println(">>> Netty WebSocket 服务启动，端口: " + PORT);
                ChannelFuture f = b.bind(PORT).sync();
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }
}