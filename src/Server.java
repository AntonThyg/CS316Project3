import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Server {
    static String directoryPath = "src\\ServerFiles\\";

    static ServerSocketChannel welcomeChannel;
    static SocketChannel serveChannel;

    static ByteBuffer replyBuffer;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Please specify the server port.");
            return;
        }
        int port = Integer.parseInt(args[0]);

        welcomeChannel = ServerSocketChannel.open();
        welcomeChannel.socket().bind(new InetSocketAddress(port));

        while (true) {
            serveChannel = welcomeChannel.accept();
            processClient();
        }
    }

    static void processClient() throws IOException {
        ByteBuffer clientQueryBuffer = ByteBuffer.allocate(1000);
        int bytesRead = serveChannel.read(clientQueryBuffer);

        if (bytesRead == -1) {
            // Connection closed by the client
            return;
        }

        clientQueryBuffer.flip();
        byte[] clientQueryArray = new byte[bytesRead];
        clientQueryBuffer.get(clientQueryArray);
        String clientQuery = new String(clientQueryArray);

        String[] parts = clientQuery.split("\n");

        System.out.println(parts[0]);
        switch (parts[0].toUpperCase()) {

            //list all files
            case "L":
                list();

                break;

            //rename a file
            case "R":
                rename(parts);
                break;

            //delete a file
            case "X":
                delete(parts);
                break;

            case "D":
                download(parts);
                break;
            case "U":
                upload(parts);
                break;

            default:
                if (!(parts[0].equals("Q"))) {
                    String message = "invalid operation symbol";
                    replyBuffer = ByteBuffer.wrap(message.getBytes());
                    serveChannel.write(replyBuffer);
                }
        }

        serveChannel.close();
    }
    static void list() throws IOException{
        replyBuffer = ByteBuffer.wrap(listFile().getBytes());
        serveChannel.write(replyBuffer);
    }
    public static String listFile() {
        File path = new File(directoryPath);
        File[] files = path.listFiles();
        String message = "";

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) { //this line weeds out other directories/folders
                message = message + (files[i].getName() + "\n");
            }

        }
        return message;
    }

    static void rename(String[] parts) throws IOException{
        replyBuffer = ByteBuffer.wrap(renameFile(parts[1], parts[2]).getBytes());
        serveChannel.write(replyBuffer);
    }
    //parts[1] is the old file, parts[2] is the new name
    public static String renameFile(String previousFileName, String newFileName) {
        File previousFile = new File(directoryPath + previousFileName);
        File newFile = new File(directoryPath + newFileName);

        boolean flag = previousFile.renameTo(newFile);

        if (flag) return "S";
        return "F";
    }

    static void delete(String[] parts) throws IOException{
        replyBuffer = ByteBuffer.wrap(deleteFile(parts[1]).getBytes());
        serveChannel.write(replyBuffer);
    }
    public static String deleteFile(String fileName) {
        File f = new File(directoryPath + fileName);
        System.out.println(f);
        if (f.exists()) {
            f.delete();
            return "S";
        } else {
            return "F";
        }
    }

    static void download(String[] parts) throws IOException{
        FileInputStream fs = new FileInputStream(directoryPath + parts[1]);

        FileChannel fc = fs.getChannel();
        ByteBuffer content = ByteBuffer.allocate(1000);

        while (fc.read(content) >= 0) {
            content.flip();
            serveChannel.write(content);
            content.clear();
        }
        fs.close();
        fc.close();

    }

    static void upload(String[] parts) throws IOException{
        FileOutputStream fo = new FileOutputStream(directoryPath + parts[1], true);
        FileChannel foc = fo.getChannel();
        ByteBuffer reply = ByteBuffer.allocate(1000);
        while (serveChannel.read(reply) >= 0) {
            reply.flip();
            foc.write(reply);
            reply.clear();
        }
        fo.flush();
        fo.close();
    }


}