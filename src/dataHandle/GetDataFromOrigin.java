/**
 * 将源数据转为MovieLens格式的数据
 * 剔除观看节目少于30个的用户
 * 剔除被观看人数少于10个的节目
 */
package dataHandle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author xv
 *
 */
public class GetDataFromOrigin {
	protected final int numLimit = 10;
	ArrayList<String> data = new ArrayList<String>();
	HashMap<String, String> proLength = new HashMap<String, String>();
	HashMap<String, Integer> itemCnt = new HashMap<String, Integer>();
	private final int itemLimit = 30;
	private final int fileNum = 3000;
	private final int fileStep = 5;
	
	public void readOriginFile(String filePath) {
//		System.out.println("filePath = " + filePath);
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
//                	System.out.println("********************************************************");
//                	System.out.println(lineTxt);
                    String[] tmp = lineTxt.split("\\|");
//                    for (int i = 0; i < tmp.length; i++) {
//                    	System.out.println((i+1) + " : " + tmp[i] + " , " + tmp[i].isEmpty());
//                    }
                    if (tmp.length > 7) {
                    	if (!tmp[0].isEmpty() && proLength.containsKey(tmp[7])) {
                    		double t1 = Double.valueOf(tmp[5]);
                    		double t2 = Double.valueOf(proLength.get(tmp[7]));
                    		double t = t1/t2;
                    		if (!Double.isNaN(t)) {
                    			String elem = tmp[0] + "\t" + tmp[7] + "\t" + String.valueOf(t);
                        		data.add(elem);
                    		}
                    	}
                    }
                }
//                System.out.println("watchlists = " + watchLists.get(1)  + ",  aaa = " + watchLists.get(462));
                read.close();
	    }else{
	        System.out.println("找不到指定的文件");
	    }
	    } catch (Exception e) {
	        System.out.println("读取文件内容出错");
	        e.printStackTrace();
	    }
	}
	
	public void write() {
		String filePath  = "/home/xv/DataForRecom/saveData/allData.txt";
//		ArrayList<String> ttt = data;
//		ttt.clear();
//		System.out.println("after clear data = " + data);
		try {
		    File f = new File(filePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		    BufferedWriter writer = new BufferedWriter(write);
		    for (String elem:data) {
		    	writer.write(elem+"\n");
		    }
		    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	public void getProLength(String filePath) {
//		System.out.println("filePath = " + filePath);
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
//                	System.out.println("********************************************************");
//                	System.out.println(lineTxt);
                    String[] tmp = lineTxt.split("\t");
//                    for (int i = 0; i < tmp.length; i++) {
//                    	System.out.println((i+1) + " : " + tmp[i] + " , " + tmp[i].isEmpty());
//                    }
                    if (Integer.parseInt(tmp[2]) >= 100) {
                    	proLength.put(tmp[0], tmp[3]);
                    } 
                }
                read.close();
	    }else{
	        System.out.println("找不到指定的文件");
	    }
	    } catch (Exception e) {
	        System.out.println("读取文件内容出错");
	        e.printStackTrace();
	    }
	}
	
	public void getAllFileNames(String path) {
		File file = new File(path);
		File[] fileNames = file.listFiles();
		System.out.println("the number of files is " + fileNames.length);
//		for (int i = 0; i < fileNames.length; i++) {
//			System.out.println(fileNames[i]);
//		}
//		for (int i = 0; i < fileNames.length; i++) {
		for (int i = 0; i < fileNum; i += fileStep) {
			readOriginFile(fileNames[i].toString());
		}
		System.out.println("data.size() = " + data.size());
		Collections.sort(data);
		write();
		

		
		cleanData();
		
		//remove little items
		removeItemLittleWatched();
		
//		reOrderUser() ;
		writeFormatData();
	}
	
	
	public void reOrderUser() {
		String filePath  = "/home/xv/DataForRecom/saveData/allData.txt";
//		ArrayList<String> ttt = data;
//		ttt.clear();
//		System.out.println("after clear data = " + data);
		try {
		    File f = new File(filePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    HashMap<String, Integer> proCache = new HashMap<String, Integer>();
		    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		    BufferedWriter writer = new BufferedWriter(write);
		    String lastUser = "";
		    String lastItem = "";
		    int userID = 0;
		    int itemID = 0;
		    for (String elem:data) {
		    	String[] tmp = elem.split("\t");
		    	if (lastUser.compareTo(tmp[0]) != 0) {
		    		userID++;
		    		lastUser = tmp[0];
		    	}
		    	
		    	//re number item
//		    	if (proCache.containsKey(tmp[1]) ==  false) {
//		    		itemID++;
//		    		proCache.put(tmp[1], itemID);
//		    	}
//		    	if (lastItem.compareTo(tmp[1]) != 0) {
//		    		itemID++;
//		    		lastItem = tmp[1];
//		    	}
		    	String d = String.valueOf(userID) + "\t" + tmp[1]+ "\t" + tmp[2];
		    	writer.write(d+"\n");
		    }
		    System.out.println("userID = " + userID);
		    System.out.println("itemID = " + itemID);
		    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	
	
	public void writeFormatData() {
		//re number user and item 
		String filePath  = "/home/xv/DataForRecom/saveData/allData.txt";
		
//		String filePath = "/home/xv/DataForRecom/saveData/sampleOne.txt";
//		String filePath = "/home/xv/DataForRecom/saveData/sampleOneOut.txt";
		
//		ArrayList<String> ttt = data;
//		ttt.clear();
//		System.out.println("after clear data = " + data);
		try {
		    File f = new File(filePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    HashMap<String, Integer> proCache = new HashMap<String, Integer>();
		    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		    BufferedWriter writer = new BufferedWriter(write);
		    String lastUser = "";
		    String lastItem = "";
		    int userID = 0;
		    int itemID = 0;
		    for (String elem:data) {
		    	String[] tmp = elem.split("\t");
		    	if (lastUser.compareTo(tmp[0]) != 0) {
		    		userID++;
		    		lastUser = tmp[0];
		    	}
		    	if (proCache.containsKey(tmp[1]) ==  false) {
		    		itemID++;
		    		proCache.put(tmp[1], itemID);
		    	}
		    	//re number item
//		    	if (lastItem.compareTo(tmp[1]) != 0) {
//		    		itemID++;
//		    		lastItem = tmp[1];
//		    	}
		    	String d = String.valueOf(userID) + "\t" + String.valueOf(proCache.get(tmp[1])) + "\t" + tmp[2];
		    	writer.write(d+"\n");
		    }
		    System.out.println("userID = " + userID);
		    System.out.println("itemID = " + itemID);
		    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	public void writeFormatDataForSampleOne() {
		//re number user and item 
//		String filePath  = "/home/xv/DataForRecom/saveData/allData.txt";
		
		String filePath = "/home/xv/DataForRecom/saveData/sampleOne.txt";
		String filePathOut = "/home/xv/DataForRecom/saveData/sampleOneOut.txt";
		
		
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
//                    String[] tmp = lineTxt.split("\t");
                	data.add(lineTxt);
                }
//                System.out.println("watchlists = " + watchLists.get(1)  + ",  aaa = " + watchLists.get(462));
                read.close();
	    }else{
	        System.out.println("找不到指定的文件");
	    }
	    } catch (Exception e) {
	        System.out.println("读取文件内容出错");
	        e.printStackTrace();
	    }
		
		Collections.sort(data);
		
//		ArrayList<String> ttt = data;
//		ttt.clear();
//		System.out.println("after clear data = " + data);
		try {
		    File f = new File(filePathOut);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    HashMap<String, Integer> proCache = new HashMap<String, Integer>();
		    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		    BufferedWriter writer = new BufferedWriter(write);
		    String lastUser = "";
		    String lastItem = "";
		    int userID = 0;
		    int itemID = 0;
		    for (String elem:data) {
		    	String[] tmp = elem.split("\t");
		    	if (lastUser.compareTo(tmp[0]) != 0) {
		    		userID++;
		    		lastUser = tmp[0];
		    	}
		    	if (proCache.containsKey(tmp[1]) ==  false) {
		    		itemID++;
		    		proCache.put(tmp[1], itemID);
		    	}
		    	//re number item
//		    	if (lastItem.compareTo(tmp[1]) != 0) {
//		    		itemID++;
//		    		lastItem = tmp[1];
//		    	}
		    	String d = String.valueOf(userID) + "\t" + String.valueOf(proCache.get(tmp[1])) + "\t" + tmp[2];
		    	writer.write(d+"\n");
		    }
		    System.out.println("userID = " + userID);
		    System.out.println("itemID = " + itemID);
		    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	
	public void cleanData() {
		data.clear();
		String filePath  = "/home/xv/DataForRecom/saveData/allData.txt";
		String cleanedData  = "/home/xv/DataForRecom/saveData/cleanedData.txt";
		try {	    
            String encoding="GBK";
            File file=new File(filePath);
            File f = new File(cleanedData);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		    BufferedWriter writer = new BufferedWriter(write);
		    String lastUser = "";
		    String lastItem = "";
		    int cnt = 1;
		    double ratio = 0.0;
            
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    String[] tmp = lineTxt.split("\t");
                    if (lastUser.compareTo(tmp[0]) == 0 && lastItem.compareTo(tmp[1]) == 0) {
                    	ratio += Double.valueOf(tmp[2]);
                    	cnt++;
                    } else {
                    	if (lastUser.isEmpty() == false) {
                        	ratio /= cnt;
                        	String dt = lastUser + "\t" + lastItem + "\t" + String.valueOf(ratio);
                        	data.add(dt);
                    	} 
                    	cnt = 1;
                    	lastUser = tmp[0];
                    	lastItem = tmp[1];
                    	ratio = Double.valueOf(tmp[2]);
                    }
                }
                ratio /= cnt;
            	String dt = lastUser + "\t" + lastItem + "\t" + String.valueOf(ratio);
            	data.add(dt);                
                read.close();
                
//                ArrayList<String> tmpData = new ArrayList<String>();
                int i = 1;
                int start =0;
                cnt = 1;
                String[] tmp =data.get(0).split("\t");
                lastUser = tmp[0];
                while (i < data.size()) {
                	tmp =data.get(i).split("\t");
                	if (lastUser.compareTo(tmp[0]) == 0) {
                		cnt++;
                	} else {
                		if (cnt < itemLimit) {
                			i = start-1;
                			while(cnt> 0) {
                				data.remove(start);
                				cnt--;
                			}
                		} else {
                			start = i;
                		}
                		cnt = 1;
                		lastUser = tmp[0];
                	}
                	i++;
                }
                
                for (String elem:data) {
    		    	writer.write(elem+"\n");
    		    }
    		    writer.close();
                
                
	    }else{
	        System.out.println("找不到指定的文件");
	    }
	    } catch (Exception e) {
	        System.out.println("读取文件内容出错");
	        e.printStackTrace();
	    }
		
	}
	
	public void removeItemLittleWatched() {
		int i = 0;
		while (i < data.size()) {
			String[] tmp = data.get(i).split("\t");
			if (itemCnt.containsKey(tmp[1])) {
				int cnt = itemCnt.get(tmp[1]);
				cnt++;
				itemCnt.put(tmp[1], cnt);
			} else {
				itemCnt.put(tmp[1], 1);
			}
			i++;
		}
		i = 0;
		while (i < data.size()) {
			String[] tmp = data.get(i).split("\t");
			if (itemCnt.get(tmp[1]) < numLimit) {
				data.remove(i);
			} else {
				i++;
			}
		}
		
		
		i = 1;
        int start =0;
        int cnt = 1;
        String[] tmp =data.get(0).split("\t");
        String lastUser = tmp[0];
        while (i < data.size()) {
        	tmp =data.get(i).split("\t");
        	if (lastUser.compareTo(tmp[0]) == 0) {
        		cnt++;
        	} else {
        		if (cnt < itemLimit) {
        			i = start-1;
        			while(cnt> 0) {
        				data.remove(start);
        				cnt--;
        			}
        		} else {
        			start = i;
        		}
        		cnt = 1;
        		lastUser = tmp[0];
        	}
        	i++;
        }
		
		
	}
	
	
	public static void main(String[] args) throws Exception {
		GetDataFromOrigin tmp = new GetDataFromOrigin();
		String proInfo = "/home/xv/DataForRecom/saveData/programInfo.txt";
		tmp.getProLength(proInfo);
		String path = "/home/xv/DataForRecom/originData/";
		tmp.getAllFileNames(path);
		
		
//		tmp.writeFormatDataForSampleOne();

		
		System.out.println("Done");
	}
}
