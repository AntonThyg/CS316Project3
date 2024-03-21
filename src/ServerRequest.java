import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

public class ServerRequest {
    char type;

    public ServerRequest(char type) {
        this.type = type;
    }

}
