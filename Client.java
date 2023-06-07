import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
	//send request to the server
	private static class Listener implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				BufferedReader br_in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String read;
				while (true)
				{
					read = br_in.readLine();
					if (read != null && !(read.isEmpty()))
						System.out.println(read);
				}
			} catch (IOException e)
			{
				return;
			}
		}

	}

	// writing portion of a server to client communication
	private static class Writer implements Runnable
	{
		private PrintWriter br_out;

		@Override
		public void run()
		{
			Scanner write = new Scanner(System.in);

			try
			{
				br_out = new PrintWriter(clientSocket.getOutputStream(), true);
				while (true)
				{
					if (write.hasNext())
						br_out.println(write.nextLine());
				}

			} catch (IOException e)
			{
				write.close();
				return;
			}
		}

	}

	// connection through a java socket
	private static Socket clientSocket;
	public static void main(String[] args)
	{
		if(args.length != 1)
			throw new IllegalArgumentException();
		try
		{
			clientSocket = new Socket(args[0], 55000);
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// start the communicating threads
		new Thread(new Writer()).start();
		new Thread(new Listener()).start();

	}
}