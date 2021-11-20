package server;
///A Simple Web Server (WebServer.java)

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {

    protected void start(int port) {

        ServerSocket serverSocket;
        try {
            // create the main server socket
            serverSocket = new ServerSocket(port);

            System.out.println("\nLe serveur a démarré sur le port "+port);
            System.out.println("Vous povez accéder au serveur via l'url: http://localhost:"+port);
            System.out.println("Saisissez CTRL c pour arréter le serveur");

        } catch (Exception e) {
            System.out.println("Erreur: " + e);
            return;
        }

        // on maintient le serveur dans un etat eveillé
        System.out.println("\nEn attente d'une connection au serveur...");
        while(true){
            try {
                // en attente d'une connection
                Socket socketClient = serverSocket.accept();
                System.out.println("\nClient connecté!");
                System.out.println(socketClient.toString());

                BufferedReader socIn = new BufferedReader(new InputStreamReader(
                        socketClient.getInputStream()));
                PrintWriter socOut = new PrintWriter(socketClient.getOutputStream());

                File pathIndex = new File("src/server/files/index.html");

                // read the data sent. We basically ignore it,
                // stop reading once a blank line is hit. This
                // blank line signals the end in 0, end 148,of the client HTTP
                // headers.
                /*String str = ".";
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

                 */

                socketClient.close();
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
    }

    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start(3000); // le serveur démarre sur le port 3000
    }
}
