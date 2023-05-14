package Sapphire.Utilities;

import java.util.Arrays;

public class MockDir {
    public MockDir parent;
    public MockDir[] nextLayer;
    private String name;
    public String fullName;

    public MockDir(String[] dirList, MockDir previous){
        parent = previous;
        fullName = dirList[0];
        String[] brokenUp;
        if(fullName.contains("/")){
            brokenUp = fullName.split("/");
        }else{
            brokenUp = fullName.split("\\");
        }
        name = brokenUp[brokenUp.length-1];
        nextLayer = new MockDir[0];
        int index = 0;
        for(String s : dirList){
            if(!s.contains(fullName)){
                return;
            }
            nextLayer = append(nextLayer, new MockDir(Arrays.copyOfRange(dirList, index++, dirList.length),this));
            index++;
        }
    }
    private MockDir[] append(MockDir[] mdl, MockDir md){
        MockDir [] forReturn = new MockDir[mdl.length+1];
        System.arraycopy(mdl, 0, forReturn, 0, mdl.length);
        forReturn[mdl.length] = md;
        return forReturn;
    }
    
    public String toString(){
        String output = "";
        output+=fullName+"\n";
        for(MockDir next : nextLayer){
            output = "\t"+next.name+"\n";
        }
        return output;
    } 

    public MockDir getNextDir(String nextName){
        for(MockDir next : nextLayer){
            if(next.name.equals(nextName)){
                return next;
            }
        }
        return this;
    }
}
