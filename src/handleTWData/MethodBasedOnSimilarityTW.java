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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author xv
 *
 */
public class MethodBasedOnSimilarityTW {
	protected final int user_num = 2318;
	protected final int item_num = 4358;
	protected final int clusterNum = 200;
	protected final double averg = 0.8076;
	
//	protected final int user_num = 943;
//	protected final int item_num = 1682;
//	protected final int clusterNum = 100;
//	protected final double averg = 3;
	
//	protected final int user_num = 4000;
//	protected final int item_num = 3000;
//	protected final int clusterNum = 300;
//	protected final double averg = 0.9394;
	
	protected final int userClusterNum = 100;
	
	protected HashMap<Integer, HashMap<Integer, Double> > trainData = new HashMap<Integer, HashMap<Integer, Double> >();
	protected HashMap<Integer, HashMap<Integer, Double> > testData = new HashMap<Integer, HashMap<Integer, Double> >();
	protected HashMap<Integer, Integer> itemCnt = new HashMap<Integer, Integer>();
	protected HashMap<Integer, HashMap<Integer, Double>> itemByUsers = new HashMap<Integer,HashMap<Integer, Double>>();
	//	protected int[][] trainData = new int[user_num][item_num];
//	protected int[][] testData = new int[user_num][item_num];
//	protected double[][] simiMatrix = new double[item_num][item_num];
	protected double[][] simiMatrix = new double[item_num][item_num];
	protected double[][] userSim = new double[user_num][user_num];
	
	protected HashMap<Integer, ArrayList<Integer> > clusterResult = new HashMap<Integer, ArrayList<Integer>>();
	protected HashMap<Integer, ArrayList<Integer> > userCluster = new HashMap<Integer, ArrayList<Integer>>();
	protected double[][] itemVector = new double[item_num][clusterNum];
	protected double[][] userVector = new double[user_num][clusterNum];
	protected double[][] ratingMatrix = new double[user_num][item_num];
	
	protected double[][] upmat = new double[user_num][item_num];
	
	private int testEntryNum = 0;
	
	protected ArrayList<ArrayList<Integer>> recomItems = new ArrayList<ArrayList<Integer>>();
	
	protected HashMap<Integer, Double> userAvg = new HashMap<Integer, Double>();
	protected HashMap<Integer, Double> itemAvg = new HashMap<Integer, Double>();
//	protected HashMap<Integer, Double> userVar = new HashMap<Integer, Double>();
	
//	protected Integer[][]  userToCluster = new Integer[user_num][userClusterNum]; 
	
	protected int[] testRec = new int[item_num];
	protected int[] testRec2 = new int[item_num];
	
