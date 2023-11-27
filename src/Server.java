import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;

class Task implements Serializable{
    public String TaskName;
    public int id;
    public String NamaMahasiswa;
    boolean isDone = false;
    public String getTaskName() {
        return TaskName;
    }
    public Boolean isDone(){
        return isDone;
    }   
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task myObject = (Task) o;
        return id == myObject.id && TaskName.equals(myObject.TaskName);
    }
}

class Response implements Serializable{
    List<Task> ResponseTask;
    String Logs;
    Response(List<Task> Tasks, String Log){
        this.ResponseTask = Tasks;
        this.Logs = Log;
    }
    public String getLogs() {
        return Logs;
    }
    public List<Task> getResponseTask() {
        return ResponseTask;
    }
}

public class Server {
    HashMap<String, List<Task>> Data;
    HashMap<String, List<ObjectOutputStream>> GroupMember;
    // HashMap<String, List<String>> Logs;
    ServerSocket O_ServerSocket;
    public static void main(String[] args) {
        // this.Data = new HashMap<>();
        Server O_Server = new Server();
        O_Server.StartServer();
    }
    void StartServer(){
        this.Data = new HashMap<>();
        this.GroupMember = new HashMap<>();
        try {
            O_ServerSocket = new ServerSocket(5000);
            System.out.println("Server is running...");
            while (true) {
                Socket clientSocket = O_ServerSocket.accept();

                Thread clienThread = new Thread(new HandleClient(clientSocket));
                clienThread.start();
            }
            
        } catch (IOException | NullPointerException e) {

            e.printStackTrace();
            // TODO: handle exception
        }
        
    }
    public class HandleClient implements Runnable{
        String groupName;
        String ClientName;
        ObjectInputStream objectInput;
        ObjectOutputStream objectOutput;
        HandleClient(Socket clientSocket){
            // this.groupName = ;
            try {
                objectInput = new ObjectInputStream(clientSocket.getInputStream());
                objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                
                e.printStackTrace();
                // TODO: handle exception
            }
            
        }
        public void run(){
            try {
                while (true) {
                    // System.out.println("1");
                    // ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                    Request receivedRequest = null;
                    try {
                        receivedRequest = (Request) objectInput.readObject();
                    } catch (SocketException e) {
                        // System.out.println("Disconnected");
                        String Log =  this.ClientName + " Disconnected ";
                        System.out.println(Log);
                        Response Response = new Response(Data.get(groupName), Log);
                        GroupMember.get(groupName).remove(objectOutput);
                        List<ObjectOutputStream> clients = GroupMember.get(groupName);
                        if (clients != null) {
                            for (ObjectOutputStream client : clients) {
                                // System.out.println(GroupMember);
                                client.reset();
                                client.writeObject(Response);
                                client.flush();
                            }
                        }
                        // TODO: handle exception
                    }catch(ClassNotFoundException e){
                        
                        e.printStackTrace();
                    }
                    // objectInput.reset();
                    // System.out.println(receivedRequest.RequestType.equals("InsertClient"));
                    if (receivedRequest.RequestType.equals("InsertClient")) {
                        if (GroupMember.containsKey(receivedRequest.groupName)) {
                            GroupMember.get(receivedRequest.groupName).add(objectOutput);
                        }else{
                            Data.put(receivedRequest.groupName, new ArrayList<Task>());
                            GroupMember.put(receivedRequest.groupName, new ArrayList<ObjectOutputStream>());
                            GroupMember.get(receivedRequest.groupName).add(objectOutput);
                        }
                        this.groupName = receivedRequest.groupName;
                        this.ClientName = receivedRequest.Requester;
                        String Log = receivedRequest.Requester + " Enter The Group ";
                        Response Response = new Response(Data.get(receivedRequest.groupName), Log);

                        List<ObjectOutputStream> clients = GroupMember.get(receivedRequest.groupName);
                        if (clients != null) {
                            for (ObjectOutputStream client : clients) {
                                // System.out.println(GroupMember);
                                client.reset();
                                client.writeObject(Response);
                                client.flush();
                            }
                        }
                        System.out.println(GroupMember);
                    }else if (receivedRequest.RequestType.equals("AddTask")) {
                        // System.out.println("Error??");
                        addTask(receivedRequest, objectOutput);
                    }else if(receivedRequest.RequestType.equals("MarkDone")){
                        markDone(receivedRequest, objectOutput);
                    }else if(receivedRequest.RequestType.equals("DeleteTask")){
                        deleteTask(receivedRequest, objectOutput);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: handle exception
            }
        }
    }
    // void HandleClient(Socket clientSocket){
    //     try {
    //         ObjectInputStream objectInput = new ObjectInputStream(clientSocket.getInputStream());
    //         ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
    //         while (true) {
    //             // System.out.println("1");
    //             // ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
    //             Request receivedRequest = (Request) objectInput.readObject();
    //             // objectInput.reset();
    //             if (receivedRequest.RequestType.equals("AddTask")) {
    //                 // System.out.println("Error??");
    //                 addTask(receivedRequest, objectOutput);
    //             }else if(receivedRequest.RequestType.equals("MarkDone")){
    //                 markDone(receivedRequest, objectOutput);
    //             }else if(receivedRequest.RequestType.equals("DeleteTask")){
    //                 deleteTask(receivedRequest, objectOutput);
    //             }
    //         }
    //     } catch (IOException | ClassNotFoundException e) {
    //         e.printStackTrace();
    //         // TODO: handle exception
    //     }
        
    // }
    void addTask(Request receivedRequest, ObjectOutputStream clientSocket){
        try {
            // receivedRequest.task.id = 
            if(Data.get(receivedRequest.groupName).isEmpty()){
                receivedRequest.task.id = 0;
            }else{
                receivedRequest.task.id = Data.get(receivedRequest.groupName).get(Data.get(receivedRequest.groupName).size()-1).id + 1;
            }
            Data.get(receivedRequest.groupName).add(receivedRequest.task);
            String Log = receivedRequest.Requester + " Added Task " + receivedRequest.task.TaskName;
            Response Response = new Response(this.Data.get(receivedRequest.groupName), Log);
            List<ObjectOutputStream> clients = GroupMember.get(receivedRequest.groupName);
            if (clients != null) {
                for (ObjectOutputStream client : clients) {
                    client.reset();
                    client.writeObject(Response);
                    client.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
        
    }
    void markDone(Request receivedRequest, ObjectOutputStream clientSocket){
        try {
            
            int index = Data.get(receivedRequest.groupName).indexOf(receivedRequest.task);
            Data.get(receivedRequest.groupName).get(index).isDone = true;
            // System.out.println(index);
            // System.out.println(receivedRequest.task);
            String Log = receivedRequest.Requester + " Marked Done " + receivedRequest.task.TaskName;
            Response Response = new Response(this.Data.get(receivedRequest.groupName), Log);
            List<ObjectOutputStream> clients = GroupMember.get(receivedRequest.groupName);
            if (clients != null) {
                for (ObjectOutputStream client : clients) {
                    client.reset();
                    client.writeObject(Response);
                    client.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }
    void deleteTask(Request receivedRequest, ObjectOutputStream clientSocket){
        try {
            Data.get(receivedRequest.groupName).remove(receivedRequest.task);
            String Log = receivedRequest.Requester + " Delete Task " + receivedRequest.task.TaskName;
            Response Response = new Response(this.Data.get(receivedRequest.groupName), Log);
            List<ObjectOutputStream> clients = GroupMember.get(receivedRequest.groupName);
            if (clients != null) {
                for (ObjectOutputStream client : clients) {
                    client.reset();
                    client.writeObject(Response);
                    client.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }
}
