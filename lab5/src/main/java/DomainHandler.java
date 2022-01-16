import org.apache.log4j.Logger;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.ResolverConfig;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class DomainHandler extends Handler {
    private static final Logger logger = Logger.getLogger(Handler.class);
    private final Selector selector;
    private SocketChannel clientSocket;
    private SocketChannel hostSocket;
    private DatagramChannel dns;
    private InetSocketAddress hostAddress;
    private int port;

    public DomainHandler(Selector selector, SocketChannel clientSocket, int port) throws IOException {
        this.clientSocket = clientSocket;
        this.selector = selector;
        this.port = port;
        String[] dnsServers = ResolverConfig.getCurrentConfig().servers();
        dns = DatagramChannel.open();
        dns.configureBlocking(false);
        dns.connect(new InetSocketAddress(dnsServers[0], port));
        dns.register(selector, SelectionKey.OP_WRITE, this);
        clientSocket.register(selector, 0, this);
    }

    @Override
    public void read(SelectableChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            dns.read(buffer);
            Message msg = new Message(buffer.array());
            Record[] recs = msg.getSectionArray(1);
            for (Record rec : recs) {
                if (rec instanceof ARecord) {
                    ARecord arec = (ARecord) rec;
                    InetAddress adr = arec.getAddress();
                    hostAddress = new InetSocketAddress(adr, port);
                    hostSocket = SocketChannel.open();
                    hostSocket.configureBlocking(false);
                    hostSocket.connect(this.hostAddress);
                    hostSocket.register(selector, SelectionKey.OP_CONNECT, this);
                    closeChannels(selector, dns);
                    return;
                }
            }
        } catch (IOException e) {
            logger.error("unable to read");
            closeChannels(selector, dns, clientSocket, hostSocket);
        }
    }
}
