package Sapphire;

//#region imports
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.zip.*;

import Sapphire.Utilities.*;
import Sapphire.Networking.StructuredResponse;
//#endregion imports

public class Client{
    //#region init
    public boolean connected = false;
    String authToken;
    String authorizedDirectory;
    int temporaryFileID = 0;
    String temporaryFilePath;
    String externalDirectoryFilesPath;
    boolean shutdown = false;
    String serverURL;

    HashMap<String,String> startableApps; //key = application name, value = startup script
    HashMap<Integer,String> otherClients; //key = other device ID, value = other deivce name
    HashMap<Integer,String> directories;  //key = other device ID, value = dir_structure location

    public Client(Sapphire.StringReader sr){
        // read authToken
        serverURL = sr.getString("serverIP");
        authorizedDirectory = sr.getString("DefaultAuthorizedDirectory");
        externalDirectoryFilesPath = sr.getString("ExternalDirectoryFilesPath");
        temporaryFilePath = sr.getString("TemporaryFilePath");
        startableApps = new HashMap<String,String>();
        otherClients = new HashMap<Integer,String>();
        directories = new HashMap<Integer,String>();
        
        authToken = sr.getString("ClientAuthToken");
        if(authToken.equals("")){
            System.err.println("Authorization token not set, create a new token in configuration.");
        }  
    }

    public void setAuthToken(String newToken){
        authToken = newToken;
        Sapphire.StringReader.writeInput("ClientAuthToken", newToken);
    }
    public String getAuthToken(){
        return authToken;
    }
    public void setDirPermissions(String newDir){
        authorizedDirectory = newDir;
        Sapphire.StringReader.writeInput("DefaultAuthorizedDirectory", newDir);
    }
    public String getDirPermissions(){
        return authorizedDirectory;
    }

    public HashMap<Integer,String> getOtherClients(){
        return otherClients;
    }
    //#endregion init

    //#region helpers
    class RequestBuilder{
        File temporaryFile;
        BufferedOutputStream requestBody;
        RequestBuilder(int temporaryFileID){
            temporaryFile = new File(temporaryFilePath+"requestBuilder"+temporaryFileID+".tmp");
            try{
                temporaryFile.createNewFile();
                requestBody = new BufferedOutputStream(new FileOutputStream(temporaryFile));
            }catch(Exception e){
                System.err.println("Could not create temporary file for new request");
            }
        }

        public void addRegion(String regionName,String regionBody){
            String newRegion = "<"+regionName+">"+regionBody+"</"+regionName+">\r\n";
            BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(newRegion.getBytes()));
            
            byte[] buffer = new byte[1024];
            int len;
            try{
                while((len=bis.read())>0){
                    requestBody.write(buffer,0,len);
                }
                bis.close();
            }catch(Exception e){
                System.err.println("Buffered input stream failure");
            }
        }

        public void addfile(String pathToFile){
            byte[] startRegion = "<File>".getBytes();
            byte[] endRegion = "</File>\r\n".getBytes();
            try{
                File inputFile = new File(zipfile(pathToFile));

                requestBody.write(startRegion,0,startRegion.length);
                Files.copy(inputFile.toPath(),requestBody);
                requestBody.write(endRegion,0,endRegion.length);
                /* 
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
                
                byte[] buffer = new byte[1024];
                int len;
                while((len=bis.read())>0){
                    requestBody.write(buffer,0,len);
                }
                bis.close(); */
            }catch(Exception e){
                System.err.println("Buffered input stream failure");
            }
        }

