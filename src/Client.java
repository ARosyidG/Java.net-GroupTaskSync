import java.net.Socket;
// import java.util.ArrayList;
// import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
// import javax.swing.SwingUtilities;

import java.io.*;

class Request implements Serializable{
    public String RequestType;
    public Task task;
    public String groupName;
    public String Requester;
    Request(){

    }
    Request(String RequestType, Task task, String groupName){
        if (RequestType.equals("AddTask")) {
            this.RequestType = RequestType;
            this.task = task;
        }else if (RequestType.equals("MarkDone")) {
            
        }else if (RequestType.equals("DeleteTask")) {
            
        }else{
            System.out.println("Request Type Can't equals to " + RequestType);
        }
    }
    Request(String RequestType, String Text){
        if (RequestType.equals("InsertClient")){
            this.RequestType = RequestType;
            this.groupName = Text;
        }
    }
    public void addTask(String groupName, Task task){
        this.groupName = groupName;
        this.task = task;
    }
}
public class Client extends JFrame{
    ObjectOutputStream Sender;
    ObjectInputStream receiver;
    String GroupName;
    String ClientName;
    DefaultListModel<Task> Tasks; 
    DefaultListModel<String> LogsModel; 
    // List<Task> tasks = new ArrayList<Task>();
    JList<String> Logs;
    JList<Task> Jtask;
    
