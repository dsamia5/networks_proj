import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private Server server; // reference to the Server

    // Initialize client handler
    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server; // Initialize the server reference
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            // Send message to other clients (work in progress to show on server and send it back)
            broadcastMessage("[SERVER]: " + clientUsername + " has joined.");
        } catch (IOException e) {
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }

	@Override
	public void run() {
		String messageFromClient;
		try {
			// Log client connection
			server.logActivity("Client " + clientUsername + " connected.");
			
			// Acknowledge successful connection to the client
			bufferedWriter.write("ACK\n");
			bufferedWriter.flush();
	
			while (socket.isConnected()) {
				messageFromClient = bufferedReader.readLine();
				
				// Check if the client wants to close the connection
				if (messageFromClient.equalsIgnoreCase("close")) {
					// Close the connection and exit the loop
					closeAll(socket, bufferedReader, bufferedWriter);
					server.logActivity("Client " + clientUsername + " disconnected."); // Log client disconnection
					break;
				}
	
				// Handle math calculation requests
				if (messageFromClient.startsWith("calculate")) {
					handleMathRequest(messageFromClient);
				} else {
					// Broadcast regular messages to other clients
					broadcastMessage(messageFromClient);
				}
			}
		} catch (IOException e) {
			closeAll(socket, bufferedReader, bufferedWriter);
		}
	}
    
    // Send out message to all clients (might be useful for future)
    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } 
            } catch (IOException e) {
                closeAll(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // Method to handle incoming math calculation requests
    public void handleMathRequest(String request) {
        server.handleMathRequest(request, this);
    }
    
    // Method to remove client from the list and broadcast its disconnection
    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat.");
    }

    // Method to close all resources and remove the client
    public void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
