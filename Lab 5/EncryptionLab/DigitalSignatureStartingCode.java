import java.util.Base64;
import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.*;


public class DigitalSignatureStartingCode {

    public static void main(String[] args) throws Exception {
        //Read the text files and save to String data
        String fileName = "shorttext.txt";
        String data = "";
        String line;
        BufferedReader bufferedReader = new BufferedReader( new FileReader(fileName));
        while((line= bufferedReader.readLine())!=null){
            data = data +"\n" + line;
        }
        System.out.println("Original content: "+ data);
        String fileName2 = "longtext.txt";
        String data2 = "";
        String line2;
        BufferedReader bufferedReader2 = new BufferedReader( new FileReader(fileName2));
        while((line2= bufferedReader2.readLine())!=null){
            data2 = data2 +"\n" + line2;
        }

        //TODO: generate a RSA keypair, initialize as 1024 bits, get public key and private key from this keypair.
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keyPair = keyGen.generateKeyPair();
        Key publicKey = keyPair.getPublic();
        Key privateKey = keyPair.getPrivate();

        //TODO: Calculate message digest, using MD5 hash function
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data.getBytes());
        byte[] digest2 = md.digest(data2.getBytes());

        //TODO: print the length of output digest byte[], compare the length of file smallSize.txt and largeSize.txt
        System.out.println("Length of file shorttext.txt is " + digest.length +
                " and length of file longtext.txt is " + digest2.length);
           
        //TODO: Create RSA("RSA/ECB/PKCS1Padding") cipher object and initialize is as encrypt mode, use PRIVATE key.
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, privateKey);

        //TODO: encrypt digest message
        byte[] cipherBytes = rsaCipher.doFinal(digest);
        byte[] cipherBytes2 = rsaCipher.doFinal(digest2);

        // 2. Compare the sizes of the signed message digests for shorttext.txt and longtext.txt.
        // Does a larger file size give a longer signed message digest?
        System.out.println("Size of signed message digest for shortttext.txt is " + cipherBytes.length +
                " and size of signed message digest for longtext.txt is " + cipherBytes2.length);

        //TODO: print the encrypted message (in base64format String using DatatypeConverter)
        String base64format = Base64.getEncoder().encodeToString(cipherBytes);
        System.out.println(base64format);

        //TODO: Create RSA("RSA/ECB/PKCS1Padding") cipher object and initialize is as decrypt mode, use PUBLIC key.
        Cipher rsaCipher2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher2.init(Cipher.DECRYPT_MODE, publicKey);

        //TODO: decrypt message
        byte[] plainBytes = rsaCipher2.doFinal(cipherBytes);

        //TODO: print the decrypted message (in base64format String using DatatypeConverter), compare with origin digest
        String originalDigest = Base64.getEncoder().encodeToString(digest);
        String decryptedMessage = Base64.getEncoder().encodeToString(plainBytes);
        System.out.println("Original digest is " + originalDigest +
                " and decrypted message is " + decryptedMessage);

    }

}