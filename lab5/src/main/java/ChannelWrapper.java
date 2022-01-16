import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Getter
public class ChannelWrapper {
    private final ByteBuffer buf = ByteBuffer.allocate(1024);
    @Setter
    private boolean outputShutdown = false;
    @Setter
    private boolean finishRead = false;
    public final SocketChannel channel;

    public ChannelWrapper(SocketChannel channel) {
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public ByteBuffer getBuf() {
        return buf;
    }

    public void setFinishRead(boolean finishRead) {
        this.finishRead = finishRead;
    }

    public void setOutputShutdown(boolean outputShutdown) {
        this.outputShutdown = outputShutdown;
    }

    public boolean isOutputShutdown() {
        return outputShutdown;
    }

    public boolean isFinishRead() {
        return finishRead;
    }
}
