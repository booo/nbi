# Assignment 01

## 1)

Please take a look at the provided documentation that is inlined in the
source code. I used a ExecuterService to introduce multi-threading. The
master thread accepts connections and the workers in the ExecuterService
handle all communication with the clients.

    java -jar webserver.jar

Should start the webserver on its default port 80. Use

    java -jar webserver.jar 8080

to start the webserver on a different port.

The default root directory of the webserver is testpage in the same
directory the webserver is running in.

## 2)

The web server returns 404 Not Found if a client tries to access any page that does
not exist. The custom animated 404 page is a bouncing ball. I use a canvas and
javascript to create the animation

## 3)

The webserver should handle IRIs.

## 4)

In the testpage directory I have provided a default index.html page that
contains 5 pages with new html5 features.

I use

    <!DOCTYPE html>

to specifiy the document type which in this case is html

I use the

    <video></video>

tag to embed a video in the html page.

I use

    <a href="tel:+1234">+1234</a>

to create a link which represents a telephone number that is callable by an
external application if the user clicks the link.

I use

    <canvas></canvas>

to create a space where you can create drawings, e.g. with javascript.

I use

    <mark>some text to highlight</mark>

to highlight some parts of text in my html file.
