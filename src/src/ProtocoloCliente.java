package src;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.security.cert.Certificate;


import java.security.cert.CertificateException;

import javax.xml.bind.DatatypeConverter;

public class ProtocoloCliente {
	public final static String HOLA="HOLA";
	public final static String OK="OK";
	public final static String DES="DES";
	public final static String AES="AES";
	public final static String BLOWFISH="Blowfish";
	public final static String RC4="RC4";
	public final static String ALGORITMOS="ALGORITMOS";
	private static String alg;
	private static int estadoActual=0;
	private byte[] bytes;

	private static Certificate certSer;


	public ProtocoloCliente()
	{	          
		this.bytes = new byte[520];
	}

	public static void procesar(BufferedReader stdIn,BufferedReader pIn,PrintWriter pOut)throws IOException, CertificateException
	{
		pOut.println(HOLA);
		String linea=pIn.readLine();
		String rta="";
		System.out.println("cliente recibio-" + linea + "-continuando.");

		if(linea.equals(OK)&& estadoActual==0)
		{
			rta=ALGORITMOS+":"+"DES:RSA:HMACSHA512";
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
				byte[] certificadoServidorBytes = DatatypeConverter.parseBase64Binary(certificado);
				java.security.cert.CertificateFactory creator = java.security.cert.CertificateFactory.getInstance("X.509");
				InputStream in = new ByteArrayInputStream(certificadoServidorBytes);
				certSer = creator.generateCertificate(in);
				System.out.println("Certificado Servidor: " + certSer);
				
				
				rta="";
				System.out.println("cliente envió-" + rta + "-continuando.");
				estadoActual++;
			}
		}
	}

	public static String darAleatorio()
	{
		Random r = new Random();
		int x= r.nextInt((3 - 0) + 1);
		if(x==1)
		{
			alg=DES;
		}
		else if(x==2)
		{
			alg=AES;
		}
		else if(x==3)
		{
			alg=BLOWFISH;
		}
		else
		{
			alg=RC4;
		}
		return alg;
	}
}