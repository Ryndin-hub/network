import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.*;

public class AcceptHandler extends Handler {
    private final Selector selector;
    private static final Logger logger = Logger.getLogger(Handler.class);

    public AcceptHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void accept(SelectableChannel channel) {
        SocketChannel clientChannel;
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) channel;
            clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ, new ConnectHandler(selector));
            logger.info("connection accepted");
        } catch (IOException e) {
            logger.error("unable to accept");
        }
    }
}
