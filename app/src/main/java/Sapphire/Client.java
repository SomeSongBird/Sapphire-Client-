package Sapphire;

//#region imports
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Scanner;
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
    HashMap<Integer,String> directories;  //key = other device ID, value = dir_structure file

    public Client(Sapphire.StringReader sr){
        // read authToken
        serverURL = sr.getString("serverIP");
        authorizedDirectory = sr.getString("DefaultAuthorizedDirectory");
        externalDirectoryFilesPath = sr.getString("ExternalDirectoryFilesPath");
        temporaryFilePath = sr.getString("TemporaryFilePath");
        startableApps = new HashMap<String,String>();
        otherClients = new HashMap<Integer,String>();
        directories = readDirs();
        
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
    public HashMap<Integer,String> getOtherDirs(){
        return directories;
    }
    private HashMap<Integer,String> readDirs(){
        HashMap<Integer,String> forReturn = new HashMap<Integer,String>();
        File dir = new File(externalDirectoryFilesPath);
        if(dir.exists()&&dir.isDirectory()){
            for(File file : dir.listFiles()){
                String[] name_extention = file.getName().split(".");
                if(name_extention.length>1){
                    if(name_extention[1].equals("dir")){
                        try{
                            int id = Integer.parseInt(name_extention[0]);
                            forReturn.put(id,externalDirectoryFilesPath + file.getName());
                        }catch(NumberFormatException nfe){}
                    }
                }
            }
        }else{
            System.out.println("problem with extern_Dirs folder");
        }
        return forReturn;
    }
    //#endregion init

    //#region helpers
    class RequestBuilder{
        String temporaryFileString;
        RequestBuilder(int temporaryFileID){
            temporaryFileString = temporaryFilePath+"requestBuilder"+temporaryFileID+".tmp";
            //System.out.println(temporaryFilePath+"requestBuilder"+temporaryFileID+".tmp");
            try{
                new File(temporaryFileString).createNewFile();
            }catch(Exception e){
                System.err.println("Could not create temporary file for new request");
            }
        }

        public void addRegion(String regionName,String regionBody){
            String newRegion = "<"+regionName+">"+regionBody+"</"+regionName+">\r\n";
            byte[] newRegionBytes = newRegion.getBytes();
            try(BufferedOutputStream requestBody = new BufferedOutputStream(new FileOutputStream(new File(temporaryFileString),true));
            ){
                requestBody.write(newRegionBytes, 0, newRegionBytes.length);
                requestBody.flush();
                requestBody.close();
            }catch(Exception e){
                System.err.println("Buffered stream failure: "+e.getMessage());
                return;
            }
        }

        public void addfile(String pathToFile){
            byte[] startRegion = "<File>".getBytes();
            byte[] endRegion = "</File>".getBytes();
            File inputFile = null; 
            try(BufferedOutputStream requestBody = new BufferedOutputStream(new FileOutputStream(new File(temporaryFileString),true));
            ){
                inputFile = new File(zipfile(pathToFile));
                requestBody.write(startRegion,0,startRegion.length);
                requestBody.flush();
                Files.copy(inputFile.toPath(),requestBody);
                requestBody.flush();
                requestBody.write(endRegion,0,endRegion.length);
                requestBody.flush();
            }catch(Exception e){
                System.err.println("Buffered input stream failure");
            }
            if(inputFile!=null) inputFile.delete();
        }

        public BufferedInputStream getInputStream(){
            BufferedInputStream bis=null;
            try{
                bis = new BufferedInputStream(new FileInputStream(new File(temporaryFileString)));
            }catch(Exception e){
                System.err.println("Problem converting temporary file to input stream");
            }
            return bis; 
        }
        
        public void closeRequest(){
            File temporaryFile = new File(temporaryFileString);
            if(temporaryFile.exists()){
                if(temporaryFile.delete()){
                    //System.out.println("temporary file deleted");
                }else{
                    System.out.println("Not deleted");
                }
            }else{
                System.out.println("temporaryFile not found");
            }
        }
    }

    public StructuredResponse sendRequest(String url,int taskID, int targetID, RequestBuilder requestBuilder){
        HttpURLConnection connection;
        StructuredResponse sRes = null;
        BufferedInputStream requestBody = null;
        if(requestBuilder!=null){
            requestBody = requestBuilder.getInputStream();
        }
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
                    // System.out.write(buffer,0, len); // to read the buffer to the CL
                    bos.write(buffer,0,len);
                    bos.flush();
                }
                bos.close();
                requestBody.close();
            }
            sRes = new StructuredResponse((connection));
            connection.disconnect();
        }catch(Exception e){
            System.out.println(e.getMessage());
            //log
        }
        finally{
            if(requestBuilder!=null) requestBuilder.closeRequest();
        }
        return sRes;
    }
    
    public MockDir readExternDir(int id){
        try {
            File f = new File(directories.get(id));
            if(f.exists()){
                String[] sDirStruct = new String[0];
                Scanner scan = new Scanner(new FileInputStream(f));
                while(scan.hasNext()){
                    append(sDirStruct,scan.nextLine());
                }
                if(sDirStruct.length>0){
                    return new MockDir(sDirStruct);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading external directory file");
        } 
        return null;
    }
    private String[] append(String[] mdl, String md){
        String [] forReturn = new String[mdl.length+1];
        System.arraycopy(mdl, 0, forReturn, 0, mdl.length);
        forReturn[mdl.length] = md;
        return forReturn;
    }
    //#endregion helpers

    //#region fileZippers

    private String zipfile(String filename){
        File infile = new File(authorizedDirectory+filename);
        String outputFileName = temporaryFilePath+filename+".zip";
        File outfile = new File(outputFileName);
        //System.out.println("tozip: "+authorizedDirectory+filename);
        //System.out.println("zip: "+outputFileName);
        
        try{
            outfile.createNewFile();
            FileInputStream fis = new FileInputStream(infile);
            ZipOutputStream zOut = new ZipOutputStream(new FileOutputStream(outfile));
            
            ZipEntry zipEntry = new ZipEntry("content");
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
            System.out.println("Error zipping file: "+e);
            outfile.delete();
            return null;
        }
        
        return outputFileName;
    }

    private void unzipFile(String path,String tmpFileName){
        File input = new File(temporaryFilePath+tmpFileName);
        if(path.charAt(0)=='/'||path.charAt(0)=='\\'){
            path=path.substring(1,path.length());
        }
        File outfile = new File(authorizedDirectory+path);
        
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
        if(sRes.status!=200){
            connected = false;
            System.out.println(sRes.status);
            return;
        }
        connected = true;
        if(sRes.isEmpty){return;}
        String taskName = sRes.regions.get("Task");
        String regionBody = null;
        
        switch(taskName){
            case "FileTransfer":
                if((regionBody = sRes.regions.get("confirmation"))!=null){
                    System.out.println("file recieved");
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
                    sendRequest(serverURL+"/file_transfer/compliance", sRes.taskID, -1, rb);
                }else if((regionBody = sRes.regions.get("requested_file_path"))!=null){
                    //zip file at location and send to server
                    if(regionBody.charAt(0)=='/'||regionBody.charAt(0)=='\\'){
                        regionBody=regionBody.substring(1,regionBody.length());
                    }
                    if((new File(authorizedDirectory+regionBody).exists())){
                        RequestBuilder rb = new RequestBuilder(temporaryFileID++);
                        try{
                            rb.addfile(regionBody);
                        }catch(Exception e){
                            //log
                            return;
                        }

                        // send zip file
                        sendRequest(serverURL+"/file_transfer/compliance", sRes.taskID, -1, rb);
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
                    sendRequest("/update_directory/compliance", sRes.taskID, -1, rb);
                }else if((regionBody = sRes.regions.get("directory_details"))!=null){
                    // store the directory details with the ID and name of the device they're from 
                    int target_client = Integer.parseInt(sRes.regions.get("target_client"));
                    String fName = externalDirectoryFilesPath+target_client+".dir";
                    File f = new File(fName);
                    try{
                        if(!f.exists()){
                            f.createNewFile();
                        }
                        String dir_structure = null;
                        if((dir_structure = sRes.regions.get("directory_details"))!=null){
                            System.out.println(dir_structure);
                            Files.writeString(f.toPath(),dir_structure);
                        }
                    }catch(Exception e){
                        
                        return;
                    }
                    directories.put(target_client, fName);
                }else{
                    System.out.println("Directory request error on update");
                }
            break;
            default:
            return;
        }
    }
    
    public void getClientList(){
        StructuredResponse response = sendRequest("/client_list", -1, -1, null);
        if(response.isEmpty){
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
        sendRequest("/file_transfer/request",-1 ,destinationID, rb);
    }
    
    public void pullFile(int targetID, String filePath, String finalPath){
        RequestBuilder rb = new RequestBuilder(temporaryFileID++);
        rb.addRegion("file_location", filePath);
        rb.addRegion("file_path", finalPath);
        sendRequest("/file_transfer/request",-1 ,targetID, rb);
    }

    public void updateExternDirs(int targetID){
        RequestBuilder rb = new RequestBuilder(temporaryFileID++);
        sendRequest("/update_directory/request", -1, targetID, rb);
    }

    public void startApp(int targetID, String appName){
        RequestBuilder rb = new RequestBuilder(temporaryFileID++);
        rb.addRegion("app_name", appName);
        sendRequest("/file_transfer/request",-1 ,targetID, rb);
    }

    //#endregion taskManagement
 
}
