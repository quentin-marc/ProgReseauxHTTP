package server;
///A Simple Web Server (WebServer.java)

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 *
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 *
 * @author Jeff Heaton
 * @version 1.0
 */
public class WebServer {


    /**
     * WebServer constructor.
     */
    protected void start() {
        ServerSocket s;

        System.out.println("Webserver starting up on port 80");
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(3000);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");
        for (;;) {
            try {
                // wait for a connection
                Socket remote = s.accept();
                // remote is now the connected socket
                System.out.println("Connection, sending data.");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        remote.getInputStream()));
                PrintWriter out = new PrintWriter(remote.getOutputStream());

                File resource = new File("src/server/files/index.html");

                // read the data sent. We basically ignore it,
                // stop reading once a blank line is hit. This
                // blank line signals the end in 0, end 148,of the client HTTP
                // headers.
                String str = ".";
                while (str != null && !str.equals(""))
                    str = in.readLine();

                // Send the response
                // Send the headers
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type: text/html");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");
                // Send the HTML page
                //out.write("<h1>Welcome to the Ultra Mini-WebServer</h1>");


                // Ouverture d'un flux de lecture binaire sur le fichier demand�
                BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(resource));
                // Envoi du corps : le fichier (page HTML, image, vid�o...)
                byte[] buffer = new byte[256];
                int nbRead;
                while((nbRead = fileIn.read(buffer)) != -1) {
                    out.write(String.valueOf(buffer), 0, nbRead);
                }
                // Fermeture du flux de lecture
                fileIn.close();
                out.flush();

                remote.close();
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
    }

    protected void httpGET(){

    }

    /**
     * Start the application.
     *
     * @param args
     *            Command line parameters are not used.
     */
    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start();
    }
}
