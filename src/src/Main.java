package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Main {

	public final static int PUERTO=3400;
	public final static String SERVIDOR="localhost";

	public static void main(String[] args) throws IOException, CertificateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		Security.addProvider((Provider)new BouncyCastleProvider());

		System.out.println("CLIENTE: Conectando al servidor:");
		Socket socket=null;
		PrintWriter escritor=null;
		BufferedReader lector=null;

		try {
			System.out.println("CLIENTE: Conectando al servidor "+SERVIDOR+" en el puerto "+PUERTO);
			socket = new Socket(SERVIDOR,PUERTO);
			escritor=new PrintWriter(socket.getOutputStream(),true);
			lector=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}

		ProtocoloCliente.procesar(lector,escritor);
		escritor.close();
		lector.close();
		socket.close();
	}
}
