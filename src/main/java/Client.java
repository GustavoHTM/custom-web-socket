import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Client {

    private Socket socket;
    private String ip;
    private Client currentConnection;
    private String name;

    private final Map<String, ArrayList<Message>> connectionMessageHistory = new HashMap<>();

    public Client(Socket socket) {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        this.name = this.ip;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Client getCurrentConnection() {
        return currentConnection;
    }

    public void setCurrentConnection(Client currentConnection) {
        this.currentConnection = currentConnection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
