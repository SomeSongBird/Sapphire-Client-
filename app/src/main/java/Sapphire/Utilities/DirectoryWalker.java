package Sapphire.Utilities;

import java.io.File;

public class DirectoryWalker{
    public String[] fileStructure;
    public DirectoryWalker(String dir_path){
        File file = new File(dir_path);
        fileStructure = new String[0];
        if(file.isDirectory()){
            fileStructure = concat(fileStructure, walk(file.getName(),file.listFiles()));
        }
    }

    private String[] walk(String root_path,File[] dirs){
        String[] file_names = new String[dirs.length+1];
        file_names[0] = root_path;
        int index = 1;
        for(File fi : dirs){
            file_names[index++] = root_path+"/"+fi.getName();
            //System.out.println(root_path+"/"+fi.getName());
            if(fi.isDirectory()){
                file_names = concat(file_names, walk(root_path+"/"+fi.getName(),fi.listFiles()));
            }
        }
        return file_names;
    }

    private String[] concat(String[] s1,String[] s2){
        String[] forRet = new String[s1.length+s2.length];
        int i=0;
        for(;i<s1.length;i++){
            forRet[i] = s1[i];
        }
        for(;i<s1.length+s2.length;i++){
            forRet[i] = s2[i-s1.length];
        }
        return forRet;
    }

    public static void main(String[] args) {
        String dir = "/home/usr";
        DirectoryWalker dw = new DirectoryWalker(dir);

        for(String file :dw.fileStructure){
            System.out.println(file);
        }

    }
}