package server;
///A Simple Web Server (WebServer.java)

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {

    // definition des URI
    private static final String SOURCE_DIRECTORY = "src/server/files/";
    private static final String NOT_FOUND = SOURCE_DIRECTORY+"notfound.html";
    private static final String INDEX = SOURCE_DIRECTORY+"index.html";

    // canaux d'I/O en attribut
    BufferedReader socIn;
    BufferedOutputStream socOut;

    /**
     * Démarrage du serveur web
     * @param port: le port du serveur
     */
    private void start(int port) {

        // démarrage du serveur
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

                // création des canaux de communication avec le client
                socIn = new BufferedReader(new InputStreamReader(
                        socketClient.getInputStream()));
                socOut = new BufferedOutputStream(socketClient.getOutputStream());

                //TODO WHAT is it?
                // lecture des données envoyées
                String str = ".";
                while (str != null && !str.equals("")){
                    str = socIn.readLine();
                }

                // par défaut on retourne la page d'index au client
                getRequest(INDEX);

                socketClient.close();
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
    }

    /**
     * Cette méthode permet de générer l'entete HTML d'une reponse qui n'a pas de corps
     * @param status: le status HTTP de la requete
     * @return: l'entete HTML de la réponse sous forme de chaine de caractères
     */
    private String genererHeader(String status) {
        String header = "HTTP/1.0 " + status + "\r\n";
        header += "Server: Bot\r\n";
        header += "\r\n";
        return header;
    }

    /**
     * Cette méthode permet de générer l'entete HTML d'une reponse qui retourne un fichier en corps
     * @param status: le status HTTP de la requete
     * @param filename: le fichier qui occupera le corps de la réponse
     * @param length: la taille de la ressource en octets
     * @return: l'entete HTML de la réponse sous forme de chaine de caractères
     */
    protected String genererHeader(String status, String filename, long length) {

        // TODO citation pack parrain? C'est cramé là
        String header = "HTTP/1.0 " + status + "\r\n";
        if(filename.endsWith(".html") || filename.endsWith(".htm"))
            header += "Content-Type: text/html\r\n";
        else if(filename.endsWith(".mp4"))
            header += "Content-Type: video/mp4\r\n";
        else if(filename.endsWith(".png"))
            header += "Content-Type: image/png\r\n";
        else if(filename.endsWith(".jpeg") || filename.endsWith(".jpeg"))
            header += "Content-Type: image/jpg\r\n";
        else if(filename.endsWith(".mp3"))
            header += "Content-Type: audio/mp3\r\n";
        else if(filename.endsWith(".avi"))
            header += "Content-Type: video/x-msvideo\r\n";
        else if(filename.endsWith(".css"))
            header += "Content-Type: text/css\r\n";
        else if(filename.endsWith(".pdf"))
            header += "Content-Type: application/pdf\r\n";
        else if(filename.endsWith(".odt"))
            header += "Content-Type: application/vnd.oasis.opendocument.text\r\n";
        header += "Content-Length: " + length + "\r\n";
        header += "Server: Bot\r\n";
        header += "\r\n";
        return header;
    }

    /**
     * Implémentation de a requete HTTP GET.
     * La méthode tente de retourner la ressource demandée par le client.
     * En cas de succès, la reponse contient une entete et un corps (la ressource demandée). le code HTTP retourné est 200.
     * En cas d'echec (la ressource n'a pas ete trouvée), la reponse contient une entete et un corps (la page d'erreur 404). le code HTTP retourné est 404.
     * En cas d'erreur interne sur le serveur, la reponse contient seulement une entete spécifiant une erreur 500.
     * Aucun autre code HTTP n'est pris en charge.
     * @param filename: l'uri de la ressource demandée
     */
    private void getRequest(String filename) {
        System.out.println("GET " + filename);
        try {
            // Vérification de l'existence de la ressource demandée, renvoie la page not found sinon
            File ressource = new File(filename);
            if(ressource.exists() && ressource.isFile()) {
                socOut.write(genererHeader("200 OK", filename, ressource.length()).getBytes());
            } else {
                /*resource = new File(FILE_NOT_FOUND);
                out.write(makeHeader("404 Not Found", FILE_NOT_FOUND, resource.length()).getBytes());*/
            }

            // Ouverture d'un flux de lecture binaire sur la ressource
            BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(ressource));

            byte[] buffer = new byte[256];
            int nbRead;
            while((nbRead = fileIn.read(buffer)) != -1) {
                socOut.write(buffer, 0, nbRead);
            }

            // Fermeture du flux de lecture
            fileIn.close();

            //Envoi des données
            socOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // En cas d'erreur on essaie d'avertir le client
            try {
                socOut.write(genererHeader("500 Internal Server Error").getBytes());
                socOut.flush();
            } catch (Exception e2) {};
        }
    }

    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start(3000); // le serveur démarre sur le port 3000
    }
}
