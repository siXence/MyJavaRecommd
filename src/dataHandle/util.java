package dataHandle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class util {
	
	/**
	 * Get train or test data
	 * @param filePath
	 * @return
	 */
	public HashMap<Integer, HashMap<Integer, Double> >  getData(String filePath) {
		HashMap<Integer, HashMap<Integer, Double> > dataSet = new HashMap<Integer, HashMap<Integer, Double> >();
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    String[] tmp = lineTxt.split("\t");
                    int uid = Integer.parseInt(tmp[0]);
                    int iid = Integer.parseInt(tmp[1]);
                    double rating = Double.valueOf(tmp[2]);
                    if (!dataSet.containsKey(uid)) {
                    	dataSet.put(uid, new HashMap<Integer, Double>());
                    } 
                    dataSet.get(uid).put(iid, rating);
                    
                }
                read.close();
	    }else{
	        System.out.println("找不到指定的文件");
	    }
    } catch (Exception e) {
        System.out.println("读取文件内容出错");
        e.printStackTrace();
    }
		
		
		return dataSet;
	}
}
