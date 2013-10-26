import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

class SearchEngine {

    public static void main(String[] args) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("./index")));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);

        QueryParser parser = new QueryParser(Version.LUCENE_45, "content", analyzer);

        Query query = parser.parse(args[0]);

        TopDocs results = searcher.search(query, null, 5);
        ScoreDoc[] hits = results.scoreDocs;

        System.out.println(results.totalHits + " total matching documents.");

        for(int i = 0; i < results.totalHits; i++) {
            System.out.println(searcher.doc(hits[i].doc).get("uri"));
        }



    }
}
