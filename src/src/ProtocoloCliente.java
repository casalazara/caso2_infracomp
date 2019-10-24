package src;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class ProtocoloCliente {
	public final static String HOLA="HOLA";
	public final static String OK="OK";
	public final static String RSA="RSA";
	public final static String AES="AES";
	public final static String ALGORITMOS="ALGORITMOS";
	public final static String ERROR="ERROR";
	public final static String HMAC="HMACSHA512";
	private static int estadoActual;

	private static X509Certificate certSer;
	private static SecretKey sk;

	public ProtocoloCliente()
	{	 
		estadoActual=0;
	}

	public static void procesar(BufferedReader stdIn,BufferedReader pIn,PrintWriter pOut)throws IOException, CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		//Empiezo la comunicación
		pOut.println(HOLA);
		String linea=pIn.readLine();
		String rta="";
		System.out.println("cliente recibio-" + linea + "-continuando.");

		if(linea.equals(OK)&& estadoActual==0)
		{
			//Si leo OK envío los algoritmos a usar.
			rta=ALGORITMOS+":"+AES+":"+RSA+":"+HMAC;
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
				certSer.checkValidity();
				System.out.println("Certificado válido");

				//Genero la llave simétrica
				sk=KeyGenerator.getInstance(AES).generateKey();
				byte[] ks=sk.getEncoded();

				//Encripto con la llave pública del servidor (sobre)
				Cipher cifradorRSA=Cipher.getInstance(RSA);
				cifradorRSA.init(Cipher.ENCRYPT_MODE,certSer.getPublicKey());
				byte[] ba=cifradorRSA.doFinal(ks);
				rta=DatatypeConverter.printBase64Binary(ba);
				pOut.println(rta);
				System.out.println("cliente envió llave simétrica-" + rta + "-continuando.");


				//Envio reto
				rta="casa";
				pOut.println(rta);
				System.out.println("cliente envió reto-" + rta + "-continuando.");

				//Leo la respuesta al reto que me envió el servidor
				linea=pIn.readLine();
				Cipher cifradorAES=Cipher.getInstance(AES);
				cifradorAES.init(Cipher.DECRYPT_MODE,sk);
				ba=cifradorAES.doFinal(DatatypeConverter.parseBase64Binary(linea));
				linea=DatatypeConverter.printBase64Binary(ba);
				System.out.println("cliente recibio reto-" + linea + "-continuando.");

				//Si son iguales continuo
				if(linea.equals(rta))
				{
					rta=OK;
				}
				else 
				{
					rta=ERROR;
				}
				//Confirmo si recibí bien o no
				pOut.println(rta);

				//Envío la cédula
				rta=cifrarSimetrico("001005755560",AES);
				pOut.println(rta);
				System.out.println("cliente envió cédula-" + rta + "-continuando.");

				//Envío la clave
				rta=cifrarSimetrico("conTraSeNaS3GuR4",AES);
				pOut.println(rta);
				System.out.println("cliente envió contraseña-" + rta + "-continuando.");

				//Leo el valor que me envió el servidor
				String valor=pIn.readLine();
				cifradorAES=Cipher.getInstance(AES);
				cifradorAES.init(Cipher.DECRYPT_MODE,sk);
				ba=cifradorAES.doFinal(DatatypeConverter.parseBase64Binary(valor));
				valor=DatatypeConverter.printBase64Binary(ba);
				System.out.println("cliente recibio valor-" + valor + "-continuando.");

				//Recibo el valor cifrado con la llave pública del servidor y el HMAC establecido y lo descifro.

				//Primero descifro el HMAC con la llave pública del servidor.
				linea=pIn.readLine();
				cifradorRSA.init(Cipher.DECRYPT_MODE,certSer.getPublicKey());
				ba=cifradorRSA.doFinal(DatatypeConverter.parseBase64Binary(linea));

				//Luego descifro con la llave secreta y usando HMAC
				Mac cifradorHMAC=Mac.getInstance(HMAC);
				cifradorHMAC.init(sk);
				byte[] be=cifradorHMAC.doFinal(DatatypeConverter.parseBase64Binary(valor));

				//Verifico que el valor sea igual a lo que acabo de descifrar.
				if(Arrays.equals(ba,be))
				{
					System.out.println("Las funciones de Hash correspondientes a los valores coinciden");
					rta=OK;
				}
				else
				{
					rta=ERROR;
				}
				pOut.println(rta);
				//Termina la conexión.
				System.out.println("Conexión terminada con "+rta+".");

			}
		}
	}

	public static String cifrarSimetrico(String mensaje, String algoritmo) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		if(mensaje.length()%4!=0)
			for(int i=0;i<mensaje.length()%4;i++)
				mensaje+="0";
		Cipher cifradorAES=Cipher.getInstance(AES);
		cifradorAES.init(Cipher.ENCRYPT_MODE,sk);
		byte[] ba=cifradorAES.doFinal(DatatypeConverter.parseBase64Binary(mensaje));
		return DatatypeConverter.printBase64Binary(ba);
	}


}