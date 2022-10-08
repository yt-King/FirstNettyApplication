import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 这种使用 ChannelHandler 的方式体现了关注点分离的设计原则，并简化业务逻辑的迭代开发的要求。
 * 处理程序很简单;它的每一个方法可以覆盖到“hook（钩子）”在活动周期适当的点
 */
@ChannelHandler.Sharable //1、标识这类的实例之间可以在 channel 里面共享
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * 覆盖 channelRead因为我们需要处理所有接收到的数据。
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("Server received: " + in.toString(CharsetUtil.UTF_8));//2、日志消息输出到控制台
        ctx.write(in);                           //3、将所接收的消息返回给发送者。注意，这还没有冲刷数据
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)//4、冲刷所有待审消息到远程节点。关闭通道后，操作完成
                .addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 覆盖 exceptionCaught 使我们能够应对任何 Throwable 的子类型。在这种情况下我们记录，并关闭所有可能处于未知状态的连接。
     * 它通常是难以 从连接错误中恢复，所以干脆关闭远程连接。当然，也有可能的情况是可以从错误中恢复的，所以可以用一个更复杂的措施来尝试识别和处理 这样的情况。
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();                //5、打印异常堆栈跟踪
        ctx.close();                            //6、关闭通道
    }
}