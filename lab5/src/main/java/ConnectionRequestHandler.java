import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ConnectionRequestHandler extends Handler {
    private static final Logger logger = Logger.getLogger(Handler.class);

    private final Selector selector;
    private SocketChannel clientSocket;
    private SocketChannel hostSocket;
    private ByteBuffer buffer;

    public ConnectionRequestHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void read(SelectableChannel channel) {
        clientSocket = (SocketChannel) channel;
        try {
            buffer = ByteBuffer.allocate(SocksProtocol.BUFF_SIZE_REQUEST);
            clientSocket.read(buffer);
            buffer.flip();
            byte version = buffer.get();
            if (version != SocksProtocol.VERSION){
                logger.error("wrong socks version");
                closeChannels(selector, clientSocket);
                return;
            }
            byte connectionCode = buffer.get();
            if (connectionCode != SocksProtocol.TCP) {
                logger.error("unable to connect");
                closeChannels(selector, clientSocket);
                return;
            }
            byte socksType = buffer.get();
            if (socksType != SocksProtocol.DOMAIN) {
                logger.error("unable to connect");
                closeChannels(selector, clientSocket);
                return;
            }

            switch (socksType) {
                case SocksProtocol.IPV4:
                    connectIPv4();
                    break;
                case SocksProtocol.DOMAIN:
                    connectDomain();
                    break;
            }
        } catch (IOException e) {
            closeChannels(selector, clientSocket, hostSocket);
        }
    }

    private void connectIPv4() throws IOException {
        byte[] address = new byte[4];
        buffer.get(address);
        short port = buffer.getShort();
        Inet4Address inet4Address = (Inet4Address) Inet4Address.getByAddress(address);

        hostSocket = SocketChannel.open();
        hostSocket.configureBlocking(false);
        hostSocket.connect(new InetSocketAddress(inet4Address, port));

        clientSocket.keyFor(selector).interestOps(0);
        hostSocket.register(selector, SelectionKey.OP_CONNECT, this);
    }

    private void connectDomain() throws IOException {
        byte domainNameLength = buffer.get();
        byte[] address = new byte[Byte.toUnsignedInt(domainNameLength)];
        buffer.get(address);
        short port = buffer.getShort();
        clientSocket.register(selector, 0, new DomainHandler(selector, clientSocket, port));
    }

    @Override
    public void write(SelectableChannel channel) {
        try {
            buffer.flip();
            buffer.put(1, SocksProtocol.REQUEST);
            clientSocket.write(buffer);
            ResendHandler handler = new ResendHandler(selector, clientSocket, hostSocket);
            clientSocket.register(selector, SelectionKey.OP_READ, handler);
            hostSocket.register(selector, SelectionKey.OP_READ, handler);
        } catch (IOException e) {
            logger.error("unable to write");
            closeChannels(selector, clientSocket, hostSocket);
        }
    }

    public void connect(SelectableChannel channel) {
        try {
            hostSocket.finishConnect();
            clientSocket.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
            hostSocket.keyFor(selector).interestOps(0);
        } catch (IOException e) {
            logger.error("unable to connect");
            closeChannels(selector, clientSocket, hostSocket);
        }
    }

}
