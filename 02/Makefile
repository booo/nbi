all:
	javac -cp lib/jsoup-1.7.2.jar:lib/lucene-core-4.5.1.jar:lib/lucene-analyzers-common-4.5.1.jar *.java

run: jar
	java -jar crawler.jar http://www.udacity.com/cs101x/index.html

clean:
	rm *.class

jar: all
	jar cvfm crawler.jar Manifest.txt *.class

pdf:
	pandoc README.md -o README.pdf
