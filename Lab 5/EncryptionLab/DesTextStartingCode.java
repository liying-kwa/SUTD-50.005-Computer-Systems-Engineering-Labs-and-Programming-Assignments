import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.crypto.*;
import java.util.Base64;


public class DesTextStartingCode {
    public static void main(String[] args) throws Exception {

        // 1. Try to print to your screen the content of the input files
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
        //System.out.println("Original content: "+ data2);

        // Carry out DES encryption
        //TODO: generate secret key using DES algorithm
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        SecretKey desKey = keyGen.generateKey();
        //TODO: create cipher object, initialize the ciphers with the given key, choose encryption mode as DES
        Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        desCipher.init(Cipher.ENCRYPT_MODE, desKey);
        //TODO: do encryption, by calling method Cipher.doFinal().
        byte[] cipherBytes = desCipher.doFinal(data.getBytes());

        // 2. Store the output ciphertext (in byte[] format) to a variable cipherBytes. Is it printable?
        System.out.println(new String(cipherBytes));

        // 3. Convert the ciphertext into Base64 format and print it to the screen. Is it printable?
        //TODO: do format conversion. Turn the encrypted byte[] format into base64format String using DatatypeConverter
        String base64format = Base64.getEncoder().encodeToString(cipherBytes);
        //TODO: print the encrypted message (in base64format String format)
        System.out.println(base64format);

        // 4. Is Base64 encoding a cryptographic operation? Why or why not?

        // 5. Print out the decrypted ciphertext for the small file. Is the output the same as for Q1?
        //TODO: create cipher object, initialize the ciphers with the given key, choose decryption mode as DES
        Cipher desCipher2 = Cipher.getInstance("DES/ECB/PKCS5Padding");
        desCipher2.init(Cipher.DECRYPT_MODE, desKey);
        //TODO: do decryption, by calling method Cipher.doFinal().
        byte[] plainBytes = desCipher2.doFinal(cipherBytes);
        //TODO: do format conversion. Convert the decrypted byte[] to String, using "String a = new String(byte_array);"
        //TODO: print the decrypted String text and compare it with original text
        System.out.println(new String(plainBytes));

        // 6. Compare the lengths of the encryption result (in byte[] format) for
        //smallFile.txt and largeFile.txt. Does a larger file give a larger encrypted byte array? Why?
        //TODO: print the length of output encrypted byte[], compare the length of file smallSize.txt and largeSize.txt
        byte[] cipherBytes2 = desCipher.doFinal(data2.getBytes());
        System.out.println("Length of file shorttext.txt is " + cipherBytes.length +
                " and length of file longtext.txt is " + cipherBytes2.length);

    }
}