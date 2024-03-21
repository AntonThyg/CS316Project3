import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

public class ClientRequest {
    char type;


    public ClientRequest(char type) {
        this.type = type;
    }

    class List implements Callable<String> {

        @Override
        public String call() throws Exception {
            try {
                queryBuffer = ByteBuffer.wrap(("l" + "\n").getBytes());
                channel.write(queryBuffer);

                int bytesRead = channel.read(replyBuffer);
                replyBuffer.flip();
                byte[] replyArray = new byte[bytesRead];
                replyBuffer.get(replyArray);
                System.out.println(new String(replyArray));
            } catch (IOException ignored) {
            }
        }
    }

}
