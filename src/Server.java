import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.io.DataInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

public class Server {
    static String directoryPath = "src\\ServerFiles\\";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("please specify server port");
            return;
        }
        int port = Integer.parseInt(args[0]);
        ServerSocketChannel welcomeChannel = ServerSocketChannel.open();
        welcomeChannel.socket().bind(new InetSocketAddress(port));
        SocketChannel serveChannel = welcomeChannel.accept();

        String parts[];
        do {


            ByteBuffer clientQueryBuffer =
                    ByteBuffer.allocate(1000);
            //read from the network and write to the buffer
            int bytesRead = serveChannel.read(clientQueryBuffer);
            //flip is required between write and read the buffer


            clientQueryBuffer.flip();
            //read the data from the buffer
            byte[] clientQueryArray = new byte[bytesRead];
            clientQueryBuffer.get(clientQueryArray);
            String clientQuery = new String(clientQueryArray);

            parts = clientQuery.split("\n");
            ByteBuffer replyBuffer;


            System.out.println(parts[0]);
            switch (parts[0].toUpperCase()) {

                //list all files
                case "L":
                    replyBuffer = ByteBuffer.wrap(listFile().getBytes());
                    serveChannel.write(replyBuffer);

                    break;

                //rename a file
                case "R":
                    replyBuffer = ByteBuffer.wrap(renameFile(parts[1], parts[2]).getBytes());
                    serveChannel.write(replyBuffer);
                    break;

                //delete a file
                case "X":
                    replyBuffer = ByteBuffer.wrap(deleteFile(parts[1]).getBytes());
                    serveChannel.write(replyBuffer);
                    break;
                case "D":
                    File f = new File(directoryPath + parts[1]);
                    f.getParentFile().mkdirs();
                    if (!f.exists()) {
                        f.createNewFile();
                    }

                    FileInputStream fs = new FileInputStream(directoryPath + parts[1]);

                    FileChannel fc = fs.getChannel();
                    ByteBuffer content = ByteBuffer.allocate(1000);
                    while (fc.read(content) >= 0) {
                        content.flip();
                        serveChannel.write(content);
                        content.clear();
                    }
                    serveChannel.close();


                    break;
                case "U":
                    String message = "";
                    if (Files.exists(Path.of(directoryPath + parts[1]))) {
                        FileOutputStream fo = new FileOutputStream(directoryPath + parts[1], true);
                        FileChannel foc = fo.getChannel();
                        ByteBuffer reply = ByteBuffer.allocate(1000);
                        while (serveChannel.read(reply) >= 0) {
                            reply.flip();
                            foc.write(reply);
                            reply.clear();
                        }
                        message = "S";

                    } else
                        message = "F";
                    replyBuffer = ByteBuffer.wrap(message.getBytes());
                    serveChannel.write(replyBuffer);
                    serveChannel.close();
                    break;

                default:
                    if (!(parts[0].equals("Q"))) {
                        message = "invalid operation symbol";
                        replyBuffer = ByteBuffer.wrap(message.getBytes());
                        serveChannel.write(replyBuffer);
                    }
            }


        } while (!Objects.equals(parts[0], "q"));
        serveChannel.close();
    }


    static String deleteFile(String fileToDelete) {
        File f = new File(directoryPath + fileToDelete);
        System.out.println(f);
        if (f.exists()) {
            f.delete();
            return "S";
        } else {
            return "F";
        }
    }

    //parts[1] is the old file, parts[2] is the new name
    static String renameFile(String previousFileName, String newFileName) {
        File previousFile = new File(directoryPath + previousFileName);
        File newFile = new File(directoryPath + newFileName);

        boolean flag = previousFile.renameTo(newFile);

        if (flag == true) {
            return "S";
        } else {
            return "F";
        }
    }

    static String listFile() {
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

}