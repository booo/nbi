import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.StringBuffer;


import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;


class Crawler {

    public Crawler() {
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {

        ArrayBlockingQueue<URI> urisToDo = new ArrayBlockingQueue<URI>(1024);
        ArrayList<URI> urisDone = new ArrayList<URI>();

        Directory dir = FSDirectory.open(new File("./index"));

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);

        IndexWriter indexWriter = new IndexWriter(
                dir,
                config
                );

        //normalize uri we start with
        urisToDo.add(new URI(args[0]).normalize());

        URI uri;
        //while there are uris to visit
        while((uri = urisToDo.poll()) != null) {
            //get document from uri
            org.jsoup.nodes.Document doc = Jsoup.connect(uri.toString())
                .userAgent("my crazy crawler").get();

            //extract all links
            Elements links = doc.select("a[href]");
            URI newURI;

            //extract content for indexing
            org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();

            //store uri in document for indexing
            luceneDoc.add(new StringField("uri", uri.toString(), Field.Store.YES));

            //store content of file
            luceneDoc.add(new TextField(
                        "content",
                        new BufferedReader(
                            new StringReader(TextExtractor.extractText(doc)
                                )
                        )));

            indexWriter.addDocument(luceneDoc);

            for(Element link : links) {
                //get absolute links and normalize them according to the rfc
                newURI = new URI(link.attr("abs:href")).normalize();
                if(!urisDone.contains(newURI)) {
                    if(!urisToDo.contains(newURI)) {
                        //add uri that is not already known
                        urisToDo.add(newURI);
                    }
                }
            }
            urisDone.add(uri);
        }

        for(URI u : urisDone) {
            System.out.println(u);
        }

        indexWriter.close();

        System.out.println("Deep web search finished. Coming next: Deep space");
    }

}


class TextExtractor {

    public static String extractText(org.jsoup.nodes.Document doc) {
        Element htmlElement = doc.select("html").first();
        StringBuffer text = new StringBuffer();
        ArrayBlockingQueue<Node> childs = new ArrayBlockingQueue<Node>(1024);
        childs.addAll(htmlElement.childNodes());
        Node child;
        while ((child = childs.poll()) != null) {
            childs.addAll(child.childNodes());
            if(child instanceof TextNode) {
               text.append(((TextNode) child).text());
            }
        }
        return text.toString();

    }
}
