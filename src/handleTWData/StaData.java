/**
 * 
 */
package handleTWData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * @author xv
 *
 */
public class StaData {
	HashMap<String, Long> kX = new HashMap<String, Long>();
	HashMap<Long, Long> kY = new HashMap<Long, Long>();
	
	HashMap<String, Long> kXUser = new HashMap<String, Long>();
	HashMap<Long, Long> kYUser = new HashMap<Long, Long>();
	
	HashMap<String, ArrayList<String>> userItems = new HashMap<String, ArrayList<String>>();
	HashMap<Long, ArrayList<String>> popUser = new HashMap<Long, ArrayList<String>>();
	
	
	public void getKX(String path) {
		File file = new File(path);
		File[] fileNames = file.listFiles();
		for (int i = 0; i < fileNames.length; i++) {
			readTxtFile(fileNames[i].toString());
		}
		System.out.println("kX = " + kX);
	}
	
	public void getKY() {
		Set<String> keys = kX.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Long num = kX.get(key);
			Long cnt = (long) 1;
			if (kY.containsKey(num)) {
				cnt = kY.get(num);
				cnt++;
			}
			kY.put(num, cnt);
		}
	}
	
	public void writeIntoFile(String filePath) {
		try {
		    File f = new File(filePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
			    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
			    BufferedWriter writer = new BufferedWriter(write);
			    Set<String> keys = kX.keySet();
				Iterator<String> iterator = keys.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					Long num = kX.get(key);
					Long cnt = kY.get(num);
					String tmp = String.valueOf(Math.log(num)) + "," + String.valueOf(Math.log(cnt)) + "\r\n";
					writer.write(tmp);
				}
//			    for (Double ele:list) {
//			    	String tmp = String.valueOf(userID++) + "," + String.valueOf(ele) + "\r\n";
//			    	writer.write(tmp);
//			    }
			    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	public void readTxtFile(String filePath) {
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    String[] tmp = lineTxt.split("\\|");
                    if (tmp.length > 7) {
                    	Long cnt = (long) 1;
//                    	item
                    	if (kX.containsKey(tmp[7])) {
                    		cnt = kX.get(tmp[7]);
                    		cnt++;
                    	}
                    	kX.put(tmp[7], cnt);
                    	
                    	
//                    	if (tmp[0].contains("starttime")) {
//                    		System.out.println("filepath = " + filePath);
//                    	}
                    	if (!tmp[0].contains("starttime")) {
                    		if (!userItems.containsKey(tmp[0])) {
                				userItems.put(tmp[0], new ArrayList<String>());
                			}
                			userItems.get(tmp[0]).add(tmp[7]);
                    	}
//                    	//user
//                      	if (kX.containsKey(tmp[0])) {
//                    		cnt = kX.get(tmp[0]);
//                    		cnt++;
//                    	}
//                    	kX.put(tmp[0], cnt);
                    }
                }
	//                System.out.println("watchlists = " + watchLists.get(1)  + ",  aaa = " + watchLists.get(462));
	                read.close();
//	                System.out.println("userItems = " + userItems);
		    }else{
		        System.out.println("找不到指定的文件");
		    }
		    } catch (Exception e) {
		        System.out.println("读取文件内容出错");
		        e.printStackTrace();
		    }
	}
	
	public void getPopUsers() {
		System.out.println("userItems = " + userItems.size());
		Set<String> keys = userItems.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Long num = (long) userItems.get(key).size();
			if (!popUser.containsKey(num)) {
				popUser.put(num, new ArrayList<String>());
			}
			popUser.get(num).add(key);
		}
		System.out.println("popUser = " + popUser);
		System.out.println("popUser.size() = " + popUser.size());
	}
	
	public void getResult(String filePath) {
		//userPopular --- item mean popular
		try {
		    File f = new File(filePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
			    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
			    BufferedWriter writer = new BufferedWriter(write);
			    Set<Long> keys = popUser.keySet();
				Iterator<Long> iterator = keys.iterator();
				while (iterator.hasNext()) {
					Long key = iterator.next();
					ArrayList<String> users = popUser.get(key);
					Long popu = (long) 0;
					Long num = (long) 0;
					if (users.size() < 1) {
						System.out.println("users.size() = 0");
					}
					for (int i = 0; i < users.size(); i++) {
						ArrayList<String> items = userItems.get(users.get(i));
						if (items.size() < 1) {
							System.out.println("items.size() = 0");
						}
//						System.out.println("items.size()  = " + items.size());
//						System.out.println("items  = " + items);
						for (int j = 0; j < items.size(); j++) {
							if (kX.containsKey(items.get(j))) {
//								System.out.println("Contain");
								popu += kX.get(items.get(j));
								num += 1;
							}
						}
					}
//					System.out.println("popu = " + popu + " , num = " + num);
					double tt = (double)popu/num;
//					String tmp = String.valueOf(Math.log(key)) + "," + String.valueOf(Math.log(tt)) + "\r\n";
					String tmp = String.valueOf(key) + "," + String.valueOf(tt) + "\r\n";
					writer.write(tmp);
				}
//			    for (Double ele:list) {
//			    	String tmp = String.valueOf(userID++) + "," + String.valueOf(ele) + "\r\n";
//			    	writer.write(tmp);
//			    }
			    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	
	public void myMethod() {
		String path = "/home/xv/DataForRecom/originData/";
		getKX(path);
//		getKY();
		getPopUsers();
		path  = "/home/xv/DataForRecom/saveData/final.txt";
		getResult(path);
	}
	
	public static void main(String[] args) throws Exception {
		StaData tmp = new StaData();
//		String path = "/home/xv/DataForRecom/originData/";
//		tmp.getKX(path);
//		tmp.getKY();
////		path  = "/home/xv/DataForRecom/saveData/popuItem.xls";
//		path  = "/home/xv/DataForRecom/saveData/user_popuItem.xls";
//		tmp.writeIntoFile(path);
		tmp.myMethod();
		
		
		
		System.out.println("Done");
	}
}
