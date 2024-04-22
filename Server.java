import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private ServerSocket serverSocket;
    private Map<ClientHandler, Long> connectedClients;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.connectedClients = new HashMap<>();
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A Client has joined");
                ClientHandler clientHandler = new ClientHandler(socket, this);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to log client activities
    public void logActivity(String message) {
        // Log the message with timestamp
        System.out.println("[" + java.time.LocalDateTime.now() + "] " + message);
    }

    // Method to handle incoming math calculation requests
    public void handleMathRequest(String request, ClientHandler clientHandler) {
        // Implement math calculation logic here
        // For simplicity, let's assume the request format is "calculate <operation> <operands>"
    }
	
	public void closeSocket() {
		
		try {
			if(serverSocket != null) {
				serverSocket.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(6666);
		Server server = new Server(serverSocket);
		server.startServer();
	}
}
