import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class RedisClient {

    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
//    private final RedisCodec<String, String> codec = new StringCodec();

    public void connect(String host, int port) {
        bootstrap.group(group)
                 .channel(NioSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) throws Exception {
                         ChannelPipeline pipeline = ch.pipeline();
//                         pipeline.addLast(codec);
                         pipeline.addLast(new RedisClientHandler());
                     }
                 });

        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();
            DefaultChannelPromise defaultChannelPromise = new DefaultChannelPromise(channel);

            ChannelFuture channelFuture = channel.writeAndFlush("*2\r\n$4\r\nAUTH\r\n$6\r\n123456\r\n".getBytes());
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    Throwable cause = future.cause();
                    System.out.println(cause);
                }
            });
            System.out.println(channelFuture);
//            future.channel().writeAndFlush(codec.encode(RedisMessage.of("PING")));
//            future.channel().closeFuture().sync();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
//            group.shutdownGracefully();
        }
    }

    private class RedisClientHandler extends SimpleChannelInboundHandler<Object> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println(msg);
        }
    }

    public static void main(String[] args) {
        new RedisClient().connect("localhost", 6379);
    }
}