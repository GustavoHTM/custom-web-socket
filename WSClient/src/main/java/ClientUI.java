public interface ClientUI {

    public void receiveMessage(String from, String message);

    public void onClose(Runnable method);

}
