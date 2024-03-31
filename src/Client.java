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
    static int serverPort;
    static String serverAddress;
    static ByteBuffer queryBuffer;
    static ByteBuffer replyBuffer = ByteBuffer.allocate(1000);
    static Scanner keyboard = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        //expect two command-line arguments from the user
        if (args.length != 2) {
            System.out.println("Syntax: Client <serverIP> <serverPort>");
            return;
        }
        serverAddress = args[0];
        serverPort = Integer.parseInt(args[1]);
        String[] command;
        do {


            System.out.println("Enter a command (type H for help)\n");
            command = keyboard.nextLine().split(" ");

            switch (command[0]) {
                case "h":
                    help();
                    break;
                case "l":
                    es.submit(new List());
                    break;
                case "x":
                    es.submit(new Delete());
                    break;
                case "r":
                    es.submit(new Rename());
                    break;
                case "d":
                    es.submit(new Download(command[1]));
                    break;

                case "u":
                    es.submit(new Upload(command[1]));
                    break;

                default:
                    if (command.charAt(0) != 'q') {
                        System.out.println("Invalid command\n");
                    }
            }

        } while (command.charAt(0) != 'q');


    }

    static void help() {
        System.out.println(
                "Commands:\n" +
                        "h: display list of commands\n" +
                        "l: display list of files on server\n" +
                        "x <file>: delete file on server\n" +
                        "r <file> <name>: rename file on server\n" +
                        "d <file>: download file from server\n" +
                        "u <file>: upload file to server\n" +
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
        try {
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
        } catch (IOException e) {
        }

    }

    static void download() throws Exception {
        System.out.println("Enter the name of the file in the server directory to be transferred");
        String targetName = keyboard.nextLine();

        try {
            queryBuffer = ByteBuffer.wrap(("d" + "\n" + targetName + "\n").getBytes());
            channel.write(queryBuffer);

            FileOutputStream fs = new FileOutputStream(directoryPathClient + targetName, true);
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

    }

    static void printResponse(String response) {
        switch (response.toUpperCase()) {
            case "S":
                System.out.println("Command successfully executed");
            case "F":
                System.out.println("Error: Command failed to execute");
        }
    }

    static class List implements Callable<String> {
        @Override
        public String call() throws Exception {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(serverAddress, serverPort));
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
            channel.close();
            return null;
        }
    }

    static class Delete implements Callable<String> {

        @Override
        public String call() throws Exception {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(serverAddress, serverPort));
            System.out.println("Target file name: ");
            String fileName = keyboard.nextLine();
            queryBuffer = ByteBuffer.wrap(("x\n" + fileName + "\n").getBytes());
            channel.write(queryBuffer);

            int bytesRead = channel.read(replyBuffer);
            replyBuffer.flip();
            byte[] replyArray = new byte[bytesRead];
            replyBuffer.get(replyArray);
            System.out.println(new String(replyArray));
            channel.close();
            return null;
        }
    }

    static class Rename implements Callable<String> {

        @Override
        public String call() throws Exception {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(serverAddress, serverPort));
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
            channel.close();
            return null;
        }
    }

    static class Upload implements Callable {
        String file;

        public Upload(String file) {
            this.file = file;
        }

        @Override
        public Object call() throws Exception {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(serverAddress, serverPort));
            String target = this.file;
            try {
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
            } catch (IOException e) {
            }

            channel.close();
            return null;
        }
    }

    static class Download implements Callable {
        String file;

        public Download(String file) {
            this.file = file;
        }

        @Override
        public Object call() throws Exception {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(serverAddress, serverPort));

            String target = this.file;
            try {
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
            } catch (IOException e) {
            }
            channel.close();
            return null;
        }
    }
}