	public void initRatingMatrix() {
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < item_num; j++) {
				upmat[i][j] = 0.0;
			}
		}
	}
	
	
	
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
	
	public void reComputeItemSim() {
		for (int i = 1; i <= item_num; i++) {
			for (int j = i; j <= item_num; j++) {
				boolean isEnter = false;
				HashMap<Integer, Double> itemI = itemByUsers.get(i);
				HashMap<Integer, Double> itemJ = itemByUsers.get(j);;
				double s = 0.0;
				for (int uid = 1; uid <= user_num; uid++) {
					if (itemI != null && itemI.containsKey(uid) && itemJ != null &&  itemJ.containsKey(uid)) {
						isEnter = true;
						s += Math.abs(upmat[uid-1][i-1] - upmat[uid-1][j-1]);
					}
				}
				if (isEnter) {
					simiMatrix[i-1][j-1]  = 1/(1+s);
				}
			}
		}
	}
	
	public void computeSimilary() {
		System.out.println("Start to compute similarity");
		long start = System.currentTimeMillis();
		for (int i = 1; i <= item_num; i++) {
			System.out.println("The " + (i) + " item...");
			for (int j = i; j <= item_num; j++) {
				double d = 0;
				for (int uid = 1; uid <= user_num; uid++) {
					double x = 0.0;
					double y = 0.0;
					if (trainData.containsKey(uid)) {
						if (trainData.get(uid).containsKey(i)) {
							x = trainData.get(uid).get(i);
						}
						if (trainData.get(uid).containsKey(j)) {
							y = trainData.get(uid).get(j);
						}
					}
					d += Math.abs(x-y);
				}
				double tmp = 1/(1+d);
//				System.out.println(tmp);
				simiMatrix[i-1][j-1] = tmp;
			}
		}
		System.out.println("simiMatrix = " + simiMatrix);
		long end = System.currentTimeMillis();
		System.out.println("computeSimilary 运行时间：" + (end - start) + "毫秒");
		System.out.println("Already get the similarity matrix.");
	}
	
	public void computeSimilaryAllItems() {
		System.out.println("Start to compute similarity");
		long start = System.currentTimeMillis();
		for (int i = 1; i <= item_num; i++) {
//			System.out.println("The " + (i) + " item...");
			for (int j = i; j <= item_num; j++) {
				double d = 0;
				for (int uid = 1; uid <= user_num; uid++) {
					double x = upmat[uid-1][i-1];
					double y = upmat[uid-1][j-1];
					
//					double x = 0.0;
//					double y = 0.0;
//					if (trainData.containsKey(uid)) {
//						if (trainData.get(uid).containsKey(i)) {
//							x = trainData.get(uid).get(i);
//						}
//						if (trainData.get(uid).containsKey(j)) {
//							y = trainData.get(uid).get(j);
//						}
//					}
					d += Math.abs(x-y);
				}
				double tmp = 1/(1+d);
//				System.out.println(tmp);
				simiMatrix[i-1][j-1] = tmp;
			}
		}
		System.out.println("simiMatrix = " + simiMatrix);
		long end = System.currentTimeMillis();
		System.out.println("computeSimilary 运行时间：" + (end - start) + "毫秒");
		System.out.println("Already get the similarity matrix.");
	}
	
	
	
	public void writeSimiMatrixIntoFile(String filePath) {
		try {
		    File f = new File(filePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		    BufferedWriter writer = new BufferedWriter(write);
		    for (int i = 0; i < item_num; i++) {
		    	String tmp = String.valueOf(simiMatrix[i][0]);
		    	for (int j = 1; j < item_num; j++) {
		    		tmp += "\t" + String.valueOf(simiMatrix[i][j]);
		    	}
		    	tmp += "\n";
		    	writer.write(tmp);
		    }
		    
		    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	public void getTrainData(String filePath) {
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int lastUid = 0;
                while((lineTxt = bufferedReader.readLine()) != null){
                    String[] tmp = lineTxt.split("\t");
                    int uid = Integer.parseInt(tmp[0]);
                    int iid = Integer.parseInt(tmp[1]);
                    if (lastUid != uid && lastUid + 1 != uid) {
                    	System.out.println("short " + uid);
                    }
                    lastUid = uid;
                    double rating = Double.valueOf(tmp[2]);
                    if (!trainData.containsKey(uid)) {
                    	trainData.put(uid, new HashMap<Integer, Double>());
                    } 
                    
                    if (!itemByUsers.containsKey(iid)) {
                    	itemByUsers.put(iid, new HashMap<Integer, Double>());
                    }
                    trainData.get(uid).put(iid, rating);
                    itemByUsers.get(iid).put(uid, rating);
                    
                    upmat[uid-1][iid-1] = rating;
                    
                    if (!itemCnt.containsKey(iid)) {
                    	itemCnt.put(iid, 0);
                    }
                    int cnt = itemCnt.get(iid);
                    cnt++;
                    itemCnt.put(iid, cnt);
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
	
	public void getTestData(String filePath) {
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                	testEntryNum++;
                    String[] tmp = lineTxt.split("\t");
                    int uid = Integer.parseInt(tmp[0]);
                    int iid = Integer.parseInt(tmp[1]);
                    double rating = Double.valueOf(tmp[2]);
                    if (!testData.containsKey(uid)) {
                    	testData.put(uid, new HashMap<Integer, Double>());
                    } 
                    testData.get(uid).put(iid, rating);
                    
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
	
	public void readSimiMatrixFile(String filePath) {
		System.out.println("Start to load simiMatrix...");
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int i = 0;
                while((lineTxt = bufferedReader.readLine()) != null){
                    String[] tmp = lineTxt.split("\t");
//                    System.out.println("tmp.size() = " + tmp.length);
                	for (int j = 0; j < item_num; j++) {
                		simiMatrix[i][j] = Double.valueOf(tmp[j]);
                	}
                    i++;
                }
                read.close();
		    }else{
		        System.out.println("找不到指定的文件");
		    }
	    } catch (Exception e) {
	        System.out.println("读取文件内容出错");
	        e.printStackTrace();
	    }
		System.out.println("load done");
	}
	
	
	public void buildSingleItemVector() {
		System.out.println("Start to build itemVector...");
		long start = System.currentTimeMillis();		
		
		
		int[] centers = new int[clusterNum];
		
		Set<Integer> keys = clusterResult.keySet();
		Iterator<Integer> iterator = keys.iterator();
		int idx = 0;
		while (iterator.hasNext()) {
			int key = iterator.next();
			centers[idx] = key;
			idx++;
		}
//		for (int i  = 0 ; i < clusterNum; i++) {
//			System.out.println("centers " + i +  "  = " + centers[i]);
//		}
		for (int i = 0; i < item_num; i++) {
			for (int j = 0; j < clusterNum; j++) {
				if (clusterResult.get(centers[j]).contains(i)) {
					itemVector[i][j] = 1.0;
					testRec[i] = j;
					break;
				}
			}

			
		}
		long end = System.currentTimeMillis();
		System.out.println("buildItemVector 运行时间：" + (end - start) + "毫秒");

	}
	
	
	
	public double getSim(int x, int y) {
		if (x <= y) {
			return simiMatrix[x][y];
		}
		return simiMatrix[y][x];
	}
	
	
	public double getUserSim(int x, int y) {
		if (x <= y) {
			return userSim[x][y];
		}
		return userSim[y][x];
	}
	
	
	public void clusteringReorderCenters(int K) {
		System.out.println("116 162 = " + getSim(116, 162) );
		System.out.println("116 201 = " + getSim(116, 201) );
		long start = System.currentTimeMillis();
		System.out.println("Start to cluster...");
		ArrayList<Integer> itemList = new ArrayList<Integer>();
		for (int i = 0; i < item_num; i++) {
			itemList.add(i);
		}
		int k = 0;
		while (k < K-1) {
			int x = 0;
			int y = 0;
			double sim = 10000000.0;
			for (int i = 0; i < itemList.size(); i++) {
				int iTerm = itemList.get(i);
				for (int j = i+1; j < itemList.size(); j++) {
					int jTerm = itemList.get(j);
					double s = getSim(iTerm, jTerm);
					if (s < sim) {
						sim = s;
						x = iTerm;
						y = jTerm;
					}
				}
			}
			System.out.println("x = " + x);
			System.out.println("y = " + y);
			clusterResult.put(x, new ArrayList<Integer>());
			clusterResult.put(y, new ArrayList<Integer>());
//			System.out.println("clusterResult size() 1= " + clusterResult.size());
			for (Integer id:itemList) {
				if (getSim(x, id) >= getSim(y, id)) {
					clusterResult.get(x).add(id);
				} else {
					clusterResult.get(y).add(id);
				}
			}
			System.out.println("clusterResult.get(x) = " + clusterResult.get(x));
			System.out.println("clusterResult.get(y) = " + clusterResult.get(y));
			int centerWithMostItems = 0;
			int cnt = 0;
			Set<Integer> keys = clusterResult.keySet();
			Iterator<Integer> iterator = keys.iterator();
			while (iterator.hasNext()) {
				int key = iterator.next();
				ArrayList<Integer> al = clusterResult.get(key);
				if (al.size() > cnt ) {
					cnt = al.size();
					centerWithMostItems = key;
				}
			}
			itemList.clear();
			itemList = clusterResult.get(centerWithMostItems);
			k++;
			if (k < K-1) {
				clusterResult.remove(centerWithMostItems);
			}
			System.out.println("itemList = " + itemList);
			System.out.println("item cluster size   = " + clusterResult.size());
		}
		

		long end = System.currentTimeMillis();
		System.out.println("clustering 运行时间：" + (end - start) + "毫秒");
		System.out.println("cluster  = " + clusterResult);
		System.out.println("cluster.size()  = " + clusterResult.size());
	}
	
	
	
	public void clustering(int K) {
		System.out.println("116 162 = " + getSim(116, 162) );
		System.out.println("116 201 = " + getSim(116, 201) );
		long start = System.currentTimeMillis();
		System.out.println("Start to cluster...");
		ArrayList<Integer> itemList = new ArrayList<Integer>();
		for (int i = 0; i < item_num; i++) {
			itemList.add(i);
		}
		int k = 2;
		int x = 0;
		int y = 0;
		double sim = 10000000.0;
		for (int i = 0; i < itemList.size(); i++) {
			int iTerm = itemList.get(i);
			for (int j = i+1; j < itemList.size(); j++) {
				int jTerm = itemList.get(j);
				double s = getSim(iTerm, jTerm);
				if (s < sim) {
					sim = s;
					x = iTerm;
					y = jTerm;
				}
			}
		}
		clusterResult.put(x, new ArrayList<Integer>());
		clusterResult.put(y, new ArrayList<Integer>());
		
		while (k <= K) {
			System.out.println("k = " + k);
			for (int i = 0; i < item_num; i++) {
				sim = -1.0;
				int center = -1;
				Set<Integer> keys = clusterResult.keySet();
				Iterator<Integer> iterator = keys.iterator();
				while (iterator.hasNext()) {
					int key = iterator.next();
					double s = getSim(i, key);
					if (sim < s ) {
						sim = s;
						center = key;
					}
				}
				clusterResult.get(center).add(i);
			}
			
			if (k == K) {
				break;
			}
			
			itemList.clear();
			int centerWithMostItems = 0;
			int cnt = 0;
			Set<Integer> keys = clusterResult.keySet();
			Iterator<Integer> iterator = keys.iterator();
			while (iterator.hasNext()) {
				int key = iterator.next();
				ArrayList<Integer> al = clusterResult.get(key);
				if (al.size() > cnt ) {
					cnt = al.size();
					centerWithMostItems = key;
					itemList = clusterResult.get(centerWithMostItems);
//					System.out.println("itemList  = " + itemList);
				}
//				clusterResult.get(key).clear();
			}
		
			
			
			k++;
			
			sim = 100000.0;
			int newCenter = -1;
			if (itemList.size() == 0) {
				System.out.println("There are none..........................................................................");
			}
			for (int i = 0; i < itemList.size(); i++) {
				double s1 = getSim(itemList.get(i),centerWithMostItems );
				if (s1 < sim) {
					sim = s1;
					newCenter = itemList.get(i);
				}
			}
			
			keys = clusterResult.keySet();
			iterator = keys.iterator();
			while (iterator.hasNext()) {
				int key = iterator.next();
				clusterResult.get(key).clear();
			}
			
			
			clusterResult.put(newCenter, new ArrayList<Integer>());
			
//			System.out.println("itemList = " + itemList);
			System.out.println("item cluster size   = " + clusterResult.size());
		}
		
		
//		for (int k = 0; k < K; k++) {
//			int x = 0;
//			int y = 0;
//			double sim = 10000000.0;
//			for (int i = 0; i < itemList.size(); i++) {
//				int iTerm = itemList.get(i);
//				for (int j = i+1; j < itemList.size(); j++) {
//					int jTerm = itemList.get(j);
//					double s = getSim(iTerm, jTerm);
//					if (s < sim) {
//						sim = s;
//						x = iTerm;
//						y = jTerm;
//					}
//				}
//			}
////			System.out.println("x = " + x);
////			System.out.println("y = " + y);
//			clusterResult.put(x, new ArrayList<Integer>());
//			clusterResult.put(y, new ArrayList<Integer>());
////			System.out.println("clusterResult size() 1= " + clusterResult.size());
//			for (Integer id:itemList) {
//				if (getSim(x, id) >= getSim(y, id)) {
//					clusterResult.get(x).add(id);
//				} else {
//					clusterResult.get(y).add(id);
//				}
//			}
////			System.out.println("x = " + x);
////			System.out.println(clusterResult.get(x));
////			System.out.println("y = " + y);
////			System.out.println(clusterResult.get(y));
//			int centerWithMostItems = 0;
//			int cnt = 0;
//			Set<Integer> keys = clusterResult.keySet();
//			Iterator<Integer> iterator = keys.iterator();
//			while (iterator.hasNext()) {
//				int key = iterator.next();
//				ArrayList<Integer> al = clusterResult.get(key);
//				if (al.size() > cnt ) {
//					cnt = al.size();
//					centerWithMostItems = key;
//				}
//			}
//			itemList.clear();
//			itemList = clusterResult.get(centerWithMostItems);
//			if (k < K-1) {
//				clusterResult.remove(centerWithMostItems);
//			}
//		}
		long end = System.currentTimeMillis();
		System.out.println("clustering 运行时间：" + (end - start) + "毫秒");
		System.out.println("cluster  = " + clusterResult);
		System.out.println("cluster.size()  = " + clusterResult.size());
	}
	
	
	public void computeUserAverage() {
		long start = System.currentTimeMillis();
		System.out.println("Start to computeUserAverage...");
		Set<Integer> keys = trainData.keySet();
		Iterator<Integer> iterator = keys.iterator();
		while (iterator.hasNext()) {
			int key = iterator.next();
			HashMap<Integer, Double> tmp = trainData.get(key);
			Set<Integer> keysItem = tmp.keySet();
			Iterator<Integer> iteratorItem = keysItem.iterator();
			Double avg = 0.0;
			int cnt = 0;
			while (iteratorItem.hasNext()) {
				int keyItem = iteratorItem.next();
//				System.out.println("keyItem = " + keyItem);
				double tmpItem = tmp.get(keyItem);
				cnt++;
				avg += tmpItem;
			}
			avg /= cnt;
			userAvg.put(key, avg);
		}
		long end = System.currentTimeMillis();
		System.out.println("userAvg = " + userAvg);
		System.out.println("computeUserAverage 运行时间：" + (end - start) + "毫秒");
	}
	
	public void computeItemAverage() {
		long start = System.currentTimeMillis();
		System.out.println("Start to computeItemAverage...");
		Set<Integer> keys = itemByUsers.keySet();
		Iterator<Integer> iterator = keys.iterator();
		while (iterator.hasNext()) {
			int key = iterator.next();
			HashMap<Integer, Double> tmp = itemByUsers.get(key);
			Set<Integer> keysItem = tmp.keySet();
			Iterator<Integer> iteratorItem = keysItem.iterator();
			Double avg = 0.0;
			int cnt = 0;
			while (iteratorItem.hasNext()) {
				int keyItem = iteratorItem.next();
//				System.out.println("keyItem = " + keyItem);
				double tmpItem = tmp.get(keyItem);
				cnt++;
				avg += tmpItem;
			}
			avg /= cnt;
			itemAvg.put(key, avg);
		}
		long end = System.currentTimeMillis();
		System.out.println("itemAvg = " + itemAvg);
		System.out.println("computeItemAverage 运行时间：" + (end - start) + "毫秒");
	}
	
	
	
	public void computeUserSimi() {
		long start = System.currentTimeMillis();
		System.out.println("Start to computeUserSimi...");
		
//		//pearson  s1
//		for (int i = 1; i <= user_num; i++) {
//			for (int j = i; j <= user_num; j++) {
//				HashMap<Integer, Double> itemI = trainData.get(i);
//				HashMap<Integer, Double> itemJ = trainData.get(j);
//				double avg1 = userAvg.get(i);
//				double avg2 = userAvg.get(j);
//				double s = 0.0;
//				double var1 = 0.0;
//				double var2 = 0.0;
//				boolean isEnter = false;
//				Set<Integer> keys = itemI.keySet();
//				Iterator<Integer> iterator = keys.iterator();
//				while (iterator.hasNext()) {
//					int key = iterator.next();
//					if (itemJ.containsKey(key)) {
//						isEnter = true;
//						s += (itemI.get(key) - avg1)*(itemJ.get(key) - avg2);
//						var1 +=  (itemI.get(key) - avg1)* (itemI.get(key) - avg1);
//						var2 += (itemJ.get(key) - avg2)*(itemJ.get(key) - avg2);
//					}
//				}
//				if (isEnter) {
//					userSim[i-1][j-1]  = s/(Math.sqrt(var1)*Math.sqrt(var2));
//				}
//			}
//		}
		
//////		abs   s2
//		for (int i = 1; i <= user_num; i++) {
//			for (int j = i; j <= user_num; j++) {
//				HashMap<Integer, Double> itemI = trainData.get(i);
//				HashMap<Integer, Double> itemJ = trainData.get(j);;
//				double s = 0.0;
//				boolean isEnter = false;
//				Set<Integer> keys = itemI.keySet();
//				Iterator<Integer> iterator = keys.iterator();
//				while (iterator.hasNext()) {
//					int key = iterator.next();
//					if (itemJ.containsKey(key)) {
//						isEnter = true;
//						s +=Math.abs (itemI.get(key) - itemJ.get(key) );
//					}
//				}
//				if (isEnter) {
//					userSim[i-1][j-1]  = 1/(1+s);
//				}
//			}
//		}
		
		
//		xiangliang's     s3
		for (int i = 1; i <= user_num; i++) {
			for (int j = i; j <= user_num; j++) {
				HashMap<Integer, Double> itemI = trainData.get(i);
				HashMap<Integer, Double> itemJ = trainData.get(j);
				int cnt = 0;
				boolean isEnter = false;
				Set<Integer> keys = itemI.keySet();
				Iterator<Integer> iterator = keys.iterator();
				while (iterator.hasNext()) {
					int key = iterator.next();
					if (itemJ.containsKey(key)) {
						isEnter = true;
						cnt++;
					}
				}
				if (isEnter) {
//					userSim[i-1][j-1]  = cnt*1.0/Math.sqrt(itemI.size()*itemJ.size());
					userSim[i-1][j-1]  = cnt*1.0/(itemI.size()*itemJ.size());
				}
			}
		}
		
		
//		//s4  correct popular programs
//		for (int i = 1; i <= user_num; i++) {
//			for (int j = i; j <= user_num; j++) {
//				HashMap<Integer, Double> itemI = trainData.get(i);
//				HashMap<Integer, Double> itemJ = trainData.get(j);
//				double s = 0.0;
//				boolean isEnter = false;
//				Set<Integer> keys = itemI.keySet();
//				Iterator<Integer> iterator = keys.iterator();
//				while (iterator.hasNext()) {
//					int key = iterator.next();
//					if (itemJ.containsKey(key)) {
//						isEnter = true;
//						s += 1/(Math.log(1+itemCnt.get(key)));
//					}
//				}
//				if (isEnter) {
//					userSim[i-1][j-1]  = s/(Math.sqrt(itemI.size())*Math.sqrt(itemJ.size()));
//				}
//			}
//		}
		
		
		long end = System.currentTimeMillis();
		System.out.println("computeUserSimi 运行时间：" + (end - start) + "毫秒");
	}
	
	
	public void saveUPmat(String filePath) {
		try {
		    File f = new File(filePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		    BufferedWriter writer = new BufferedWriter(write);
		    for (int i = 0; i < user_num; i++) {
		    	String tmp = String.valueOf(upmat[i][0]);
		    	for (int j = 1; j < item_num; j++) {
		    		tmp += "\t" + String.valueOf(upmat[i][j]);
		    	}
		    	tmp += "\n";
		    	writer.write(tmp);
		    }
		    
		    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	
	public void saveUserSim(String filePath) {
		try {
		    File f = new File(filePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		    BufferedWriter writer = new BufferedWriter(write);
		    for (int i = 0; i < user_num; i++) {
		    	String tmp = String.valueOf(userSim[i][0]);
		    	for (int j = 1; j < user_num; j++) {
		    		tmp += "\t" + String.valueOf(userSim[i][j]);
		    	}
		    	tmp += "\n";
		    	writer.write(tmp);
		    }
		    
		    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	public void userClustering(int K) {
		long start = System.currentTimeMillis();
		System.out.println("Start to userClustering...");
		ArrayList<Integer>userList = new ArrayList<Integer>();
		for (int i = 0; i < user_num; i++) {
			userList.add(i);
		}
		
		for (int k = 0; k < K; k++) {
			int x = 0;
			int y = 0;
			double sim = 10000000.0;
			for (int i = 0; i < userList.size(); i++) {
				int iTerm = userList.get(i);
				for (int j = i+1; j < userList.size(); j++) {
					int jTerm = userList.get(j);
					double s = getUserSim(iTerm, jTerm);
					if (s < sim) {
						sim = s;
						x = iTerm;
						y = jTerm;
					}
				}
			}
//			System.out.println("x = " + x);
//			System.out.println("y = " + y);
			userCluster.put(x, new ArrayList<Integer>());
			userCluster.put(y, new ArrayList<Integer>());
//			System.out.println("clusterResult size() 1= " + clusterResult.size());
			for (Integer id:userList) {
				if (getUserSim(x, id) >= getUserSim(y, id)) {
					userCluster.get(x).add(id);
				} else {
					userCluster.get(y).add(id);
				}
			}
//			System.out.println("x = " + x);
//			System.out.println(clusterResult.get(x));
//			System.out.println("y = " + y);
//			System.out.println(clusterResult.get(y));
			int centerWithMostItems = 0;
			int cnt = 0;
			Set<Integer> keys = userCluster.keySet();
			Iterator<Integer> iterator = keys.iterator();
			while (iterator.hasNext()) {
				int key = iterator.next();
				ArrayList<Integer> al = userCluster.get(key);
				if (al.size() > cnt ) {
					cnt = al.size();
					centerWithMostItems = key;
				}
			}
			userList.clear();
			userList = userCluster.get(centerWithMostItems);
			if (k < K - 1) {
				userCluster.remove(centerWithMostItems);
			}

		}
		long end = System.currentTimeMillis();
		System.out.println("userClustering 运行时间：" + (end - start) + "毫秒");
		System.out.println("cluster  = " + userCluster);
		System.out.println("cluster.size()  = " + userCluster.size());
	}
	
	public void fillMissingData() {
		int allCnt = 0;
		long start = System.currentTimeMillis();
		System.out.println("Start to userClustering...");
		for (int i = 0; i < user_num; i++) {
			double maxV = 1.0;
			ArrayList<Integer> users = new ArrayList<Integer>();
			Set<Integer> keys = userCluster.keySet();
			Iterator<Integer> iterator = keys.iterator();
			boolean isEnter = false;
			while (iterator.hasNext()) {
				int key = iterator.next();
//				if (i == 4) {
//					ArrayList<Integer> tt = userCluster.get(key);
//					for (int j = 0; j < tt.size(); j++ ) {
//						if (i == tt.get(j)) {
//							System.out.println("contain i=  4  key :" + key);
//						}
//					}
//				}
				if (userCluster.get(key).contains(i)) {
					users = userCluster.get(key);
					isEnter = true;
					break;
				}
			}
//			System.out.println("users.size() = " + users.size());
			if (isEnter == false) {
				System.out.println("users.size() 0    i = " + i);
			}
			for (int j = 0; j < item_num; j++) {
				if (!trainData.get(i+1).containsKey(j+1)) {
					double deltaR = userAvg.get(i+1);
//					ArrayList<Integer> users = new ArrayList<Integer>();
//					Set<Integer> keys = userCluster.keySet();
//					Iterator<Integer> iterator = keys.iterator();
//					while (iterator.hasNext()) {
//						int key = iterator.next();
//						if (userCluster.get(key).contains(i)) {
//							users = userCluster.get(key);
//							break;
//						}
//					}

					
					
					
					
					
//					int cnt = 0;
//					for (int uid = 0; uid < users.size(); uid++) {
//						if (trainData.get(users.get(uid)+1).containsKey(j+1)) {
//							cnt++;
//							deltaR += (trainData.get(users.get(uid)+1).get(j+1) - userAvg.get(users.get(uid)+1));
//						}
//					}
//					if (cnt > 0) {
//						deltaR /= cnt;
//						//linear
//						if (deltaR > 1) {
//							deltaR = 1;
//						}
					
					
					//xiangliang ' compute preference
					int cnt = 0;
					deltaR = 0.0;
					for (int uid = 0; uid < users.size(); uid++) {
						if (trainData.get(users.get(uid)+1).containsKey(j+1)) {
							cnt++;
							deltaR += userSim[i][users.get(uid)]*trainData.get(users.get(uid)+1).get(j+1);
						}
					}
					if (cnt > 0) {
						//linear
//						if (deltaR > 1) {
////							deltaR = 1;
//							System.out.println("deltaR = " + deltaR);
//						}
						maxV = Math.max(maxV,  deltaR);

//						upmat[i][j] = 0.0;   //0.2446
//						upmat[i][j] = 0.5;  //0.1708
//						upmat[i][j] = Math.random(); //0.1018
//						upmat[i][j] =  userAvg.get(i+1);  //0.1108
						if (Double.isNaN(deltaR)) {
							deltaR = 0.0;
						}
						upmat[i][j] = deltaR;  //0.0798(s1)     0.1328(s2)      0.096(s3)     0.1566(prefer2, s3)   0.1350(s2, p2)　　　0.1553(s1, p2)
						allCnt++;
					}

				}
			}
			if (maxV > 0) {
				for (int j = 0; j < item_num; j++) {
					upmat[i][j] /= maxV;
				}
			}
			
		}
		System.out.println("allCnt -------------------------------------------------> " + allCnt);
		long end = System.currentTimeMillis();
		System.out.println("userClustering 运行时间：" + (end - start) + "毫秒");
	}
	
	
	// To accolate the value for missing data
	public void fillMissingProg() {
		computeUserAverage() ;
		computeUserSimi();
		String path = "/home/xv/DataForRecom/saveData/userSim.txt";
		saveUserSim(path);
		userClustering(userClusterNum-1);
		fillMissingData();
		path = "/home/xv/DataForRecom/saveData/upmat.txt";
		saveUPmat(path);
	}
	
	
	
	/**
	 * multi-dimension
	 * similar to centers of clusters
	 */
	public void buildMultiItemVector() {
		System.out.println("Start to build itemVector...");
		long start = System.currentTimeMillis();		
		int[] centers = new int[clusterNum];
		Set<Integer> keys = clusterResult.keySet();
		Iterator<Integer> iterator = keys.iterator();
		int idx = 0;
		while (iterator.hasNext()) {
			int key = iterator.next();
			centers[idx++] = key;
		}
		
//		System.out.println("Multi ------------------------------");
//		for (int i  = 0 ; i < clusterNum; i++) {
//			System.out.println("centers " + i +  "  = " + centers[i]);
//		}
		
		for (int i = 0; i < item_num; i++) {
			double sum = 0.0;
			double vMax = -2.0;
			for (int j = 0; j < clusterNum; j++) {
				double tmp = getSim(i, centers[j]);
//				if (!Double.isNaN(tmp)) {
//					System.out.println("the simi " + i + "and " + j + " is not NaN");
//				}
				sum += tmp;
				itemVector[i][j] = tmp;
				
				vMax = Math.max(vMax, tmp);
				if (vMax < tmp) {
					vMax = tmp;
					testRec2[i] = j;
				}
				
			}
			
			if (vMax > -1.0) {
				for (int j = 0; j < clusterNum; j++) {
//					itemVector[i][j] /= sum;
					
					itemVector[i][j] /= vMax;
					//0.01 best
					if (itemVector[i][j] < 0.8) {
						itemVector[i][j] = 0.0;
					}
				}
			}
			
			
			int cnt = 0;
			for (int j = 0; j < clusterNum; j++) {
//				itemVector[i][j] /= sum;
				
				//0.01 best
				if (itemVector[i][j]  >=  0.5) {
					cnt++;
				}
			}
			System.out.println("cnt = " + cnt);
			
			
			
			
//			if (sum > 0) {
//				for (int j = 0; j < clusterNum; j++) {
//					itemVector[i][j] /= sum;
//					
////					itemVector[i][j] /= vMax;
//					//0.01 best
//					if (itemVector[i][j] < 0.1) {
//						itemVector[i][j] = 0.0;
//					}
//				}
//			}
			
		}
		long end = System.currentTimeMillis();
		System.out.println("buildItemVector 运行时间：" + (end - start) + "毫秒");
	}
	
	
	/**
	 * only add the items that have been watched by users
	 */
	public void buildMultiItemVector2() {
		System.out.println("Start to build itemVector2...");
		long start = System.currentTimeMillis();		
			
		for (int i = 0; i < item_num; i++) {
			double sum = 0.0;
//			for (int c = 0; c < clusterNum; c++) {
//				double numer = 0.0;
//				double denom1 = 0.0;
//				double denom2 = 0.0;
				
				Set<Integer> keys = clusterResult.keySet();
				Iterator<Integer> iterator = keys.iterator();
				int c = 0;
				while (iterator.hasNext()) {
					int key = iterator.next();
					ArrayList<Integer> itemsInCluster = clusterResult.get(key);
					ArrayList<Integer> usersAll = new ArrayList<Integer>();
					for (Integer item:itemsInCluster) {
						HashMap<Integer, Double> items = itemByUsers.get(item+1);
						Set<Integer> uids = items.keySet();
						Iterator<Integer> iter = uids.iterator();
						while(iter.hasNext()) {
							int uid = iter.next();
							if (!usersAll.contains(uid)) {
								usersAll.add(uid);
							}
						}
						
					}
					double numerator = 0.0;
					double denom1 = 0.0;
					double denom2 = 0.0;
					for (Integer uid:usersAll) {
						if (itemByUsers.get(i+1).containsKey(uid)) {
							double deltaR = 0.0;
							int cnt = 0;
							for (Integer itemInClu:itemsInCluster) {
								if (itemByUsers.get(itemInClu+1).containsKey(uid)) {
									cnt++;
									deltaR +=( itemByUsers.get(itemInClu+1).get(uid) - itemAvg.get(itemInClu+1));
								}
							}
							if (cnt > 0) {
								deltaR /= cnt;
							}
							numerator += deltaR*(itemByUsers.get(i+1).get(uid)-itemAvg.get(i+1));
							denom1 += deltaR*deltaR;
							denom2 += (itemByUsers.get(i+1).get(uid)-itemAvg.get(i+1))*(itemByUsers.get(i+1).get(uid)-itemAvg.get(i+1));
						}
					}
					if (denom1 > 0 && denom2 > 0) {
						itemVector[i][c] = numerator/(Math.sqrt(denom1*denom2));
						sum += numerator/(Math.sqrt(denom1*denom2));
					}
					c++;
				}
				if (sum > 0) {
					for (c = 0; c < clusterNum; c++) {
						itemVector[i][c] /= sum;
					}
				}
				

//			}
		}
		long end = System.currentTimeMillis();
		System.out.println("buildItemVector 2运行时间：" + (end - start) + "毫秒");
	}
	
	/**
	 * similar with all items in a cluster, and calculate the mean
	 */
	public void buildMultiItemVector3() {
		System.out.println("Start to build itemVector2...");
		long start = System.currentTimeMillis();		
			
		for (int i = 0; i < item_num; i++) {

				
				Set<Integer> keys = clusterResult.keySet();
				Iterator<Integer> iterator = keys.iterator();
				int c = 0;
				while (iterator.hasNext()) {
					int key = iterator.next();
					ArrayList<Integer> itemsInCluster = clusterResult.get(key);
					int cnt = 0;
					double s = 0.0;
					for (Integer item:itemsInCluster) {
						s += simiMatrix[i][item];
						cnt++;
					}
					if (cnt > 0) {
						s /= cnt;
					}
					itemVector[i][c] = s;
					
				}

//			}
		}
		long end = System.currentTimeMillis();
		System.out.println("buildItemVector 2运行时间：" + (end - start) + "毫秒");
	}
	
	
	
//	public void buildUserVectorBySum() {
//		System.out.println("Start to build userVector...");
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < user_num; i++) {
//			double[] tmp = new double[clusterNum];
//			HashMap<Integer, Double> items = trainData.get(i+1);
//			Set<Integer> keys = items.keySet();
//			Iterator<Integer> iterator = keys.iterator();
//			while (iterator.hasNext()) {
//				int key = iterator.next();
//				for (int j = 0; j < clusterNum; j++) {
//					tmp[j] += itemVector[key-1][j];
//				}
//			}
//			userVector[i] = tmp;
//		}
//		long end = System.currentTimeMillis();
//		System.out.println("buildUserVectorBySum 运行时间：" + (end - start) + "毫秒");
//	}
	
	
	public void getRatingMatrix() {
		System.out.println("Start to getRatingMatrix...");
		long start = System.currentTimeMillis();
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < item_num; j++) {
				for (int k = 0; k < clusterNum; k++) {
					ratingMatrix[i][j] += userVector[i][k]*itemVector[j][k];
				}
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("getRatingMatrix 运行时间：" + (end - start) + "毫秒");
	}
	
	
//	public void getRatingMatrixBySVD(int iterNum, double learnRate, double lamda) {
//		double[] bi = new double[item_num];
//		double[] bu = new double[user_num];
//		Random rd = new Random();
////		System.out.println(rd.nextDouble());
//		for (int i = 0; i < user_num; i++) {
//			for (int j = 0; j < clusterNum; j++) {
//				userVector[i][j] = rd.nextDouble();	
//			}
//		}
////		getRatingMatrix();
//		
//		for (int i = 0; i < iterNum; i++) {
//
//			Set<Integer> keys = trainData.keySet();
//			Iterator<Integer> iterator = keys.iterator();
//			while (iterator.hasNext()) {
//				int uid = iterator.next();
//				HashMap<Integer, Double> items = trainData.get(uid);
//				Set<Integer> keys_item = items.keySet();
//				Iterator<Integer> iterator_item = keys_item.iterator();
//				while (iterator_item.hasNext()) {
//					int iid = iterator_item.next();
//					double x = items.get(iid);
//					double prod = 0.0;
//					for (int k = 0; k < clusterNum; k++) {
//						prod += userVector[uid-1][k]*itemVector[iid-1][k];
//					}
//					double y = prod + averg  + bu[uid-1] + bi[iid-1];
//					double eui = x - y;
////					System.out.println("eui = " + eui);
//					
//					bu[uid-1] += learnRate*(eui - lamda*bu[uid-1]);
//					bi[iid-1] += learnRate*(eui - lamda*bi[iid-1]);
//					for (int j = 0; j < clusterNum; j++) {
//						userVector[uid-1][j] += learnRate*(eui*itemVector[iid-1][j] - lamda*userVector[uid-1][j]);
//					}
//					
//				}
//			}
//			learnRate *= 0.9;
//			System.out.println("The test_RMSE in " + (i+1) + "time :---------------------------------------------------------------------");
//			for (int ii = 0; ii < user_num; ii++) {
//				for (int jj = 0; jj < item_num; jj++) {
////					ratingMatrix[ii][jj] = 0.0;
//					double p = 0.0;
//					for (int k = 0; k < clusterNum; k++) {
//						p += (userVector[ii][k]*itemVector[jj][k]);
//					}
//					ratingMatrix[ii][jj] = p + averg + bu[ii] + bi[jj];
//				}
//			}
////			getRatingMatrix();
//			getRMSE();
//		}
//	}
	
	
	/**
	 * Get the items rated by  descending order for user
	 */
	public void sortItemsForUser() {
		System.out.println("Start to sortItemsForUser...");
		long start = System.currentTimeMillis();		
		ArrayList<ArrayList<Double>> sortedItems = new ArrayList<ArrayList<Double>>();
		for (int i = 0; i < user_num; i++) {
			sortedItems.clear();
			for (int j = 0; j < item_num; j++) {
				ArrayList<Double> tmp = new ArrayList<Double>();
				tmp.add(ratingMatrix[i][j]);
				tmp.add((double) (j+1));
				
//				String tmp = String.valueOf(ratingMatrix[i][j]) + "\t" + String.valueOf(j+1);
				sortedItems.add(tmp);
			}
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
			Collections.sort(sortedItems, new Comparator<Object>(){
				public int compare(Object obj1,Object obj2){
			        @SuppressWarnings("unchecked")
					ArrayList<Double> a=(ArrayList<Double>)obj1;
			        @SuppressWarnings("unchecked")
					ArrayList<Double> b=(ArrayList<Double>)obj2;
			        Double v1=a.get(0);
			        Double v2=b.get(0);
			        if(v1==v2){return 0;}
			        if(v1>v2){return 1;}
			        return -1;
			    }
			});
			Collections.reverse(sortedItems);
			ArrayList<Integer> itemIDs = new ArrayList<Integer>();
			for (int j = 0; j < item_num; j++) {
				int id = Integer.parseInt(new java.text.DecimalFormat("0").format(sortedItems.get(j).get(1)));
//				int id = (int)sortedItems.get(j).get(1);
//				String[] info = sortedItems.get(j).split("\t");
				itemIDs.add(id);
			}
			if (i < 20 || i > user_num-10) {
				System.out.println("recommend 1 =   " + itemIDs);
				System.out.println(sortedItems);
			}
			recomItems.add(itemIDs);
		}
		long end = System.currentTimeMillis();
//		System.out.println("recommendation result.......................................");
//		for (int i = 0; i < recomItems.size(); i++) {
//			System.out.println(recomItems.get(i));
//		}
		System.out.println("sortItemsForUser 运行时间：" + (end - start) + "毫秒");
	}
	
    /**
     * 写入文件
    *
     * @param writePath
     * 文件路径
    */
    public static void write(String writePath, ArrayList<Double> list) {
	    try {
		    File f = new File(writePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
			    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
			    BufferedWriter writer = new BufferedWriter(write);
			    int userID = 1;
			    for (Double ele:list) {
			    	String tmp = String.valueOf(userID++) + "," + String.valueOf(ele) + "\r\n";
			    	writer.write(tmp);
			    }
			    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
    }
	
	
//	public void getPreAndRecallAndF() {
//		System.out.println("Start to getPreAndRecallAndF...");
//		long start = System.currentTimeMillis();		
//		int cnt = 0;
//		ArrayList<Double> precison = new ArrayList<Double>();
//		ArrayList<Double> recall = new ArrayList<Double>();
//		ArrayList<Double> f1 = new ArrayList<Double>();
//		for (int i = 0; i < 100; i++) {
//			//i --- recommender num
//			 for (int uid = 1; uid <= user_num; uid++) {
//				 boolean trainHasUser = trainData.containsKey(uid);
//				 boolean testHasUser = testData.containsKey(uid);
//				 int iid = recomItems.get(uid-1).get(i);
//				 if (testHasUser) {
//					 if (((!trainHasUser) || (trainHasUser && (!trainData.get(uid).containsKey(iid)))) 
//							 && testData.get(uid).containsKey(iid)) {
//						 cnt++; 
//					 }
//				 }
////				 for (int j = 0; j < i; j++) {
////					 int iid = recomItems.get(uid).get(j);
////					 if (testHasUser) {
////						 if (((!trainHasUser) || (trainHasUser && (!trainData.get(uid).containsKey(iid)))) && testData.get(uid).containsKey(iid)) {
////							 cnt++;
////						 }
////					 }
////				 }
//			 }
//			 
//			 double pre = (double)cnt/((double)(i+1)*user_num);
//			 double reca = (double)cnt/((double)testEntryNum);
//			 double f = 2*pre*reca/(pre+reca);
//			 precison.add(pre);
//			 recall.add(reca);
//			 f1.add(f);
//		}
//		String filePath = "/home/xv/DataForRecom/saveData/precision.xls";
//		write(filePath, precison);
//		filePath = "/home/xv/DataForRecom/saveData/recall.xls";
//		write(filePath, recall);
//		filePath = "/home/xv/DataForRecom/saveData/f1.xls";
//		write(filePath, f1);
//		long end = System.currentTimeMillis();
//		System.out.println("getPreAndRecallAndF 运行时间：" + (end - start) + "毫秒");
//	}
    
    public void getPreAndRecallAndF() {
		System.out.println("Start to getPreAndRecallAndF...");
		long start = System.currentTimeMillis();		
		ArrayList<Double> precison = new ArrayList<Double>();
		ArrayList<Double> recall = new ArrayList<Double>();
		ArrayList<Double> f1 = new ArrayList<Double>();
		for (int j = 1; j <= 100; j++) {
			//i --- recommender num
//			System.out.println("top = " + j);
			if (j == 1) {
				System.out.println("recommd = " + recomItems.get(0));
			}
			
			int cnt = 0;
			 for (int uid = 1; uid <= user_num; uid++) {
				 boolean trainHasUser = trainData.containsKey(uid);
				 boolean testHasUser = testData.containsKey(uid);
				 int i = -1;
//				 if (uid == 1) {
//					 System.out.println("----------------------------------------------------------------------------------------------------");
//				 }
				 int c = 0;
				 while (c< j && i < recomItems.get(uid-1).size()) {
					 i++;
					 int iid = recomItems.get(uid-1).get(i);
//					 if (uid == 1) {
//						 System.out.println("iid = " + iid);
//					 }
					 if (trainHasUser && trainData.get(uid).containsKey(iid)) {
						 continue;
					 }
					 if (testHasUser &&testData.get(uid).containsKey(iid) ) {
//						 if (((!trainHasUser) || (trainHasUser && (!trainData.get(uid).containsKey(iid)))) 
//								 && testData.get(uid).containsKey(iid)) {
//							 cnt++; 
//						 }
						 cnt++;
					 }
					 c++;
//					 System.out.println("i = " + i);
				 }
				 
//				 for (int j = 0; j < i; j++) {
//					 int iid = recomItems.get(uid).get(j);
//					 if (testHasUser) {
//						 if (((!trainHasUser) || (trainHasUser && (!trainData.get(uid).containsKey(iid)))) && testData.get(uid).containsKey(iid)) {
//							 cnt++;
//						 }
//					 }
//				 }
			 }
			 if (j < 6) {
				 System.out.println("cnt = " + cnt);
				 System.out.println("(j)*user_num =  " + (j)*user_num);
				 System.out.println("testEntryNum =  " + testEntryNum);
			 }
			 double pre = (double)cnt/((double)(j)*user_num);
			 double reca = (double)cnt/((double)testEntryNum);
			 double f = 2*pre*reca/(pre+reca);
			 precison.add(pre);
			 recall.add(reca);
			 f1.add(f);
		}
		String filePath = "/home/xv/DataForRecom/saveData/precisionKNN.xls";
		write(filePath, precison);
		filePath = "/home/xv/DataForRecom/saveData/recallKNN.xls";
		write(filePath, recall);
		filePath = "/home/xv/DataForRecom/saveData/f1KNN.xls";
		write(filePath, f1);
		long end = System.currentTimeMillis();
		System.out.println("getPreAndRecallAndF 运行时间：" + (end - start) + "毫秒");
	}
	
	
	public void buildUserVectorBySum() {
		System.out.println("Start to build userVector...");
		long start = System.currentTimeMillis();
		for (int i = 0; i < user_num; i++) {
			double[] tmp = new double[clusterNum];
			HashMap<Integer, Double> items = trainData.get(i+1);
			Set<Integer> keys = items.keySet();
			Iterator<Integer> iterator = keys.iterator();
			while (iterator.hasNext()) {
				int key = iterator.next();
				for (int j = 0; j < clusterNum; j++) {
					tmp[j] += itemVector[key-1][j];
				}
			}
			userVector[i] = tmp;
		}
		long end = System.currentTimeMillis();
		System.out.println("buildUserVectorBySum 运行时间：" + (end - start) + "毫秒");
	}
	
	public void buildUserVectorBySumAll() {
		System.out.println("Start to build userVector...");
		long start = System.currentTimeMillis();
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < item_num; j++) {
				for (int k = 0; k < clusterNum; k++) {

					if (trainData.containsKey(i+1)) {
						if (trainData.get(i+1).containsKey(j+1)) {
							userVector[i][k] += 0.7*upmat[i][j]*itemVector[j][k];
						} else {
							userVector[i][k] += 0.3*upmat[i][j]*itemVector[j][k];
						}
					}
					
//					if (upmat[i][j] > 0) {
//						userVector[i][k] += upmat[i][j]*itemVector[j][k];
//					}
					
					userVector[i][k] += upmat[i][j]*itemVector[j][k];
				}
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("buildUserVectorBySum 运行时间：" + (end - start) + "毫秒");
	}
	
	
//	public void getRatingMatrix() {
//		System.out.println("Start to getRatingMatrix...");
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < user_num; i++) {
//			for (int j = 0; j < item_num; j++) {
//				for (int k = 0; k < clusterNum; k++) {
//					ratingMatrix[i][j] += userVector[i][k]*itemVector[j][k];
//				}
//			}
//		}
//		long end = System.currentTimeMillis();
//		System.out.println("getRatingMatrix 运行时间：" + (end - start) + "毫秒");
//	}
	
	
	
	public void getRMSE() {
		System.out.println("Start to calculate rmse...");
		double rmse = 0.0;
		int testNum = 20000;
		Set<Integer> keys = testData.keySet();
		Iterator<Integer> iterator = keys.iterator();
		while (iterator.hasNext()) {
			int key = iterator.next();
			HashMap<Integer, Double> items = testData.get(key);
			Set<Integer> keys_item = items.keySet();
			Iterator<Integer> iterator_item = keys_item.iterator();
			while (iterator_item.hasNext()) {
				int key_item = iterator_item.next();
				double x = items.get(key_item);
				double y = ratingMatrix[key-1][key_item-1];
				rmse += (x-y)*(x-y);
			}
		}
		rmse /= testNum;
		rmse = Math.sqrt(rmse);
		System.out.println("RMSE = " + rmse);
	}
}