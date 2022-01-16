import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ConnectHandler extends Handler {
    private static final Logger logger = Logger.getLogger(Handler.class);
    private final Selector selector;

    public ConnectHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void read(SelectableChannel channel) {
        SocketChannel clientChannel = (SocketChannel) channel;
        try {
            ByteBuffer readbuf = ByteBuffer.allocate(SocksProtocol.BUFF_SIZE_CLIENT);
            clientChannel.read(readbuf);
            readbuf.flip();
            byte version = readbuf.get();
            if (version != SocksProtocol.VERSION){
                logger.error("wrong socks version");
                closeChannels(selector, clientChannel);
                return;
            }
            byte nAuth = readbuf.get();
            if (nAuth < SocksProtocol.MIN_AUTHENTIFICATION) {
                logger.error("unable to connect");
                closeChannels(selector, clientChannel);
                return;
            }
            for (int i = 0; i < Byte.toUnsignedInt(nAuth); i++) {
                if (readbuf.get() != SocksProtocol.NO_AUTHENTIFICATION){
                    logger.error("unable to connect");
                    closeChannels(selector, clientChannel);
                    return;
                }
            }
            clientChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            logger.error("unable to read");
            closeChannels(selector, clientChannel);
        }
    }

    @Override
    public void write(SelectableChannel channel) {
        SocketChannel clientChannel = (SocketChannel) channel;
        try {
            ByteBuffer buf = ByteBuffer.allocate(SocksProtocol.BUFF_SIZE_CLIENT);
            buf.put(SocksProtocol.VERSION);
            buf.put(SocksProtocol.NO_AUTHENTIFICATION);
            buf.flip();
            clientChannel.write(buf);
            clientChannel.register(selector, SelectionKey.OP_READ, new ConnectionRequestHandler(selector));
        } catch (IOException e) {
            logger.error("unable to write");
            closeChannels(selector, clientChannel);
        }
    }
}
