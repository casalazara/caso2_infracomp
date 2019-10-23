package src;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Random;
import java.security.cert.Certificate;


import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class ProtocoloCliente {
	public final static String HOLA="HOLA";
	public final static String OK="OK";
	public final static String RSA="RSA";
	public final static String AES="AES";
	public final static String ALGORITMOS="ALGORITMOS";
	private static int estadoActual=0;

	private static X509Certificate certSer;
	private static SecretKey sk;

	public ProtocoloCliente()
	{	          
	}

	public static void procesar(BufferedReader stdIn,BufferedReader pIn,PrintWriter pOut)throws IOException, CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		pOut.println(HOLA);
		String linea=pIn.readLine();
		String rta="";
		System.out.println("cliente recibio-" + linea + "-continuando.");

		if(linea.equals(OK)&& estadoActual==0)
		{
			rta=ALGORITMOS+":"+AES+":"+RSA+":HMACSHA512";
			pOut.println(rta);
			System.out.println("cliente envió-" + rta + "-continuando.");
			estadoActual++;
		}

		linea=pIn.readLine();
		System.out.println("cliente recibio-" + linea + "-continuando.");
		if(linea.equals(OK)&& estadoActual==1)
		{
			String certificado=pIn.readLine();
			if(certificado!=null)
			{
				//Leo el certificado del servidor
				byte[] certificadoServidorBytes = DatatypeConverter.parseBase64Binary(certificado);
				CertificateFactory creator = CertificateFactory.getInstance("X.509");
				InputStream in = new ByteArrayInputStream(certificadoServidorBytes);
				certSer = (X509Certificate) creator.generateCertificate(in);
				System.out.println("Certificado Servidor: " + certSer);


				//Genero la llave simétrica
				sk=KeyGenerator.getInstance(AES).generateKey();
				byte[] ks=sk.getEncoded();

				//Encripto con la llave pública del servidor (sobre)
				Cipher cifrador=Cipher.getInstance(RSA);
				cifrador.init(Cipher.ENCRYPT_MODE,certSer.getPublicKey());
				byte[] ba=cifrador.doFinal(ks);
				rta=DatatypeConverter.printBase64Binary(ba);
				System.out.println(rta.length());
				if(rta.length()%4!=0)
					for(int i=0;i<rta.length()%4;i++)
						rta+="0";
				System.out.println(rta.length()+"despues");

				pOut.println(rta);
				System.out.println("cliente envió-" + rta + "-continuando.");


				//Envio reto
				rta="No sé qué poner";
				pOut.println(rta);
				System.out.println("cliente envió-" + rta + "-continuando.");

				estadoActual++;
			}
		}
	}

	//	public static String darAleatorio()
	//	{
	//		Random r = new Random();
	//		int x= r.nextInt((3 - 0) + 1);
	//		if(x==1)
	//		{
	//			alg=AES;
	//		}
	//		else if(x==2)
	//		{
	//			alg=AES;
	//		}
	//		else if(x==3)
	//		{
	//			alg=BLOWFISH;
	//		}
	//		else
	//		{
	//			alg=RC4;
	//		}
	//		return alg;
	//	}
}