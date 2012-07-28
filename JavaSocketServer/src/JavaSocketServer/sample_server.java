package JavaSocketServer;

import java.net.*;

public class sample_server {
    private static int port = 4444;
    private static int maxConnections = 0;
	
	public static void main(String[] args) {
		int i =0;
		
		try
		{
			ServerSocket listener = new ServerSocket(port);
			Socket server;
			
			while((i++<maxConnections)||(maxConnections ==0))
			{
				server = listener.accept();
				SocketServer connection = new SocketServer(server);
				Thread t = new Thread(connection);
				t.start();
			}
		}
		catch(Exception ioe)
		{
			System.out.println("IOException on socket listen on:" + ioe);
			ioe.printStackTrace();			
		}

	}

}
