package dataHandle;

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


public class DataHandle {
	
	private final int user_num = 943;
	private final int item_num = 1682;
	private final int clusterNum = 200;
	private HashMap<Integer, HashMap<Integer, Double> > trainData = new HashMap<Integer, HashMap<Integer, Double> >();
	private HashMap<Integer, HashMap<Integer, Double> > testData = new HashMap<Integer, HashMap<Integer, Double> >();
//	private int[][] trainData = new int[user_num][item_num];
//	private int[][] testData = new int[user_num][item_num];
	private double[][] simiMatrix = new double[item_num][item_num];
	
	private HashMap<Integer, ArrayList<Integer> > clusterResult = new HashMap<Integer, ArrayList<Integer>>();
	private double[][] itemVector = new double[item_num][clusterNum];
	private double[][] userVector = new double[user_num][clusterNum];
	
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
//			System.out.println("The " + (i+1) + " times...");
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
		System.out.println("50 = " + simiMatrix[50][50]);
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
//			System.out.println("clusterResult = " + clusterResult);
//			System.out.println("x= " + x);
//			System.out.println("y= " + y);
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
			
//			System.out.println("1 " + clusterResult.get(x).size());
//			System.out.println("1  = " + clusterResult.get(x));
//			System.out.println("2 " + clusterResult.get(y).size());
//			System.out.println("2 = " + clusterResult.get(y));
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
//			System.out.println("itemList.size() = " + itemList.size());
//			System.out.println("itemList = " + itemList);
//			System.out.println("clusterResult size() 2= " + clusterResult.size());
			if ( k == 2) {
				int tt = 0;
				tt += 1;
			}

		}
		long end = System.currentTimeMillis();
		System.out.println("clustering 运行时间：" + (end - start) + "毫秒");
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
				sum += tmp;
				itemVector[i][j] = tmp;
			}
			for (int j = 0; j < clusterNum; j++) {
				itemVector[i][j] /= sum;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("buildItemVector 运行时间：" + (end - start) + "毫秒");
	}
	
	public void buildUserVectorBySum() {
		
	}
	
	
	
	
	
	public void ourMethod() {
		String filePath = "/home/xv/DataForRecom/ml-100k/u1.base";
		trainData = getData(filePath);
		filePath = "/home/xv/DataForRecom/ml-100k/u1.base";
		testData = getData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/simiMatrix.txt";
		
//		dh.computeSimilary();
//		dh.writeSimiMatrixIntoFile(filePath);
		
		readSimiMatrixFile(filePath);
		clustering(clusterNum);
		buildMultiItemVector();
	}
	
	
	
	public static void main(String[] args) throws Exception {
		DataHandle dh = new DataHandle();
		dh.ourMethod();
		
		System.out.println("cluster.size() = " + dh.clusterResult.size());
	}
	
	
}
