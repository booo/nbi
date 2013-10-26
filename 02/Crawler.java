import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.net.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

class Crawler {

    public Crawler() {
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {

        ArrayBlockingQueue<URI> urisToDo = new ArrayBlockingQueue<URI>(1024);
        ArrayList<URI> urisDone = new ArrayList<URI>();

        urisToDo.add(new URI(args[0]).normalize());

        URI uri;

        while((uri = urisToDo.poll()) != null) {
            Document doc = Jsoup.connect(uri.toString()).userAgent("my crazy crawler").get();
            Elements links = doc.select("a[href]");
            URI newURI;

            for(Element link : links) {
                newURI = new URI(link.attr("abs:href")).normalize();
                if(!urisDone.contains(newURI)) {
                    if(!urisToDo.contains(newURI)) {
                        urisToDo.add(newURI);
                    }
                }
            }
            urisDone.add(uri);
        }

        for(URI u : urisDone) {
            System.out.println(u);
        }

        System.out.println("Deep web search finished. Coming next: Deep space");
    }

}
