package server;
///A Simple Web Server (WebServer.java)

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {

    // definition des URI
    private static final String SOURCE_DIRECTORY = "src/server/files/";
    private static final String NOT_FOUND = SOURCE_DIRECTORY+"notFound.html";
    private static final String INDEX = SOURCE_DIRECTORY+"index.html";

    // canaux d'I/O en attribut
    BufferedInputStream socIn;
    //BufferedReader socIn;
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
                System.out.println("\nRequete entrante. Informations du client:");
                System.out.println("[IP: "+socketClient.getInetAddress()+", port: "+socketClient.getPort()+", localPort: "+socketClient.getLocalPort()+"]\n");

                // création des canaux de communication avec le client
                socIn = new BufferedInputStream(socketClient.getInputStream());
                socOut = new BufferedOutputStream(socketClient.getOutputStream());

                // Si la requete est mal formée (la sequence doit se terminer par \r\n\r\n), affichage d'une erreur 400
                // lecture de la requete caractère par caractère et enregistrement des 4 derniers
                // 8 * 255 limite taille requete HTTP par navigateur
                int c1 = '\0', c2 = '\0', c3 = '\0', c4 = '\0', cLu = '\0';
                String header = "";
                for (int i = 0; i < 8 * 255; i++){
                    cLu = socIn.read();
                    if(c1 == '\0'){
                        c1 = cLu;
                    }
                    else if(c2 == '\0'){
                        c2 = cLu;
                    }
                    else if(c3 == '\0'){
                        c3 = cLu;
                    }
                    else if(c4 == '\0'){
                        c4 = cLu;
                    }
                    else{
                        c1 = c2;
                        c2 = c3;
                        c3 = c4;
                        c4 = cLu;
                    }

                    header += (char) cLu;

                    //requete bien formee, on sort de la boucle
                    if((c1 == '\r' && c2 == '\n' && c3 == '\r' && c4 == '\n'))
                        break;
                }

                // la requete est bien formee
                if(c1 == '\r' && c2 == '\n' && c3 == '\r' && c4 == '\n' && !header.isEmpty()){

                    String[] listeMotsHeader = header.split(" ");
                    String typeRequete = listeMotsHeader[0];
                    String nomRessource = listeMotsHeader[1].substring(1, listeMotsHeader[1].length());

                    if(nomRessource.isEmpty()) {
                        // TODO get obligatoirement?
                        requeteGET(INDEX);
                    }
                    else if(nomRessource.startsWith(SOURCE_DIRECTORY)) {

                        // On redirigie vers la méthode associée à la requete de l'utilisateur
                        switch (typeRequete){
                            case "GET":
                                requeteGET(nomRessource);
                                break;
                            case "POST":
                                requetePOST(nomRessource);
                                break;
                            case "HEAD":
                                requeteHEAD(nomRessource);
                                break;
                            case "PUT":
                                requetePUT(nomRessource);
                                break;
                            case "DELETE":
                                requeteDELETE(nomRessource);
                                break;
                            default:
                                // Si la requete ne correspond à aucune des requetes implémentées
                                socOut.write(genererHeader("501 Not Implemented").getBytes());
                                socOut.flush();
                        }
                    } else {
                        // Interdiction d'accéder à des ressources situées en dehors du dossier prévu à cet effet
                        socOut.write(genererHeader("403 Forbidden").getBytes());
                        socOut.flush();
                    }
                }
                else{
                    socOut.write(genererHeader("400 Bad Request").getBytes());
                    socOut.flush();
                }

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
        else if(filename.endsWith(".css"))
            header += "Content-Type: text/css\r\n";
        else if(filename.endsWith(".png"))
            header += "Content-Type: image/png\r\n";
        else if(filename.endsWith(".jpeg") || filename.endsWith(".jpg"))
            header += "Content-Type: image/jpg\r\n";
        else if(filename.endsWith(".mp3"))
            header += "Content-Type: audio/mp3\r\n";
        else if(filename.endsWith(".mp4"))
            header += "Content-Type: video/mp4\r\n";
        else if(filename.endsWith(".avi"))
            header += "Content-Type: video/x-msvideo\r\n";
        else if(filename.endsWith(".odt"))
            header += "Content-Type: application/vnd.oasis.opendocument.text\r\n";
        else if(filename.endsWith(".pdf"))
            header += "Content-Type: application/pdf\r\n";

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
    // TODO Implémentation erreur 404
    private void requeteGET(String filename) {

        try {
            // Vérification de l'existence de la ressource demandée, renvoie la page not found sinon
            File ressource = new File(filename);
            if(ressource.exists() && ressource.isFile()) {
                socOut.write(genererHeader("200 OK", filename, ressource.length()).getBytes());
            } else {
                ressource = new File(NOT_FOUND);
                socOut.write(genererHeader("404 Not Found", NOT_FOUND, ressource.length()).getBytes());
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

    /**
     * Implémentation de a requete HTTP POST.
     * // TODO JAVADOC (important de bien détailler l'action de la méthode)
     * @param filename: l'uri de la ressource demandée
     */
    private void requetePOST(String filename) {
        System.out.println("requete POST " + filename);
    }

    /**
     * Implémentation de a requete HTTP HEAD.
     * // TODO JAVADOC (important de bien détailler l'action de la méthode)
     * @param filename: l'uri de la ressource demandée
     */
    private void requeteHEAD(String filename) {
        System.out.println("requete HEAD " + filename);
    }

    /**
     * Implémentation de a requete HTTP PUT.
     * La méthode tente de créer une nouvelle ressource, dont le contenu est constitué des données du corps de la requète reçue.
     * Si une ressource du même nom existait déjà sur le serveur, elle est écrasée et l'en-tête de la réponse envoyée a un code de 204 No Content.
     * Si la ressource n'existait pas, elle est créée et l'en-tête de la réponse envoyée a un code de 201 Created.
     * La réponse ne contient pas de corps.
     * @param filename: l'uri de la ressource demandée
     */
    private void requetePUT(String filename) {
        try {
            File resource = new File(filename);
            boolean existed = resource.exists();

            // Efface le contenu fichier avant de le remplacer par les informations reçues
            PrintWriter pw = new PrintWriter(resource);
            pw.close();

            // Ouverture d'un flux d'écriture binaire vers le fichier
            BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(resource));

            // Lecture du corps de la requete
            byte[] buffer = new byte[256];
            while(socIn.available() > 0) {
                int nbRead = socIn.read(buffer);
                fileOut.write(buffer, 0, nbRead);
            }
            fileOut.flush();

            //Fermeture du flux d'écriture vers le fichier
            fileOut.close();

            // Envoi du Header (pas besoin de corps)
            if(existed) {
                // Ressource modifiée avec succès, aucune information supplémentaire à fournir
                socOut.write(genererHeader("204 No Content").getBytes());
            } else {
                // Ressource créée avec succès
                socOut.write(genererHeader("201 Created").getBytes());
            }
            // Envoi des données
            socOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur on essaie d'avertir le client
            try {
                socOut.write(genererHeader("500 Internal Server Error").getBytes());
                socOut.flush();
            } catch (Exception e2) {};
        }
        System.out.println("requete PUT " + filename);
    }

    /**
     * Implémentation de a requete HTTP DELETE.
     * // TODO JAVADOC (important de bien détailler l'action de la méthode)
     * @param filename: l'uri de la ressource demandée
     */
    private void requeteDELETE(String filename) {
        System.out.println("requete DELETE " + filename);
    }

    /**
     * Lance le serveur
     * @param args: aucun argument nécessaire en entrée
     */
    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start(3000); // le serveur démarre sur le port 3000
    }
}
