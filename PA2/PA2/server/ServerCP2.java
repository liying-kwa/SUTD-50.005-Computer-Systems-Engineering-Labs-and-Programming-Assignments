import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class ServerCP2 {

    // Obtain server's private key from .der file
    public static PrivateKey getPrivateKey(String filename) throws Exception {
        byte [] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory .getInstance( "RSA" );
        return kf.generatePrivate(spec);
    }

    public static void main(String[] args) {

        int port = 4321;
        if (args.length > 0) port = Integer.parseInt(args[0]);

        ServerSocket welcomeSocket = null;
        Socket connectionSocket = null;
        DataOutputStream toClient = null;
        DataInputStream fromClient = null;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedFileOutputStream = null;

        try {
            welcomeSocket = new ServerSocket(port);
            connectionSocket = welcomeSocket.accept();
            fromClient = new DataInputStream(connectionSocket.getInputStream());
            toClient = new DataOutputStream(connectionSocket.getOutputStream());
            PrintWriter out = new PrintWriter(connectionSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            //System.out.println("Server IP: " + welcomeSocket.getInetAddress().getLocalHost().getHostAddress());

            // Wait for client to say hello
            while (true) {
                String request = in.readLine();
                if (request.equals("Hello SecStore, please prove your identity!")){
                    System.out.println("Client said hello!");
                    break;
                } else {
                    System.out.println("Waiting for client...");
                }
            }

            // Get nonce from client
            System.out.println("Getting nonce from client");
            int numBytesNonce = fromClient.readInt();
            byte[] nonce = new byte[numBytesNonce];
            fromClient.readFully(nonce, 0, numBytesNonce);
            System.out.println("Nonce received");

            // Encrypt nonce and send back to client
            System.out.println("Encrypting and sending nonce back to client");
            PrivateKey privateKey = getPrivateKey("private_key.der");
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptedNonce = rsaCipher.doFinal(nonce);
            toClient.writeInt(encryptedNonce.length);
            toClient.write(encryptedNonce);
            toClient.flush();

            // Wait for client to request for signed certificate
            while (true) {
                String request = in.readLine();
                if (request.equals("Give me your certificate signed by CA")){
                    System.out.println("Client is requesting for signed certificate...");
                    break;
                } else {
                    System.out.println("Waiting for client to ask for sign certificate...");
                }
            }

            // Send signed certificate to client
            System.out.println("Sending certificate to client...");
            InputStream fis = new FileInputStream("server.crt");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate serverCert = (X509Certificate) cf.generateCertificate(fis);
            toClient.write(serverCert.getEncoded());
            toClient.flush();

            // Wait for client to finish verification
            while (true) {
                String request = in.readLine();
                if (request.equals("Handshake")){
                    System.out.println("Client has finished verifying!");
                    break;
                } else if (request.equals("No handshake")) {
                    System.out.println("Client has failed to verify. Closing connections...");
                    toClient.close();
                    fromClient.close();
                    out.close();
                    in.close();
                    connectionSocket.close();
                    break;
                } else {
                    System.out.println("Waiting for client to verify...");
                }
            }

            // Wait for client to send session key over
            while (true) {
                String request = in.readLine();
                if (request.equals("Session Key")){
                    System.out.println("Client is preparing to send session key");
                    break;
                } else {
                    System.out.println("Client is not ready");
                }
            }

            // Retrieve encrypted session key from client
            System.out.println("Retrieving session key from server...");
            int aesKeyEncryptedSize = fromClient.readInt();
            byte[] aesKeyEncrypted = new byte[aesKeyEncryptedSize];
            fromClient.readFully(aesKeyEncrypted, 0, aesKeyEncryptedSize);

            // Decrypt encrypted session key and convert the encoded form to SecretKey object
            Cipher rsaCipher2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher2.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] aesKeyDecrypted = rsaCipher2.doFinal(aesKeyEncrypted);
            SecretKey aesKey = new SecretKeySpec(aesKeyDecrypted, 0, aesKeyDecrypted.length, "AES");
            System.out.println("Decrypted session key!");

            while (!connectionSocket.isClosed()) {

                int packetType = fromClient.readInt();

                // If the packet is for transferring the filename
                if (packetType == 0) {

                    System.out.println("Receiving file...");

                    int numBytes = fromClient.readInt();
                    byte[] filename = new byte[numBytes];
                    // Must use read fully!
                    // See: https://stackoverflow.com/questions/25897627/datainputstream-read-vs-datainputstream-readfully
                    fromClient.readFully(filename, 0, numBytes);

                    fileOutputStream = new FileOutputStream("recv_"+ new String(filename, 0, numBytes));
                    bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

                    // If the packet is for transferring a chunk of the file
                } else if (packetType == 1) {

                    int numBytes = fromClient.readInt();
                    int numBytesEncrypted = fromClient.readInt();
                    byte[] block = new byte[numBytesEncrypted];
                    fromClient.readFully(block, 0, numBytesEncrypted);

                    // Decrypt the block
                    Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
                    //System.out.println(Base64.getEncoder().encodeToString(block));
                    byte[] decryptedBlock = aesCipher.doFinal(block);
                    //System.out.println(new String(decryptedBlock));

                    if (numBytes > 0)
                        bufferedFileOutputStream.write(decryptedBlock, 0, numBytes);

                    if (numBytes < 117) {
                        if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
                        if (bufferedFileOutputStream != null) fileOutputStream.close();
                    }   // File transferring is done, close all connections
                } else if (packetType == 2) {
                    System.out.println("Closing connection...");
                    toClient.close();
                    fromClient.close();
                    out.close();
                    in.close();
                    connectionSocket.close();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
