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
	protected final int clusterNum = 300;
	protected final double averg = 0.5016;
	
//	protected final int user_num = 943;
//	protected final int item_num = 1682;
//	protected final int clusterNum = 100;
//	protected final double averg = 3;
	
//	protected final int user_num = 4000;
//	protected final int item_num = 3000;
//	protected final int clusterNum = 300;
//	protected final double averg = 0.7898;
	
	protected HashMap<Integer, HashMap<Integer, Double> > trainData = new HashMap<Integer, HashMap<Integer, Double> >();
	protected HashMap<Integer, HashMap<Integer, Double> > testData = new HashMap<Integer, HashMap<Integer, Double> >();
	protected HashMap<Integer, Integer> itemCnt = new HashMap<Integer, Integer>();
	//	protected int[][] trainData = new int[user_num][item_num];
//	protected int[][] testData = new int[user_num][item_num];
//	protected double[][] simiMatrix = new double[item_num][item_num];
	protected double[][] simiMatrix = new double[item_num][item_num];
	
	protected HashMap<Integer, ArrayList<Integer> > clusterResult = new HashMap<Integer, ArrayList<Integer>>();
	protected double[][] itemVector = new double[item_num][clusterNum];
	protected double[][] userVector = new double[user_num][clusterNum];
	protected double[][] ratingMatrix = new double[user_num][item_num];
	
	protected double[][] upmat = new double[user_num][item_num];
	
	private int testEntryNum = 0;
	
	protected ArrayList<ArrayList<Integer>> recomItems = new ArrayList<ArrayList<Integer>>();
	
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
			System.out.println("The " + (i) + " item...");
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
                    trainData.get(uid).put(iid, rating);
                    
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
	
	
	
	public double getSim(int x, int y) {
		if (x <= y) {
			return simiMatrix[x][y];
		}
		return simiMatrix[y][x];
	}
	
	
	public void clustering(int K) {
		long start = System.currentTimeMillis();
		System.out.println("Start to cluster...");
		ArrayList<Integer> itemList = new ArrayList<Integer>();
		for (int i = 0; i < item_num; i++) {
			itemList.add(i);
		}
		
		for (int k = 0; k < K; k++) {
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
//			System.out.println("x = " + x);
//			System.out.println("y = " + y);
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
//			System.out.println("x = " + x);
//			System.out.println(clusterResult.get(x));
//			System.out.println("y = " + y);
//			System.out.println(clusterResult.get(y));
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
			clusterResult.remove(centerWithMostItems);

		}
		long end = System.currentTimeMillis();
		System.out.println("clustering 运行时间：" + (end - start) + "毫秒");
		System.out.println("cluster  = " + clusterResult);
		System.out.println("cluster.size()  = " + clusterResult.size());
	}
	
	
	
	/**
	 * multi-dimension
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
		for (int i = 0; i < item_num; i++) {
			double sum = 0.0;
			for (int j = 0; j < clusterNum; j++) {
				double tmp = getSim(i, centers[j]);
//				if (!Double.isNaN(tmp)) {
//					System.out.println("the simi " + i + "and " + j + " is not NaN");
//				}
				sum += tmp;
				itemVector[i][j] = tmp;
			}
			for (int j = 0; j < clusterNum; j++) {
				itemVector[i][j] /= sum;
				if (itemVector[i][j] < 0.01) {
					itemVector[i][j] = 0.0;
				}
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("buildItemVector 运行时间：" + (end - start) + "毫秒");
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
		ArrayList<String> sortedItems = new ArrayList<String>();
		for (int i = 0; i < user_num; i++) {
			sortedItems.clear();
			for (int j = 0; j < item_num; j++) {
				String tmp = String.valueOf(ratingMatrix[i][j]) + "\t" + String.valueOf(j+1);
				sortedItems.add(tmp);
			}
			Collections.sort(sortedItems);
			Collections.reverse(sortedItems);
			ArrayList<Integer> itemIDs = new ArrayList<Integer>();
			for (int j = 0; j < item_num; j++) {
				String[] info = sortedItems.get(j).split("\t");
				itemIDs.add(Integer.parseInt(info[1]));
			}
			if (i == 0) {
				System.out.println("recommend 1 =   " + itemIDs);
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
			System.out.println("top = " + j);
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
			 
			 double pre = (double)cnt/((double)(j)*user_num);
			 double reca = (double)cnt/((double)testEntryNum);
			 double f = 2*pre*reca/(pre+reca);
			 precison.add(pre);
			 recall.add(reca);
			 f1.add(f);
		}
		String filePath = "/home/xv/DataForRecom/saveData/precision.xls";
		write(filePath, precison);
		filePath = "/home/xv/DataForRecom/saveData/recall.xls";
		write(filePath, recall);
		filePath = "/home/xv/DataForRecom/saveData/f1.xls";
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