        public BufferedInputStream build(){
            BufferedInputStream bis=null;
            try{
                requestBody.close();
                bis = new BufferedInputStream(new FileInputStream(temporaryFile));
            }catch(Exception e){
                System.err.println("Problem converting temporary file to input stream");
            }
            return bis; 
        }
        public void closeRequest(){
            if(temporaryFile!=null){
                temporaryFile.delete();
            }
        }
    }

    public StructuredResponse sendRequest(String url,int taskID, int targetID, BufferedInputStream requestBody){
        HttpURLConnection connection;
        StructuredResponse sRes = null; 
        try{
            connection = (HttpURLConnection) new URL(serverURL+url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("authToken", authToken);
            connection.setRequestProperty("taskID", taskID+"");
            connection.setRequestProperty("targetID", targetID+"");
            if(requestBody!=null){
                OutputStream output = connection.getOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(output);

                byte[] buffer = new byte[1024];
                int len;
                while((len=requestBody.read(buffer))>0){
                    bos.write(buffer,0,len);
                }
            }
            sRes = new StructuredResponse((connection));
            connection.disconnect();
        }catch(Exception e){
            System.out.println(e.getMessage());
            //log
        }
        return sRes;
    }
    //#endregion helpers

    //#region fileZippers

    private String zipfile(String filename){
        File infile = new File(filename);
        String outputFileName = temporaryFilePath+filename+".zip";
        File outfile = new File(outputFileName);
        try{
            outfile.createNewFile();
            FileInputStream fis = new FileInputStream(infile);
            ZipOutputStream zOut = new ZipOutputStream(new FileOutputStream(outfile));
            
            ZipEntry zipEntry = new ZipEntry("entry");
            zOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zOut.write(bytes, 0, length);
            }
            zOut.flush();
            zOut.closeEntry();
            fis.close();
            zOut.close();
        }catch(IOException e){
            //log error
            return null;
        }
        
        return outputFileName;
    }

    private void unzipFile(String path,String tmpFileName){
        File input = new File(temporaryFilePath+tmpFileName);
        File outfile = new File(path);
        
        try{
            ZipInputStream zis = new ZipInputStream(new FileInputStream(input));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outfile));
            
            ZipEntry zent = zis.getNextEntry();
            
            while(zent!=null){
                byte[] buffer = new byte[1024];
                int len;
                while((len=zis.read(buffer))>0){
                    bos.write(buffer,0,len);
                }
                zent = zis.getNextEntry();
            }
            bos.flush();
            bos.close();
            zis.close();

            input.delete();
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
    }
    //#endregion fileZippers

    //#region taskManagement
    
    public void update(){
        StructuredResponse sRes = sendRequest("/update", -1,-1, null); //writes file(if any) to local storage and returns path to temporary file under file_location
        if(sRes==null){
            connected = false;
            return;
        }
        connected = true;
        if(sRes.isEmpty){return;}
        String taskName = sRes.regions.get("Task");
        String regionBody = null;
        switch(taskName){
            case "FileTransfer":
                if((regionBody = sRes.regions.get("confirmation"))!=null){
                    // log confirmation
                }else if((regionBody = sRes.regions.get("final_path"))!=null){
                    // read file and place in final path location
                    String temporaryFile=null;
                    if((temporaryFile=sRes.regions.get("file_location"))==null){
                        //log failure
                        return;
                    }
                    unzipFile(regionBody,temporaryFile);
                    RequestBuilder rb = new RequestBuilder(temporaryFileID++);
                    rb.addRegion("confirmation",regionBody);
                    //send confirmation;
                    sendRequest(serverURL+"/file_transfer/compliance", sRes.taskID, -1, rb.build());
                    rb.closeRequest();
                }else if((regionBody = sRes.regions.get("requested_file_path"))!=null){
                    //zip file at location and send to server
                    if((new File(regionBody).exists())){
                        RequestBuilder rb = new RequestBuilder(temporaryFileID++);
                        try{
                            rb.addfile(regionBody);
                        }catch(Exception e){
                            //log
                            return;
                        }

                        // send zip file
                        sendRequest(serverURL+"/file_transfer/compliance", sRes.taskID, -1, rb.build());
                        rb.closeRequest();
                        return;
                    }else{
                        // log error
                        return;
                    }
                }else{
                    //log error
                }
            break;
            case "RemoteStart":
                if((regionBody = sRes.regions.get("app_name"))!=null){

                }
            break;
            case "Directory":
                if((regionBody = sRes.regions.get("directory_request"))!=null){
                    DirectoryWalker dw = new DirectoryWalker(authorizedDirectory);
                    RequestBuilder rb = new RequestBuilder(temporaryFileID++);
                    String fullDir = "";
                    for(String dir:dw.fileStructure){
                        fullDir+=dir+"\n";
                    }
                    rb.addRegion("directory_details",fullDir);
                    sendRequest(serverURL+"/update_directory/compliance", sRes.taskID, -1, rb.build());
                    rb.closeRequest();
                }else if((regionBody = sRes.regions.get("directory_details"))!=null){
                    // store the directory details with the ID and name of the device they're from 
                    int target_client = Integer.parseInt(sRes.regions.get("target_client"));
                    String fName = externalDirectoryFilesPath+"client_"+target_client+".txt";
                    File f = new File(fName);
                    try{
                        f.createNewFile();
                    }catch(Exception e){
                        // log
                    }
                    try (PrintWriter out = new PrintWriter(fName)) {
                        out.println(regionBody);
                    }catch(Exception e){

                    }
                    directories.put(target_client, f.getName());
                }else{
                    //log error
                }
            break;
            default:
            return;
        }
    }
    
    public void getClientList(){
        StructuredResponse response = sendRequest("/client_list", -1, -1, null);
        if(response.isEmpty){
            System.out.println("Empty client list");
            return;
        }

        String clientList = response.regions.get("ClientList");
        String[] clients = clientList.split(":");
        for(String client : clients){
            String[] clientSplit = client.split(",");
            otherClients.put(Integer.parseInt(clientSplit[0]),clientSplit[1]);
        }
    }

    public void sendFile(int destinationID, String destinationPath, String pathToFile){
        RequestBuilder rb = new RequestBuilder(temporaryFileID++);
        rb.addRegion("final_path", destinationPath);
        rb.addfile(pathToFile);
        sendRequest("/file_transfer/request",-1 ,destinationID, rb.build());
        rb.closeRequest();
    }
    
    public void pullFile(int targetID, String filePath, String finalPath){
        RequestBuilder rb = new RequestBuilder(temporaryFileID++);
        rb.addRegion("file_location", filePath);
        rb.addRegion("file_path", finalPath);
        sendRequest("/file_transfer/request",-1 ,targetID, rb.build());
        rb.closeRequest();
    }

    public void startApp(int targetID, String appName){
        RequestBuilder rb = new RequestBuilder(temporaryFileID++);
        rb.addRegion("app_name", appName);
        sendRequest("/file_transfer/request",-1 ,targetID, rb.build());
        rb.closeRequest();
    }

    //#endregion taskManagement
 
}