    public static void main(String[] args) {
        Client client = new Client();
        client.RunAPP();
    }
    void RunAPP(){    

        final String serverAddress = "localhost";
        final int serverPort = 5000;
        Scanner input = new Scanner(System.in);
        try (Socket socket = new Socket(serverAddress, serverPort)){
            this.ClientName = JOptionPane.showInputDialog("Enter Your Name");
            this.GroupName = JOptionPane.showInputDialog("Enter Group Name");

            if (ClientName == null || GroupName.trim().isEmpty() || GroupName == null || GroupName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Name and group name cannot be empty. Exiting.");
                System.exit(0);
            }
            setTitle("To-Do List Client - " + ClientName + " (Group: " + GroupName + ")");
            setSize(600, 300);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Tasks = new DefaultListModel<Task>();
            // Tasks.addElement(new Task());;
            Jtask = new JList<>(Tasks);
            // Jtask.setCellRenderer(new DefaultListCellRenderer());
            Jtask.setCellRenderer(new TaskCellRenderer());
            // System.out.println(Jtask.getModel());

            // JButton Test = new JButton("Test");
            JButton addButton = new JButton("Add Task");
            JButton removeButton = new JButton("Remove Selected Task");
            JButton markDoneButton = new JButton("Mark as Done");
            
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    AddTask();
                }
            });
            removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    DeleteTask();
                }
            });
            markDoneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    MarkDone();
                }
            });
            // Layout
            JPanel mainPanel = new JPanel(new GridLayout(1, 3));

            JPanel tasksPanel = new JPanel(new BorderLayout());
            tasksPanel.add(new JScrollPane(this.Jtask), BorderLayout.CENTER);
            
            
            JPanel buttonsPanel = new JPanel(new GridLayout(2, 1));
            buttonsPanel.add(addButton);
            buttonsPanel.add(removeButton);

            tasksPanel.add(buttonsPanel, BorderLayout.SOUTH);

            JPanel doneTasksPanel = new JPanel(new BorderLayout());
            doneTasksPanel.add(markDoneButton, BorderLayout.SOUTH);
            // tasksPanel.add(Test, BorderLayout.WEST);
            // JPanel LogsPanel = new JPanel(new BorderLayout());
            
            doneTasksPanel.add(new JLabel("LOGS"), BorderLayout.NORTH);
            LogsModel = new DefaultListModel<String>();
            Logs = new JList<String>(LogsModel);
            doneTasksPanel.add(Logs, BorderLayout.CENTER);
            
            mainPanel.add(tasksPanel);
            mainPanel.add(doneTasksPanel);

            add(mainPanel);
            setVisible(true);
            // System.exit(0);
            //connect to server
            Sender = new ObjectOutputStream(socket.getOutputStream());
            receiver = new ObjectInputStream(socket.getInputStream());
            // objectOutput.writeObject(newRequest);
            Request clientLogin = new Request("InsertClient",this.GroupName);
            clientLogin.Requester = this.ClientName;
            // clientLogin.RequestType = "InsertClient";
            // clientLogin.groupName = "AA";
            Sender.reset();
            Sender.writeObject(clientLogin);
            Sender.flush();
            try {
                Thread read = new Thread(()->{
                    try {
                        while (true) {
                            // List<Task> receivedTask = (List<Task>) receiver.readObject();
                            Response receivedResponse= (Response) receiver.readObject();
                            this.LogsModel.addElement(receivedResponse.getLogs());
                            // System.out.println(receivedResponse.getLogs());
                            System.out.println(receivedResponse.getResponseTask());
                            updateTask(receivedResponse.getResponseTask());
                        }
                    } catch (Exception e) {
                        // e.printStackTrace();
                        System.out.println("ERROR");
                    }
                    
                });
                read.start();
                while (true) {
                    if (input.nextInt() == 1) {
                        AddTask();
                        // Sender.writeObject(newRequest);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Server Offline","Connection error", 1);
            System.exit(0);
        }
    }
    // public class TaskCellRenderer extends JCheckBox implements ListCellRenderer<Task>{

    // }
    public class TaskCellRenderer extends JCheckBox implements ListCellRenderer<Task> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(value.getTaskName());
            setSelected(value.isDone());
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return this;
        }
    }
    void AddTask(){
        Task newTask = new Task();
        newTask.TaskName = JOptionPane.showInputDialog("Decribe the Task");
        newTask.NamaMahasiswa = ClientName;
        Request newRequest = new Request();
        newRequest.groupName = this.GroupName;
        newRequest.Requester = ClientName;
        newRequest.task = newTask;
        newRequest.RequestType = "AddTask";
        // Scanner input = new Scanner(System.in);
        try {
            Sender.reset();
            Sender.writeObject(newRequest);
            Sender.flush();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    void MarkDone(){
        // System.out.println(Jtask.getSelectedValue());
        Request newRequest = new Request();
        newRequest.groupName = this.GroupName;
        newRequest.task = Jtask.getSelectedValue();
        newRequest.Requester = ClientName;
        newRequest.RequestType = "MarkDone";
        try {
            Sender.reset();
            Sender.writeObject(newRequest);
            Sender.flush();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    void DeleteTask(){
        Request newRequest = new Request();
        newRequest.groupName = this.GroupName;
        newRequest.task = Jtask.getSelectedValue();
        newRequest.Requester = ClientName;
        newRequest.RequestType = "DeleteTask";
        try {
            Sender.reset();
            Sender.writeObject(newRequest);
            Sender.flush();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    // void updateTasks(List<Task> updatedTasks) {
    //     SwingUtilities.invokeLater(new Runnable() {
    //         @Override
    //         public void run() {
    //             Tasks.clear();
    //             ((Collection<Task>) Tasks).addAll(updatedTasks);
    //             Jtask.repaint();
    
    //             // Tambahkan kode di sini untuk memperbarui panel tugas yang sudah selesai
    //         }
    //     });
    // }
    void updateTask(List<Task> tasks){
        // this.Tasks = new DefaultListModel<Task>();
        // for (Task task : tasks) {
        //     this.Tasks.addElement(task);
        // }
        // this.Jtask = new JList<Task>(this.Tasks);
        // System.out.println(this.Jtask.getModel()); 
        //Update The GUI
        if(!tasks.isEmpty()){
            System.out.println(tasks);
            Tasks.clear();
            Tasks.addAll(tasks);
            // Jtask.updateUI();
            // System.out.println(Jtask.getModel());
        }else{
            Tasks.clear();
        }
        
    }
}