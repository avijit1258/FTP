package ftp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class FTPServer {

	JTextArea clientsMessage;
	JTextArea userList;
	ArrayList clientOutputStreams;
	JButton sendButton;
	JButton chooseFile;
	JTextField smsToAll;
	JFrame window = new JFrame("SERVER");
	JFileChooser jFileChooser = new JFileChooser();
	ArrayList<String> uname = new ArrayList<String>();
	ArrayList<Socket> port = new ArrayList<Socket>();
	
	public class ClientHandler implements Runnable{
		
		BufferedReader reader;
		Socket sock;
		//JTextArea clientsMessage; get nullPointerException for this line wasted one day
		
		public ClientHandler(Socket clientSocket) {
			
			try {
				sock = clientSocket;
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
			    reader = new BufferedReader(isReader);
			    
			    
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		
		public void run() {
			String message;
			String[] parts = null;
			boolean once = false;
			
			
			try {
					while((message = reader.readLine()) != null) {
						if(!once)
						{
							//System.out.println(sock.getPort());
							port.add(sock);
							parts = message.split("\\:");
							//System.out.println(parts[0]);
							uname.add(parts[0]);
							once = true;
						}
						//System.out.println("read" + message);
						clientsMessage.append(message+"\n");
						tellEveryone(message);
						showingClients();
					}
					uname.remove(parts[0]);
					port.remove(sock);
					showingClients();
					tellEveryone(parts[0].toUpperCase()+ "("+sock+")"+"has left the conversation\n");
					clientsMessage.append(parts[0].toUpperCase()+ "("+sock+")"+"has left the conversation\n");
				}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
		}
	}	
	
	public  class SendButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			clientsMessage.append("SERVER : " + smsToAll.getText()+"\n");
			tellEveryone("SERVER : " + smsToAll.getText());
			
			smsToAll.setText("");
			smsToAll.requestFocus();
			
		}
		
	}
	
	public  class ChooseButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			int result = jFileChooser.showOpenDialog(window);
			  
			  if (result == JFileChooser.APPROVE_OPTION) {
				  
	  	            File selectedFile = jFileChooser.getSelectedFile();
	  	            clientsMessage.append(" Sending file: " + selectedFile.getName());
	  	            tellEveryone("Sending file: " + selectedFile.getName());
	  	            tellEveryone("READY:"+selectedFile.getName());
	  	            try {
						sendFileToEveryone(selectedFile.getAbsolutePath());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	  	            tellEveryone("DONE !!! File "+selectedFile.getName() + "sent . ");
	  	            clientsMessage.append("DONE !!! File "+selectedFile.getName() + "sent . ");
	  	            //System.out.println("Selected file: " + selectedFile.getAbsolutePath());
	  	            //System.out.println("Selected file: " + selectedFile.getName());
	  
	           }
			 
		}
		
	}
	
	
	public void showingClients()
	{
		String st = "000..,";
		String t = "";
		for(String s : uname)
		{
			st += s+",";
			t += s+"\n";
		}
		
		System.out.println(st);
		
		Iterator it = clientOutputStreams.iterator();
		userList.setText(t);
		while(it.hasNext()) {
			try {
				PrintWriter writer = (PrintWriter) it.next();
				writer.println(st);
				writer.flush();
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
			
	}
	
		public void go() {
			clientOutputStreams = new ArrayList();
			
			try {
				ServerSocket serverSock = new ServerSocket(6666);
				
				while(true) {
					Socket clientSocket = serverSock.accept();
					//System.out.println(clientSocket.getPort());
					PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
					clientOutputStreams.add(writer);
					
					
					Thread t = new Thread(new ClientHandler(clientSocket));
					t.start();
					System.out.println("got a connection from "+clientSocket);
					//showingClients();
				}
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			
		}
		
		
		
		public  void tellEveryone(String message) {
			Iterator it = clientOutputStreams.iterator();
			while(it.hasNext()) {
				try {
					PrintWriter writer = (PrintWriter) it.next();
					writer.println(message);
					writer.flush();
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		public  void sendFileToEveryone(String filePath) throws IOException {
			
			
			File f = new File(filePath);
			
			FileInputStream fis = new FileInputStream(f);
			
			int ch =  0 ;
			
			do {
				ch = fis.read();
				Iterator it = clientOutputStreams.iterator();
				while(it.hasNext()) {
					try {
						
						//System.out.println(ch+".........");
						PrintWriter writer = (PrintWriter) it.next();
						writer.println(String.valueOf(ch));
						writer.flush();
					}catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}while(ch != -1);
			
			fis.close();
			
			
		}
		
		
		public  void addAction()
		{
			  
		      JPanel content = new JPanel();
		      
		      clientsMessage = new JTextArea(15,50);
		      clientsMessage.setLineWrap(true);
		      clientsMessage.setWrapStyleWord(true);
		      clientsMessage.setEditable(false);
		      JScrollPane qScroller = new JScrollPane(clientsMessage);
			  qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			  qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			  
			    userList = new JTextArea(15, 10);
				userList.setLineWrap(true);
				userList.setWrapStyleWord(true);
				userList.setEditable(false);
				JScrollPane uScroller = new JScrollPane(userList);
				uScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				
			  
			  smsToAll = new JTextField(20);
			  sendButton = new JButton("Send To All");
			  chooseFile = new JButton("Open File To Send");
			  sendButton.addActionListener(new SendButtonListener());
			  chooseFile.addActionListener(new ChooseButtonListener());
			  content.add(qScroller);
			  content.add(uScroller);
			  content.add(smsToAll);
			  content.add(sendButton);
			  content.add(chooseFile);
			  window.getContentPane().add(BorderLayout.CENTER, content);
			  window.setSize(800, 400);
			  window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			  window.setVisible(true);
			  clientsMessage.setText("");
			  
			  
		}
		
		public static void main(String[] args) {
			
			  FTPServer cs = new FTPServer();
			  cs.addAction();
			  cs.go();
			  
			  
			  
		}
	
	
	
}
