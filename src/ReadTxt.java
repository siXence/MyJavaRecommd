import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;


public class ReadTxt {
	
	public static void readTxtFile(String filePath, String writePath){
    	System.out.println("read test data..................");
        try {
        	
        	 File f = new File(writePath);
 		    if (!f.exists()) {
 		    	f.createNewFile();
 		    }
 			    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
 			    BufferedWriter writer = new BufferedWriter(write);
        	
                String encoding="GBK";
                File file=new File(filePath);
                int lastUserID = -1;
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
//                        System.out.println(lineTxt);
                        String[] tmp = lineTxt.split("\t");
//                        System.out.println("tmp = " + tmp[2]);
//                        String output = tmp[0] + "\t" + tmp[1] + "\t"  + tmp[1] + "\t" + tmp[2] + "\r\n";
                        String output = tmp[0] + "\t" + tmp[1] + "\t" + tmp[2] + "\n" ;
                        writer.write(output);
                    }
//                    System.out.println("watchlists = " + watchLists.get(1)  + ",  aaa = " + watchLists.get(462));
                    read.close();
                    writer.close();
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
     
    }
    
    public static void main(String[] args) throws Exception {
    	String filePath = "u1.test";
    	String output = "u1out.test";
    	readTxtFile(filePath, output);
    	filePath = "u1.base";
    	output = "u1out.base";
    	readTxtFile(filePath, output);
    	System.out.println("Done");
    }

}
