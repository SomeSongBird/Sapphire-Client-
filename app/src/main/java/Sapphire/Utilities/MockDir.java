package Sapphire.Utilities;

//import java.util.Scanner;

public class MockDir {
    public MockDir parent;
    public MockDir[] nextLayer;
    public String name;
    public String fullName;

    public MockDir(String[] dirList){ //first call
        MockDir[] allDirs = new MockDir[0];
        nextLayer = new MockDir[0];
        fullName = dirList[0];
        parent = this;
        String[] split = fullName.split("[\\\\/]");
        name = split[split.length-1];

        for(int i=1;i<dirList.length;i++){
            String[] secondSplit = dirList[i].split("[\\\\/]");
            allDirs = append(allDirs, new MockDir(dirList[i],secondSplit[secondSplit.length-1]));
            //System.out.println(allDirs.length);
        }
        MockDir previous = this;
        for(MockDir md : allDirs){
            while(true){
                if(md.fullName.contains(previous.fullName)){
                    //System.out.println("current: "+md.fullName+" | previous: "+previous.fullName);
                    md.parent = previous;
                    previous.nextLayer = append(previous.nextLayer, md);
                    previous = md;
                    break;
                }else{
                    if(previous==this){
                        System.out.println("early end");
                        return;
                    } 
                    previous = previous.parent;       
                }
            }
        }
    }

    private MockDir(String path, String name){ //interior calls
        fullName = path;
        this.name = name;
        nextLayer = new MockDir[0];
        parent = null;
    }
    
    private MockDir[] append(MockDir[] mdl, MockDir md){
        MockDir [] forReturn = new MockDir[mdl.length+1];
        System.arraycopy(mdl, 0, forReturn, 0, mdl.length);
        forReturn[mdl.length] = md;
        return forReturn;
    }

    public String getPath(){
        String[] split = fullName.split("[\\\\/]");
        String forRet = split[1];
        for(int i=2;i<split.length;i++){
            forRet+="/"+split[i];
        }
        return forRet;
    }

    public String toString(){
        String output = "";
        output+=fullName+"\n";
        for(MockDir next : nextLayer){
            output += "\t"+next.name+" | "+((next.isDirectory())?"dir":"file")+"|"+next.nextLayer.length+"\n";
        }
        return output;
    } 

    // getNextDir is broken. idk why but the next dir's subDirectories doesn't exist.  implement by accessing the nextLayer directly
    /* public MockDir getNextDir(String nextName){
        for(MockDir next : nextLayer){
            System.out.println(next.nextLayer.length);
            if(next.name.equals(nextName)){
                return next;
            }
        }
        return this;
    } */
    
    public boolean isDirectory(){
        if(nextLayer.length>0){ return true; }
        return false;
    }

}

