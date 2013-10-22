all:
	javac HttpServer.java

run: jar
	java -jar webserver.jar 8080

clean:
	rm *.class

jar: all
	jar cfm webserver.jar Manifest.txt *.class 404.html

pdf:
	pandoc README.md -o README.pdf
