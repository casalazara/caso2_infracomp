package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class ProtocoloCliente {
	public final static String HOLA="HOLA";
	public final static String OK="OK";
	public final static String DES="DES";
	public final static String AES="AES";
	public final static String BLOWFISH="Blowfish";
	public final static String RC4="RC4";
	public final static String ALGORITMOS="ALGORITMOS";
	private static String alg;


	public static void procesar(BufferedReader stdIn,BufferedReader pIn,PrintWriter pOut)throws IOException
	{
		pOut.println(HOLA);
		String linea=pIn.readLine();
		String rta="";
        System.out.println("cliente recibio-" + linea + "-continuando.");

		if(linea.equals(OK))
		{
			rta=ALGORITMOS+":"+darAleatorio();
			pOut.println(rta);
	        System.out.println("cliente envió-" + rta + "-continuando.");
		}
		
		linea=pIn.readLine();
        System.out.println("cliente recibio-" + linea + "-continuando.");

		if(linea.equals(OK))
		{
			
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