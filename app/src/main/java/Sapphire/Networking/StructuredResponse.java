package Sapphire.Networking;

//#region imports
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.*;
import java.io.*;
import java.net.HttpURLConnection;
import Sapphire.StringReader;
//#endregion imports

public class StructuredResponse {
    public static long fileID = 1;
    public HashMap<String,String> regions;
    public int taskID;
    public boolean isEmpty = false;
    public int status;

    public StructuredResponse(HttpURLConnection res){
        String sResponseBody = "";
        InputStream connectionInput = null;
        File temporaryFile = new File(StringReader.getString("TemporaryFilePath")+ (fileID++) +".tmp");
        try{
            status = res.getResponseCode();
            if(status==200){
                temporaryFile.createNewFile();
                connectionInput = res.getInputStream();
                taskID = Integer.parseInt(res.getHeaderField("taskID"));
                
                BufferedInputStream bufferedInputStream = new BufferedInputStream(connectionInput);
                FileOutputStream temporaryFileOutput = new FileOutputStream(temporaryFile);

                byte[] buffer = new byte[1024];
                int len;
                while((len=bufferedInputStream.read(buffer))>=0){
                    sResponseBody += new String(buffer);
                    temporaryFileOutput.write(buffer, 0, len);
                }
                bufferedInputStream.close();
                temporaryFileOutput.close();
            }else{
                return;
                //System.out.println("Response code: "+status);
            }
        }catch(Exception e){
            System.out.println("Structured Response Error: "+e.getMessage());
            temporaryFile.delete();
            isEmpty = true;
            return;
        }
        if(sResponseBody.equals("")||sResponseBody.equals("None")){
            temporaryFile.delete();
            isEmpty = true;
            return;
        }
        
        //System.out.println(sResponseBody);
        String[] sregions = getRegionNames(sResponseBody);
        if(sregions.length==0){
            temporaryFile.delete();
            isEmpty = true;
            return;
        }
        regions = new HashMap<String,String>();
        //System.out.println(sregions.length);
        for(String regionName : sregions){
            //System.out.println("regionName : "+ regionName);
            // place the regions into the details to be indexed
            if(regionName.equals("File")){
                // files are stored in a temporary file and what's stored is the file location
                regions.put("file_location",(fileID++)+".tmp");
                String tempFileLocation = StringReader.getString("TemporaryFilePath")+ regions.get("file_location");
                //System.out.println("tempFileLocation: "+tempFileLocation);
                File temp_file = new File(tempFileLocation);
                try{
                    temp_file.createNewFile();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(temporaryFile));
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(temp_file));
                    
                    byte[] buffer = new byte[1024];
                    int len;
                    boolean started=false,ended=false;
                    while(((len=bufferedInputStream.read(buffer))>0)&&(!ended)){ //process the bytes in chunks so only the file portion get placed into the file
                        int[] regionbounds = getFileBounds(buffer);
                        if(!started){
                            if(regionbounds[0]!=-1){
                                started = true;
                                if(regionbounds[1]==-1){
                                    regionbounds[1] = len;
                                }
                                bufferedOutputStream.write(buffer,regionbounds[0],regionbounds[1]-regionbounds[0]);
                            }
                        }else{
                            if(regionbounds[1]==-1){
                                regionbounds[1]=len;
                            }else{
                                ended=true;
                            }
                            bufferedOutputStream.write(buffer,0,regionbounds[1]);
                        }
                    }
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    bufferedInputStream.close();
                } //the file will not exist but java will yell at me if the errors aren't handled
                catch(Exception e){
                    System.out.println("Structured Response File Error: "+e.getMessage());
                    temporaryFile.delete();
                }
            }else{
                regions.put(regionName, findRegionBody(sResponseBody,regionName));
            }
        }
        //System.out.println("fully outside");
        if(temporaryFile.exists()){
            temporaryFile.delete();
        }
    }

    //#region helpers


    private String[] getRegionNames(String input){
        //System.out.println(input);
        String[] regionNames = new String[0];
        // placing the body into a usable form based on the regions they're in
        Pattern regionPattern = Pattern.compile("<(\\w*)>");
        Matcher matcher = regionPattern.matcher(input);

        while(matcher.find()){
            String name = input.substring(matcher.start()+1, matcher.end()-1);
            //System.out.println("Region: "+name);
            Pattern closingPattern = Pattern.compile("<\\/"+name+">");
            Matcher secondary = closingPattern.matcher(input);
            if(secondary.find(matcher.end())){
                regionNames = append(regionNames,name);
            }
        }
        
        /* Pattern regionPattern = Pattern.compile("<(.*)>(.|\\n)*<\\/\\1>");
        Matcher matcher = regionPattern.matcher(input);
        
        // find a full region
        while(matcher.find()){
            String region = input.substring(matcher.start(),matcher.end()); // get a string of just that region
            String regionName = region.substring(region.indexOf("<")+1,region.indexOf(">")); 
            //System.out.println("region : "+ region);
            regionNames = append(regionNames,regionName);
        } */
        return regionNames;
    }

    private String[] append(String[] arr,String str){
        String[] returnArr = new String[arr.length+1];
        System.arraycopy(arr, 0, returnArr, 0, arr.length);
        returnArr[returnArr.length-1] = str;
        return returnArr;
    }

    private String findRegionBody(String input, String regionName){
        Pattern startPattern = Pattern.compile("<"+regionName+">");
        Pattern endPattern = Pattern.compile("<\\/"+regionName+">");
        Matcher startMatcher = startPattern.matcher(input);
        Matcher endMatcher = endPattern.matcher(input);
        // find a full region
        if(startMatcher.find()){
            int start = startMatcher.end()+2; // +2 because every region name ends with \r\n
            endMatcher.find();
            int end = endMatcher.start()-2; // -2 because every region name begins with \r\n
            String body = input.substring(start,end);
            //System.out.println(body);
            return body;
        }else{
            return "error";
        }
    }

    private int[] getFileBounds(byte[] input){
        byte[] startRegionNameBytes = ("<File>\r\n").getBytes();
        byte[] endRegionNameBytes = ("\r\n</File>").getBytes();
        int regionNameSize = startRegionNameBytes.length;
        int[] regionBounds = {-1,-1};
        for(int i=0;i<input.length;i++){
            byte[] slice = Arrays.copyOfRange(input, i, (i+regionNameSize));
            byte[] slice2 = Arrays.copyOfRange(input, i, (i+regionNameSize+1));
            if(Arrays.equals(slice, startRegionNameBytes)&&(regionBounds[0]==-1)){
                regionBounds[0] = i+regionNameSize; 
                i+= regionNameSize;
            }else if(Arrays.equals(slice2, endRegionNameBytes)&&(regionBounds[1]==-1)){
                //System.out.println(new String(slice2));
                regionBounds[1] = i-1;
                //System.out.println(new String(input,0,i-1));
                break;
            }
        }
        return regionBounds;
    }
    

    //#endregion helpers
}
