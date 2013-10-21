import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.*;

class HttpServer implements Runnable {

    private ServerSocket serverSocket;
    private ExecutorService pool;
    private final int port;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting http server...");
        int port = 80;
        if(args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        Thread httpServer = new Thread(new HttpServer(port));
        httpServer.start();
    }

    HttpServer(int port) {
        this.port = port;
    }

    public void run() {

        try {
            serverSocket = new ServerSocket(this.port);
            pool = Executors.newCachedThreadPool();

            while(true) {
                Socket socket = serverSocket.accept();
                pool.execute(new Handler(socket));
            }
        } catch(IOException ex) {
        } finally {
        }
    }
}

class Handler implements Runnable {

    private final Socket client;

    Handler(Socket client) {
        this.client = client;
    }

    public void run() {


        try {
            char[] buffer = new char[1024];
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(client.getInputStream())
                    );
            BufferedOutputStream output = new BufferedOutputStream(this.client.getOutputStream());

            int count = bufferedReader.read(buffer, 0, 1024);
            String requestString = new String(buffer, 0, count);

            String[] lines = requestString.split("\n");

            if(lines.length < 1) {
                output.write("HTTP/1.1 400 Bad Request".getBytes());
                return;
            }

            String[] firstLine = lines[0].split("\\s");

            if(firstLine.length != 3 ||
                    firstLine[0].compareTo("GET") != 0 ||
                    firstLine[2].compareTo("HTTP/1.1") != 0
                    ) {
                        output.write("HTTP/1.1 400 Bad Request".getBytes());
                        return;
                    }

            String DOCUMENT_ROOT = "./testpage/";

            Path path = Paths.get(DOCUMENT_ROOT + firstLine[1]);

            System.out.println("Requested path: " + path);

            if(Files.exists(path) && Files.isDirectory(path)) {
                path = Paths.get(DOCUMENT_ROOT + firstLine[1] + "/index.html");
            }

            if(!Files.exists(path)) {

                output.write("HTTP/1.1 404 Not Found\n".getBytes());
                //output.write(("Content-Length: " + Files.size(path) + "\n").getBytes());
                output.write("\n".getBytes());

                BufferedInputStream bufferedInputStream = new BufferedInputStream(
                        getClass()
                            .getResourceAsStream("/404.html")
                        );
                int n;
                byte[] byteBuffer = new byte[1024];

                while((n = bufferedInputStream.read(byteBuffer)) > -1) {
                    output.write(byteBuffer, 0, n);
                }
                output.flush();
                return;
            }

            output.write("HTTP/1.1 200 OK\n".getBytes());
            output.write(("Content-Length: " + Files.size(path) + "\n").getBytes());
            output.write("\n".getBytes());

            BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path));
            int n;
            byte[] byteBuffer = new byte[1024];

            while((n = bufferedInputStream.read(byteBuffer)) > -1) {
                output.write(byteBuffer, 0, n);
            }
            output.flush();


        } catch(IOException ex) {
        } finally {
            if(!this.client.isClosed()) {
                try {
                    this.client.close();
                } catch(IOException ex) {
                }
            }
        }
    }

}
