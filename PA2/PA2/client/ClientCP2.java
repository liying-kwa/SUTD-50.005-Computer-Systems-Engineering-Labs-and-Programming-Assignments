import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class ClientCP2 {

    // Obtain server's public key from .der file
    public static PublicKey getServerPublicKeyFromFile(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    // Get public key from serverCert and verify that ServerCert is signed with the CA's private key
    public static PublicKey getServerPublicKeyFromCert(X509Certificate serverCert) throws Exception {
        // Extract server's public key
        PublicKey key = serverCert.getPublicKey();
        // Extract CA's public key
        InputStream fis = new FileInputStream("cacse.crt");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert =(X509Certificate)cf.generateCertificate(fis);
        PublicKey caKey = caCert.getPublicKey();
        // Check validity and verify signed certificate.
        serverCert.checkValidity();
        serverCert.verify(caKey);
        return key;
    }

    public static void main(String[] args) {

        String serverAddress = "localhost";
        //if (args.length > 0) serverAddress = args[0];
        int port = 4321;
        //if (args.length > 1) port = Integer.parseInt(args[1]);
        String[] filenames = {"100.txt", "200.txt", "500.txt"};
        //String[] filenames = {"100000.txt"};
        if (args.length > 0) {
            filenames = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                filenames[i] = args[i];
            }
        }

        Socket clientSocket = null;
        DataOutputStream toServer = null;
        DataInputStream fromServer = null;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedFileInputStream = null;

        long timeStarted = System.nanoTime();

        try {

            System.out.println("Establishing connection to server...");

            // Connect to server and get the input and output streams
            clientSocket = new Socket(serverAddress, port);
            toServer = new DataOutputStream(clientSocket.getOutputStream());
            fromServer = new DataInputStream(clientSocket.getInputStream());
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Say hello to server
            out.println("Hello SecStore, please prove your identity!");
            System.out.println("Saying hello to server");

            // Generate (one-time) nonce and send to server
            System.out.println("Generating and sending nonce to server");
            byte[] nonce = new byte[32]; // length is bounded by 32
            new Random().nextBytes(nonce);
            toServer.writeInt(nonce.length);
            toServer.write(nonce);
            toServer.flush();

            // Retrieve encrypted nonce from server
            int numBytesEncryptedNonce = fromServer.readInt();
            byte[] encryptedNonce = new byte[numBytesEncryptedNonce];
            fromServer.readFully(encryptedNonce, 0, numBytesEncryptedNonce);
            System.out.println("Retrieved encrypted nonce from server");

            // Request for signed certificate from server
            System.out.println("Requesting for signed certificate from server...");
            out.println("Give me your certificate signed by CA");

            // Retrieve signed certificate from server
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate serverCert = (X509Certificate)cf.generateCertificate(fromServer);
            System.out.println("Retrieved signed certificate from server");

            // Verify serverCert and get server's public key
            PublicKey serverPublicKey = getServerPublicKeyFromCert(serverCert);
            System.out.println("Signed certificate validated");

            // Decrypt encrypted nonce and check that it matches the nonce sent
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, serverPublicKey);
            byte[] decryptedNonce = rsaCipher.doFinal(encryptedNonce);
            if (Arrays.equals(nonce,decryptedNonce)) {
                System.out.println("Server is verified! :)");
                out.println("Handshake");
            } else {
                System.out.println("Verification failed :( Closing connections...");
                out.println("No handshake");
                toServer.close();
                fromServer.close();
                out.close();
                in.close();
                clientSocket.close();
            }

            // Tell server to receive session key
            System.out.println("Creating session key and sending to server...");
            out.println("Session Key");

            // Generate AES key and encrypt using server's public key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            SecretKey aesKey = keyGen.generateKey();

            // Encrypt AES key using server's public key
            Cipher rsaCipher2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher2.init(Cipher.ENCRYPT_MODE, serverPublicKey);
            byte[] aesKeyEncrypted = rsaCipher2.doFinal(aesKey.getEncoded());

            // Send session key to server
            toServer.writeInt(aesKeyEncrypted.length);
            toServer.write(aesKeyEncrypted);
            toServer.flush();
            System.out.println("Sent session key to server!");

            System.out.println("Sending files...");

            for (int i = 0; i < filenames.length; i++) {

                // Send the filename
                toServer.writeInt(0);
                toServer.writeInt(filenames[i].getBytes().length);
                toServer.write(filenames[i].getBytes());
                toServer.flush();

                // Open the file
                fileInputStream = new FileInputStream(filenames[i]);
                bufferedFileInputStream = new BufferedInputStream(fileInputStream);

                // Send the file
                int numBytes = 0;
                int numBytesEncrypted = 0;
                byte[] fromFileBuffer = new byte[117];
                for (boolean fileEnded = false; !fileEnded; ) {
                    numBytes = bufferedFileInputStream.read(fromFileBuffer);
                    fileEnded = numBytes < 117;

                    // Encrypt file chunk and check size of encrypted chunk
                    Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
                    //System.out.println(new String(fromFileBuffer));
                    byte[] fromFileBufferEncrypted = aesCipher.doFinal(fromFileBuffer);
                    //System.out.println(Base64.getEncoder().encodeToString(fromFileBufferEncrypted));
                    numBytesEncrypted = fromFileBufferEncrypted.length;

                    toServer.writeInt(1);
                    toServer.writeInt(numBytes);
                    toServer.writeInt(numBytesEncrypted);
                    toServer.write(fromFileBufferEncrypted);
                    toServer.flush();
                }

                bufferedFileInputStream.close();
                fileInputStream.close();

            }

            // Tell server that file sending has ended
            toServer.writeInt(2);

            // Close all connections
            System.out.println("Closing connection...");
            toServer.close();
            fromServer.close();
            out.close();
            in.close();
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");

    }

}