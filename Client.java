import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String username;
	
	// Construct Client with socket and username
	public Client(Socket socket, String username) {
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.username = username;
		} catch (IOException e) {
			closeAll(socket, bufferedReader, bufferedWriter);
		}
	}
	
	// Send message 
	public void sendMessage() {
		try {
			// Sends username to clientHandler
			bufferedWriter.write(username);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			Scanner scanner = new Scanner(System.in);
			// While connected send message
			while (socket.isConnected() ) {
				String messageToSend = scanner.nextLine();
				if (messageToSend.equalsIgnoreCase("close")) {
					closeConnection(); // Call the closeConnection() method to terminate the connection
					break;
				}
				bufferedWriter.write(messageToSend);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
		} catch (IOException e) {
			closeAll(socket, bufferedReader, bufferedWriter);
		}
	}

	// Another thread to avoid blocking, allows people to read messages without having to write message
	public void listenForMessage() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String msgFromChat;
				
				while(socket.isConnected()) {
					try {
						msgFromChat = bufferedReader.readLine();
                        if (msgFromChat == null) {
                            break;
                        }
						System.out.println(msgFromChat);
					} catch (IOException e) {
						closeAll(socket, bufferedReader, bufferedWriter);
					}
				}
			}
		}).start();
	}

	// Method to send close connection request to the server
	public void closeConnection() {
		try {
			bufferedWriter.write("close\n"); // Send close connection request to the server
			bufferedWriter.flush();
			closeAll(socket, bufferedReader, bufferedWriter); // Close the client connection locally
			System.exit(0); // Close the client terminal
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	// Exit out completely 
	public void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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
		
		public static void main(String[] args) throws UnknownHostException, IOException {
			
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter your name: ");
			String username = scanner.nextLine();
			Socket socket = new Socket("localhost", 6666);
			Client client = new Client(socket, username);
			client.listenForMessage();
			client.sendMessage();
		}
}
