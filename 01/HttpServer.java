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

        //handle optional argument port
        int port = 80;
        if(args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        //start main thread that handles incomming connections
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
                //accept incomming connections and pass the socket to the new
                //thread from the thread pool
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

            //read request from client
            int count = bufferedReader.read(buffer, 0, 1024);
            String requestString = new String(buffer, 0, count);

            //get line base input
            String[] lines = requestString.split("\n");

            //return bad request if there is no line
            if(lines.length < 1) {
                output.write("HTTP/1.1 400 Bad Request".getBytes());
                return;
            }

            //split first line to get path and method
            String[] firstLine = lines[0].split("\\s");

            //only handle get requests and HTTP/1.1 requests
            if(firstLine.length != 3 ||
                    firstLine[0].compareTo("GET") != 0 ||
                    firstLine[2].compareTo("HTTP/1.1") != 0
                    ) {
                        output.write("HTTP/1.1 400 Bad Request".getBytes());
                        return;
                    }

            String pathString = firstLine[1];
            Boolean showHeader = false;
            //check if we should show the request from the client on the page
            if(firstLine[1].contains("?")) {
                String[] parts = firstLine[1].split("\\?");
                pathString = parts[0];
                if(parts.length == 2) {
                    showHeader = parts[1].contains("header=show");
                }
            }

            //place your documents here
            String DOCUMENT_ROOT = "./testpage/";

            Path path = Paths.get(DOCUMENT_ROOT + pathString);

            //if directory is requested change path to default document in this
            //path

            System.out.println(pathString);

            if(Files.exists(path) && Files.isDirectory(path)) {
                path = Paths.get(DOCUMENT_ROOT + pathString + "/index.html");
            }

            if(!Files.exists(path)) {

                //if file does not exist return 404 Not Found
                output.write("HTTP/1.1 404 Not Found\n".getBytes());
                output.write("\n".getBytes());
                //load animated 404.html page from jar file
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

            int contentLength = (int) Files.size(path);

            //return requested file with Content-Length in the header
            output.write("HTTP/1.1 200 OK\n".getBytes());

            if(showHeader) {
                contentLength += requestString.getBytes().length;
            }
            output.write(("Content-Length: " + contentLength + "\n").getBytes());
            output.write("\n".getBytes());

            //return request header to client
            if(showHeader) {
                output.write(requestString.getBytes());
            }

            BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path));
            int n;
            byte[] byteBuffer = new byte[1024];

            while((n = bufferedInputStream.read(byteBuffer)) > -1) {
                output.write(byteBuffer, 0, n);
            }
            output.flush();


        } catch(IOException ex) {
            //do not handle execptions
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
