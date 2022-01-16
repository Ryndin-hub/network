import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        Proxy proxy = new Proxy(port);
        proxy.start();
    }
}
