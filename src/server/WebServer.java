package server;
///A Simple Web Server (WebServer.java)

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class WebServer {

    // definition des URI
    private static final String SOURCE_DIRECTORY = "src/server/files/";
    private static final String RESSOURCE_DIRECTORY = "ressources/";
    private static final String NOT_FOUND = SOURCE_DIRECTORY+"notFound.html";
    private static final String STATUT_DELETE = SOURCE_DIRECTORY+"statutDelete.html";
    private static final String INDEX = SOURCE_DIRECTORY+"index.html";

    // canaux d'I/O en attribut
    BufferedInputStream socIn;
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
                        requeteGET(INDEX);
                    }
                    else if(nomRessource.startsWith(SOURCE_DIRECTORY) || nomRessource.startsWith(RESSOURCE_DIRECTORY)) {

                        // On redirige vers la méthode associée à la requête de l'utilisateur
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

        String contentHeader = "HTTP/1.0 " + status + "\r\n";
        if(filename.endsWith(".html") || filename.endsWith(".htm"))
            contentHeader += "Content-Type: text/html\r\n";
        else if(filename.endsWith(".css"))
            contentHeader += "Content-Type: text/css\r\n";
        else if(filename.endsWith(".png"))
            contentHeader += "Content-Type: image/png\r\n";
        else if(filename.endsWith(".jpeg") || filename.endsWith(".jpg"))
            contentHeader += "Content-Type: image/jpeg\r\n";
        else if(filename.endsWith(".mp3"))
            contentHeader += "Content-Type: audio/mp3\r\n";
        else if(filename.endsWith(".mp4"))
            contentHeader += "Content-Type: video/mp4\r\n";
        else if(filename.endsWith(".avi"))
            contentHeader += "Content-Type: video/x-msvideo\r\n";
        else if(filename.endsWith(".odt"))
            contentHeader += "Content-Type: application/vnd.oasis.opendocument.text\r\n";
        else if(filename.endsWith(".pdf"))
            contentHeader += "Content-Type: application/pdf\r\n";

        contentHeader += "Content-Length: " + length + "\r\n";
        contentHeader += "Server: Bot\r\n";
        contentHeader += "\r\n";
        return contentHeader;
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
     * Implémentation de la requete HTTP POST.
     * Cette méthode essaie d'ajouter des informations à la fin d'une ressource HTML existante. Plusieurs cas sont possibles:
     * 1. La ressource existe: les informations sont ajoutées à la fin de la ressource et on appelle la méthode GET pour retourner la page
     * 2. La ressource n'existe pas: elle est créée et un code 201 est retourné et on appelle la méthode GET pour retourner la page
     * 3. L'utilisateur tente d'accéder à une ressource qui n'est pas au format HTML: 403 Forbidden
     * La réponse de cette requête possède en corps la ressource modifiee
     * @param filename: l'uri de la ressource demandée
     */
    private void requetePOST(String filename) {
        System.out.println("requete POST " + filename);

        try {
            File ressource = new File(filename);
            boolean ressourceExistante = ressource.exists();

            // Ouverture d'un flux d'écriture binaire vers le fichier, en mode insertion a la fin
            // si le fichier n'existe pas, il est créé
            BufferedOutputStream fluxEcritureFichier = new BufferedOutputStream(new FileOutputStream(ressource, ressourceExistante));

            // Recopie des informations recues dans le fichier
            byte[] buffer = new byte[8192], bufferRefactor = new byte[8192];
            String bufferString = "", bufferStringRefactor = "";
            int nbCarac = 0;
            while(socIn.available() > 0) {

                // lecture des paramètres de la requete
                nbCarac = socIn.read(buffer);

                // on place cette requette dans un paragraphe et on remplace les = par :
                bufferString = new String(buffer);

                bufferStringRefactor = "<p>"+bufferString.substring(0, nbCarac).replace("=", ": ")+"</p>";
                System.out.println(bufferStringRefactor);
                bufferRefactor = bufferStringRefactor.getBytes();

                // on écrit à la fin du fichier
                fluxEcritureFichier.write(bufferRefactor, 0, bufferRefactor.length);
            }
            fluxEcritureFichier.flush();
            fluxEcritureFichier.close();

            // La ressource a été créée / modifiee, on la retourne au client
            requeteGET(filename);

            // Envoi des donn�es
            socOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur on essaie d'avertir le client
            try {
                socOut.write(genererHeader("500 Internal Server Error").getBytes());
                socOut.flush();
            } catch (Exception e2) {};
        }
    }

    /**
     * Implémentation de a requête HTTP HEAD.
     * La méthode HTTP HEAD demande les en-têtes qui seraient retournés si la ressource spécifiée était demandée avec la méthode HTTP GET.
     * La méthode HEAD ne renvoie pas de corps, si c'est le cas, il doit être ignoré.
     * En cas de succès, la réponse contient un entête. Le code HTTP retourné est 200.
     * En cas d'échec (la ressource n'a pas ete trouvée), la réponse contient un entête et un corps (la page d'erreur 404). Le code HTTP retourné est 404.
     * En cas d'erreur interne sur le serveur, la réponse contient seulement un entête spécifiant une erreur 500.
     * @param filename: l'uri de la ressource demandée
     */
    private void requeteHEAD(String filename) {

        System.out.println("requete HEAD " + filename);
        try {
            // Vérification de l'existence de la ressource demandée et envoie de l'entête, renvoie la page not found sinon
            File ressource = new File(filename);
            if(ressource.exists() && ressource.isFile()) {
                socOut.write(genererHeader("200 OK", filename, ressource.length()).getBytes());
            } else {
                ressource = new File(NOT_FOUND);
                socOut.write(genererHeader("404 Not Found", NOT_FOUND, ressource.length()).getBytes());
            }

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
     * Implémentation de la requête HTTP DELETE.
     * La méthode HTTP DELETE supprime la ressource indiquée.
     * En cas de succès, la réponse contient un entête. Le code HTTP retourné est 204.
     * En cas de succès, si on trouve un fichier de statut, la réponse contient un entête et un corps (ce fichier). Le code HTTP retourné est 200.
     * En cas d'échec (la ressource n'a pas ete trouvée), la réponse contient un entête et un corps (la page d'erreur 404). Le code HTTP retourné est 404.
     * En cas d'erreur interne sur le serveur, la réponse contient seulement un entête spécifiant une erreur 500.
     * @param filename: l'uri de la ressource demandée
     */
    private void requeteDELETE(String filename) {

        System.out.println("requete DELETE " + filename);
        try {
            File ressource = new File(filename);
            // Suppression du fichier
            boolean deleted = false;
            if(ressource.exists() && ressource.isFile()) {
                deleted = ressource.delete();
            }

            // Envoi du Header
            if(deleted) {

                // Si fichier de statut trouvé, la méthode DELETE renvoie un code 200 et un fichier de statut plutôt qu'un code 204
                ressource = new File(STATUT_DELETE);
                if(ressource.exists() && ressource.isFile()) {
                    socOut.write(genererHeader("200 OK", STATUT_DELETE, ressource.length()).getBytes());

                    // Ouverture d'un flux de lecture binaire sur la ressource
                    BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(ressource));

                    byte[] buffer = new byte[256];
                    int nbRead;
                    while((nbRead = fileIn.read(buffer)) != -1) {
                        socOut.write(buffer, 0, nbRead);
                    }

                    // Fermeture du flux de lecture
                    fileIn.close();
                } else {
                    //Suppression du fichier effectué
                    socOut.write(genererHeader("204 No Content").getBytes());
                }
            } else if (!ressource.exists()) {
                // Le fichier est introuvable
                socOut.write(genererHeader("404 Not Found").getBytes());
            } else {
                // Impossible de supprimer le fichier
                socOut.write(genererHeader("403 Forbidden").getBytes());
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
