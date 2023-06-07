import java.io.*;
import java.net.*;
import java.util.*;

public class Server
{
//HAndling client and maintaining thread
	private static class ClientHandler implements Runnable
	{
		private Socket sock;
		private String user_name;
		public ClientHandler(Socket sock)
		{
			this.sock = sock;
		}

		@Override
		public void run()
		{
			if (verbose)
				System.out.println("client connection established " + sock.getInetAddress());

			try
			{
				BufferedReader br_in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				PrintWriter br_out = new PrintWriter(sock.getOutputStream(), true);

				while (true)
				{
					br_out.println("Enter your user name: ");
					user_name = br_in.readLine();

					if (user_name == null)
					{
						return;
					}

					// prevent duplicate names
					synchronized (connectedClients)
					{
						if (!user_name.isEmpty()
								&& !connectedClients.keySet().contains(user_name))
							break;
						else
							br_out.println("Name invalid or already used by another client.");
					}
				}

				if (verbose)
					System.out.println(user_name + " has joined");

				broadcasting(user_name + " is now online", user_name);
				connectedClients.put(user_name, br_out);

				String msg;
				while ((msg = br_in.readLine()) != null)
				{
					if (!(msg.isEmpty()))
					{
						//leaves the chat
						if (msg.toLowerCase().equals("offline"))
						{
							break;
						}
						//Unicasting
						else if (msg.toLowerCase().startsWith("send "))
						{
							System.out.println(user_name + " is unicasting to " +msg.split(" ")[1] );
							unicasting(msg.split(" ", 3)[2], user_name, msg.split(" ")[1]);
						}
						// broadcast
						else if (msg.toLowerCase().startsWith("broadcast "))
						{
							System.out.println(user_name + " is broadcasting ");
							broadcasting(user_name + ": " + msg.split(" ", 2)[1], user_name);
						}
						//listing the online clients
						else if(msg.toLowerCase().startsWith("list"))
						{
							listOnlineClients(br_out);
						}
						else 
							br_out.println("Invlaid command.");

					}
				}

			} catch (Exception e)
			{
				if (verbose)
					System.out.println(e);
			}

			finally
			{
				// Remove client
				if (user_name != null)
				{
					if (verbose)
						System.out.println(user_name + " leaves the server....");

					connectedClients.remove(user_name);
					broadcasting(user_name + " OFFLINE", user_name);
				}
			}
		}

	}

	// Map of clients which are connected to the server
	private static HashMap<String, PrintWriter> connectedClients = new HashMap<>();
	// max  no of clients
	private static final int MAX_CLIENT = 10;
	// Server port
	private static final int PORT_NO = 55000;
	private static boolean verbose;
	private static ServerSocket listener;

	// List the onine clients
	private static void listOnlineClients(PrintWriter p){
		for (HashMap.Entry<String, PrintWriter> set : connectedClients.entrySet()) {
			p.println("ONLINE : "+set.getKey());
		}

	}
	//method to unicast
	private static void unicasting(String msg, String sender, String rec){
		for (HashMap.Entry<String, PrintWriter> set : connectedClients.entrySet()) {
			
			if(set.getKey().equals(rec))
				set.getValue().println("From " + sender + " : " + msg);
            // System.out.println(set.getKey() + " = "+ set.getValue());
        }
	}
	// method to broadcast
	private static void broadcasting(String msg, String user_name)
	{
		// for (PrintWriter p : connectedClients.values())
		// 		p.println(msg);
		//System.out.println(user_name + " is broadcasting ");
		for (HashMap.Entry<String, PrintWriter> set : connectedClients.entrySet())
		{
			if(set.getKey().equals(user_name))
			{
				continue;
			}
			set.getValue().println(msg);
        }
	}

	//Starts the server for listening
	public static void start(boolean isVerbose)
	{
		verbose = isVerbose;

		try
		{
			listener = new ServerSocket(PORT_NO);
			if (verbose)
			{
				System.out.println("Server started >>>> " + PORT_NO);
				System.out.println("Starts listening .......");
			}

			// accept new sock connections
			while (true)
			{
				if (connectedClients.size() <= MAX_CLIENT)
				{
					// dispatch a new ClientHandler thread to the sock connection
					Thread newClient = new Thread(new ClientHandler(listener.accept()));
					newClient.start();
				}

			}
		}
		catch (BindException e)
		{
			// server already started on this port ... continue
		}
		catch (Exception e)
		{
			// error verbose
			if (verbose)
			{
				System.out.println("\nError occured: \n");
				e.printStackTrace();
				System.out.println("\nExiting...");
			}
		}
	}

	public static void stop() throws IOException
	{
		if (!listener.isClosed())
			listener.close();
	}

	//Main method
	public static void main(String[] args) throws IOException
	{
		boolean isVerbose;
		isVerbose = (args.length == 1
				&& args[0].toLowerCase().equals("verbose")) ? true : false;
		start(!isVerbose);
	}
}