public interface ClientUI {

    public void receiveMessage(String from, String message, boolean isError);

    public void onClose(Runnable method);

}
