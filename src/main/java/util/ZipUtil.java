package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Shadow on 2017/8/15.
 */
public class ZipUtil {
    public static List<File> unZip(String zipPath, String targetPath){
        try {
            List<File> fileList=new ArrayList<>();
            long startTime=System.currentTimeMillis();
            ZipInputStream Zin=new ZipInputStream(new FileInputStream(zipPath));//输入源zip路径
            BufferedInputStream Bin=new BufferedInputStream(Zin);
            String Parent=targetPath; //输出路径（文件夹目录）
            File Fout=null;
            ZipEntry entry;
            while((entry = Zin.getNextEntry())!=null && !entry.isDirectory()){
                Fout=new File(Parent,entry.getName());
                if(!Fout.exists()){
                    (new File(Fout.getParent())).mkdirs();
                }
                FileOutputStream out=new FileOutputStream(Fout);
                BufferedOutputStream Bout=new BufferedOutputStream(out);
                int b;
                while((b=Bin.read())!=-1){
                    Bout.write(b);
                }
                Bout.close();
                out.close();
                fileList.add(Fout);
                System.out.println(Fout+"解压成功");
            }
            Bin.close();
            Zin.close();
            long endTime=System.currentTimeMillis();
            System.out.println("耗费时间： "+(endTime-startTime)+" ms");
            return fileList;
        }catch (Exception e){
            return new ArrayList<>();
        }
    }
}
