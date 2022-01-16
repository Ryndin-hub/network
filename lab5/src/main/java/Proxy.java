import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

public class Proxy {
    private final int port;
    Selector selector;
    private static final Logger logger = Logger.getLogger(Proxy.class);

    public Proxy(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT, new AcceptHandler(selector));
        logger.info("proxy started");
        run();
    }

    private void run() {
        while (true) {
            try {
                selector.select();
            } catch (IOException e) {
                return;
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isValid() && key.isAcceptable()) {
                    ((Handler)key.attachment()).accept(key.channel());
                }
                if (key.isValid() && key.isConnectable()) {
                    ((Handler)key.attachment()).connect(key.channel());
                }
                if (key.isValid() && key.isReadable()) {
                    ((Handler)key.attachment()).read(key.channel());
                }
                if (key.isValid() && key.isWritable()) {
                    ((Handler)key.attachment()).write(key.channel());
                }
                iterator.remove();
            }
        }
    }
}
