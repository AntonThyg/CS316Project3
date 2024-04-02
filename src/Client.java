import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    static ExecutorService es = Executors.newFixedThreadPool(4);
    static String directoryPathClient = "src\\ClientFiles\\";
    static SocketChannel channel;
    static ByteBuffer queryBuffer;
    static ByteBuffer replyBuffer = ByteBuffer.allocate(1000);
    static Scanner keyboard = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        //expect two command-line arguments from the user
        if (args.length != 2) {
            System.out.println("Syntax: Client <serverIP> <serverPort>");
            return;
        }
        Boolean quitCondition = false;
        int serverPort = Integer.parseInt(args[1]);
        String command;
        do {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(args[0], serverPort));

            System.out.println("Enter a command (type H for help)\n");
            command = keyboard.nextLine();

            switch (command.toLowerCase()) {
                case "h":
                    help();
                    break;
                case "l":
                    list();
                    break;
                case "x":
                    delete();
                    break;
                case "r":
                    rename();
                    break;
                case "d":
                    download();
                    break;
                case "u":
                    upload();
                    break;
                case "q":
                    es.shutdown();
                    quitCondition = true;
                    break;
                default:
                    if (command.charAt(0) != 'q') {
                        System.out.println("Invalid command\n");
                    }
            }
            channel.close();

        } while (quitCondition!=true);


    }

    static void help() {
        System.out.println(
                "Commands:\n" +
                        "h: display list of commands\n" +
                        "l: display list of files on server\n" +
                        "x: delete file on server\n" +
                        "r: rename file on server\n" +
                        "d: download file from server\n" +
                        "u: upload file to server\n" +
                        "q: quit program"
        );
    }

    static void list() throws Exception {
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

    static void delete() throws IOException {
        System.out.println("Target file name: ");
        String fileName = keyboard.nextLine();
        queryBuffer = ByteBuffer.wrap(("x\n" + fileName + "\n").getBytes());
        channel.write(queryBuffer);

        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] replyArray = new byte[bytesRead];
        replyBuffer.get(replyArray);
        System.out.println(new String(replyArray));
    }

    static void rename() throws IOException {
        System.out.println("file to rename: ");
        String target = keyboard.nextLine();
        System.out.println("New name: ");
        String newName = keyboard.nextLine();
        queryBuffer = ByteBuffer.wrap(("r\n" + target + "\n" + newName + "\n").getBytes());
        channel.write(queryBuffer);

        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] replyArray = new byte[bytesRead];
        replyBuffer.get(replyArray);
        System.out.println(new String(replyArray));
    }

    static void upload() throws IOException {
        System.out.println("Target file name: ");
        String target = keyboard.nextLine();

        es.submit(new UploadTask(target));

    }

    static void download() throws Exception {
        System.out.println("Target file name: ");
        String targetName = keyboard.nextLine();

        es.submit(new DownloadTask(targetName));

    }


    static class DownloadTask implements Callable {
        String target;

        public DownloadTask(String target) {
            this.target = target;
        }

        @Override
        public Object call() throws Exception {
            try {
                queryBuffer = ByteBuffer.wrap(("d" + "\n" + target + "\n").getBytes());
                channel.write(queryBuffer);

                FileOutputStream fs = new FileOutputStream(directoryPathClient + target, true);
                FileChannel fc = fs.getChannel();
                replyBuffer = ByteBuffer.allocate(1000);

                int bytesRead;
                while ((bytesRead = channel.read(replyBuffer)) != -1) {
                    replyBuffer.flip();
                    fc.write(replyBuffer);
                    replyBuffer.clear();
                }
                fs.flush();
                fs.close();
                fc.close();
            } catch (IOException ignored) {
            }
            return null;
        }
    }

    static class UploadTask implements Callable {
        String target;

        public UploadTask(String target) {
            this.target = target;
        }

        @Override
        public Object call() throws Exception {
            queryBuffer = ByteBuffer.wrap(("u\n" + target).getBytes());
            channel.write(queryBuffer);

            FileInputStream fis = new FileInputStream(directoryPathClient + target);
            FileChannel fic = fis.getChannel();
            ByteBuffer content = ByteBuffer.allocate(1000);
            while (fic.read(content) >= 0) {
                content.flip();
                channel.write(content);
                content.clear();
            }
            return null;
        }
    }
}
