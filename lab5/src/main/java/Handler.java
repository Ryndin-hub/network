import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class Handler {

    public void accept(SelectableChannel channel) {

    }

    public void read(SelectableChannel channel) {

    }

    public void write(SelectableChannel channel) {

    }

    public void connect(SelectableChannel channel) {
    }

    private void closeChannel(SelectableChannel channel, Selector selector) {
        if (channel == null || selector == null) {
            return;
        }
        try {
            SelectionKey key = channel.keyFor(selector);
            if (key == null) {
                return;
            }
            channel.keyFor(selector).cancel();
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void closeChannels(Selector selector, SelectableChannel... channels) {
        for (var channel : channels) {
            closeChannel(channel, selector);
        }
    }
}
