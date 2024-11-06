import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;

class Task implements Serializable {
    public String taskName;
    public int id;
    public String studentName;
    boolean isDone = false;
    public String NamaMahasiswa;

    public String getTaskName() {
        return taskName;
    }

    public Boolean isDone() {
        return isDone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && taskName.equals(task.taskName);
    }
}

class Response implements Serializable {
    List<Task> responseTasks;
    String log;

    Response(List<Task> tasks, String log) {
        this.responseTasks = tasks;
        this.log = log;
    }

    public String getLog() {
        return log;
    }

    public List<Task> getResponseTasks() {
        return responseTasks;
    }
}

public class Server {
    HashMap<String, List<Task>> data;
    HashMap<String, List<ObjectOutputStream>> groupMembers;
    ServerSocket serverSocket;

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }

    void startServer() {
        this.data = new HashMap<>();
        this.groupMembers = new HashMap<>();
        try {
            serverSocket = new ServerSocket(5000);
            System.out.println("Server is running...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(new HandleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class HandleClient implements Runnable {
        String groupName;
        String clientName;
        ObjectInputStream objectInput;
        ObjectOutputStream objectOutput;

        HandleClient(Socket clientSocket) {
            try {
                objectInput = new ObjectInputStream(clientSocket.getInputStream());
                objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                while (true) {
                    Request receivedRequest = null;
                    try {
                        receivedRequest = (Request) objectInput.readObject();
                    } catch (SocketException e) {
                        handleDisconnection();
                        break;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (receivedRequest != null) {
                        switch (receivedRequest.RequestType) {
                            case "InsertClient" -> insertClient(receivedRequest);
                            case "AddTask" -> addTask(receivedRequest);
                            case "MarkDone" -> markDone(receivedRequest);
                            case "DeleteTask" -> deleteTask(receivedRequest);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleDisconnection() {
            String log = clientName + " Disconnected";
            System.out.println(log);
            Response response = new Response(data.get(groupName), log);
            groupMembers.get(groupName).remove(objectOutput);

            List<ObjectOutputStream> clients = groupMembers.get(groupName);
            if (clients != null) {
                for (ObjectOutputStream client : clients) {
                    try {
                        client.reset();
                        client.writeObject(response);
                        client.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void insertClient(Request receivedRequest) throws IOException {
            if (!groupMembers.containsKey(receivedRequest.groupName)) {
                data.put(receivedRequest.groupName, new ArrayList<>());
                groupMembers.put(receivedRequest.groupName, new ArrayList<>());
            }
            groupMembers.get(receivedRequest.groupName).add(objectOutput);
            groupName = receivedRequest.groupName;
            clientName = receivedRequest.Requester;

            String log = clientName + " Entered the Group";
            Response response = new Response(data.get(receivedRequest.groupName), log);

            for (ObjectOutputStream client : groupMembers.get(receivedRequest.groupName)) {
                client.reset();
                client.writeObject(response);
                client.flush();
            }
            System.out.println(groupMembers);
        }

        private void addTask(Request receivedRequest) throws IOException {
            int newId = data.get(receivedRequest.groupName).isEmpty() ? 0 :
                        data.get(receivedRequest.groupName).get(data.get(receivedRequest.groupName).size() - 1).id + 1;
            receivedRequest.task.id = newId;
            data.get(receivedRequest.groupName).add(receivedRequest.task);

            String log = clientName + " Added Task " + receivedRequest.task.taskName;
            broadcastResponse(receivedRequest.groupName, log);
        }

        private void markDone(Request receivedRequest) throws IOException {
            int index = data.get(receivedRequest.groupName).indexOf(receivedRequest.task);
            data.get(receivedRequest.groupName).get(index).isDone = true;

            String log = clientName + " Marked Done " + receivedRequest.task.taskName;
            broadcastResponse(receivedRequest.groupName, log);
        }

        private void deleteTask(Request receivedRequest) throws IOException {
            data.get(receivedRequest.groupName).remove(receivedRequest.task);

            String log = clientName + " Deleted Task " + receivedRequest.task.taskName;
            broadcastResponse(receivedRequest.groupName, log);
        }

        private void broadcastResponse(String groupName, String log) throws IOException {
            Response response = new Response(data.get(groupName), log);
            for (ObjectOutputStream client : groupMembers.get(groupName)) {
                client.reset();
                client.writeObject(response);
                client.flush();
            }
        }
    }
}
