import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ResendHandler extends Handler {
    private static final Logger logger = Logger.getLogger(Handler.class);
    private final Selector selector;
    private final ChannelWrapper clientChannelWrap;
    private final ChannelWrapper hostChannelWrap;
    private ChannelWrapper curChannelWrap;
    private ChannelWrapper otherChannelWrap;

    public ResendHandler(Selector selector, SocketChannel client, SocketChannel host) {
        this.selector = selector;
        clientChannelWrap = new ChannelWrapper(client);
        hostChannelWrap = new ChannelWrapper(host);
    }

    @Override
    public void read(SelectableChannel channel) {
        curChannelWrap = selectCurrentChannel(channel);
        otherChannelWrap = selectOtherChannel(channel);
        if (curChannelWrap == null || otherChannelWrap == null) {
            return;
        }
        try {
            int byteRead = curChannelWrap.getChannel().read(curChannelWrap.getBuf());
            if (byteRead > 0 && otherChannelWrap.getChannel().isConnected()) {
                addOption(SelectionKey.OP_WRITE, selector, otherChannelWrap.channel);
            }
            if (byteRead < 0) {
                deleteOption(SelectionKey.OP_READ, selector, curChannelWrap.channel);
                curChannelWrap.setFinishRead(true);
                if (curChannelWrap.getBuf().position() == 0) {
                    otherChannelWrap.getChannel().shutdownOutput();
                    otherChannelWrap.setOutputShutdown(true);
                    if (curChannelWrap.isOutputShutdown() || otherChannelWrap.getBuf().position() == 0) {
                        closeChannels(selector, curChannelWrap.getChannel(), otherChannelWrap.getChannel());
                    }
                }
            }

            if (!curChannelWrap.getBuf().hasRemaining()) {
                deleteOption(SelectionKey.OP_READ, selector, curChannelWrap.channel);
            }
        } catch (IOException e) {
            logger.error("unable to read");
            closeChannels(selector, curChannelWrap.getChannel());
        }

    }

    @Override
    public void write(SelectableChannel channel) {
        curChannelWrap = selectCurrentChannel(channel);
        otherChannelWrap = selectOtherChannel(channel);
        if (curChannelWrap == null || otherChannelWrap == null) {
            throw new RuntimeException("WRAPPING ERROR");
        }
        otherChannelWrap.getBuf().flip();
        try {
            int byteWrite = curChannelWrap.getChannel().write(otherChannelWrap.getBuf());
            if (byteWrite > 0) {
                otherChannelWrap.getBuf().compact();
                addOption(SelectionKey.OP_READ, selector, otherChannelWrap.channel);
            }
            if (otherChannelWrap.getBuf().position() == 0) {
                deleteOption(SelectionKey.OP_WRITE, selector, curChannelWrap.channel);
                if (otherChannelWrap.isFinishRead()) {
                    curChannelWrap.getChannel().shutdownOutput();
                    curChannelWrap.setOutputShutdown(true);
                    if (otherChannelWrap.isOutputShutdown()) {
                        closeChannels(selector, curChannelWrap.getChannel(), otherChannelWrap.getChannel());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("unable to write");
            closeChannels(selector, curChannelWrap.getChannel());
        }
    }

    private ChannelWrapper selectCurrentChannel(SelectableChannel channel) {
        if (channel.equals(clientChannelWrap.getChannel())) {
            return clientChannelWrap;
        } else if (channel.equals(hostChannelWrap.getChannel())) {
            return hostChannelWrap;
        } else {
            return null;
        }
    }

    private ChannelWrapper selectOtherChannel(SelectableChannel channel) {
        if (channel.equals(clientChannelWrap.getChannel())) {
            return hostChannelWrap;
        } else if (channel.equals(hostChannelWrap.getChannel())) {
            return clientChannelWrap;
        } else {
            return null;
        }
    }

    public void addOption(int option, Selector selector, SocketChannel channel) {
        SelectionKey currentOption = channel.keyFor(selector);
        if (currentOption.isValid()) {
            currentOption.interestOps(currentOption.interestOps() | option);
        }
    }

    public void deleteOption(int option, Selector selector, SocketChannel channel) {
        SelectionKey currentOption = channel.keyFor(selector);
        if (currentOption.isValid()) {
            currentOption.interestOps(currentOption.interestOps() & ~option);
        }
    }
}
