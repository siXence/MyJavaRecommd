/**
 * 
 */
package handleTWData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.apache.mahout.cf.taste.common.TasteException;

/**
 * @author xv
 *
 */
public class DirectComputeUserVector extends MethodBasedOnSimilarityTW{
	
	public void getRatingMatrixBySVD(int iterNum, double learnRate, double lamda) {
		double[] bi = new double[item_num];
		double[] bu = new double[user_num];
		Random rd = new Random();
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < clusterNum; j++) {
				userVector[i][j] = rd.nextDouble();	
			}
		}
		
		for (int i = 0; i < iterNum; i++) {
			Set<Integer> keys = trainData.keySet();
			Iterator<Integer> iterator = keys.iterator();
			while (iterator.hasNext()) {
				int uid = iterator.next();
				HashMap<Integer, Double> items = trainData.get(uid);
				Set<Integer> keys_item = items.keySet();
				Iterator<Integer> iterator_item = keys_item.iterator();
				while (iterator_item.hasNext()) {
					int iid = iterator_item.next();
					double x = items.get(iid);
					double prod = 0.0;
					for (int k = 0; k < clusterNum; k++) {
						prod += userVector[uid-1][k]*itemVector[iid-1][k];
					}
					double y = prod + averg  + bu[uid-1] + bi[iid-1];
					double eui = x - y;
					bu[uid-1] += learnRate*(eui - lamda*bu[uid-1]);
					bi[iid-1] += learnRate*(eui - lamda*bi[iid-1]);
					for (int j = 0; j < clusterNum; j++) {
						userVector[uid-1][j] += learnRate*(eui*itemVector[iid-1][j] - lamda*userVector[uid-1][j]);
					}
				}
			}
			learnRate *= 0.9;
			System.out.println("The test_RMSE in " + (i+1) + "time :---------------------------------------------------------------------");
			for (int ii = 0; ii < user_num; ii++) {
				for (int jj = 0; jj < item_num; jj++) {
					double p = 0.0;
					for (int k = 0; k < clusterNum; k++) {
						p += (userVector[ii][k]*itemVector[jj][k]);
					}
					ratingMatrix[ii][jj] = p + averg + bu[ii] + bi[jj];
				}
			}
			getRMSE();
		}
	}
	
	
	public void getRatingMatrixBySVDAllItems(int iterNum, double learnRate, double lamda) {
		double[] bi = new double[item_num];
		double[] bu = new double[user_num];
		Random rd = new Random();
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < clusterNum; j++) {
				userVector[i][j] = rd.nextDouble();	
			}
		}
		
		for (int i = 0; i < iterNum; i++) {
			for (int uid = 1; uid <= user_num; uid++) {
				
				for (int iid = 1; iid <= item_num; iid++) {
					double x = upmat[uid-1][iid-1];
					double prod = 0.0;
					for (int k = 0; k < clusterNum; k++) {
						prod += userVector[uid-1][k]*itemVector[iid-1][k];
					}
					double y = prod + averg  + bu[uid-1] + bi[iid-1];
					double eui = x - y;
					bu[uid-1] += learnRate*(eui - lamda*bu[uid-1]);
					bi[iid-1] += learnRate*(eui - lamda*bi[iid-1]);
					for (int j = 0; j < clusterNum; j++) {
						userVector[uid-1][j] += learnRate*(eui*itemVector[iid-1][j] - lamda*userVector[uid-1][j]);
					}
				}
			}
			
			learnRate *= 0.9;
			System.out.println("The test_RMSE in " + (i+1) + "time :---------------------------------------------------------------------");
			for (int ii = 0; ii < user_num; ii++) {
				for (int jj = 0; jj < item_num; jj++) {
					double p = 0.0;
					for (int k = 0; k < clusterNum; k++) {
						p += (userVector[ii][k]*itemVector[jj][k]);
					}
					ratingMatrix[ii][jj] = p + averg + bu[ii] + bi[jj];
				}
			}
			getRMSE();
			
		}
	}
	
	
	
	
	public void writeSmoothData(String filePath) {
		Random rd = new Random();
		String fileTrain= "/home/xv/DataForRecom/saveData/smoothDataTrain.csv";
		String fileTest= "/home/xv/DataForRecom/saveData/smoothDataTest.csv";
		try {
		    File fTrain = new File(fileTrain);
		    if (!fTrain.exists()) {
		    	fTrain.createNewFile();
		    }
		    OutputStreamWriter writeTrain = new OutputStreamWriter(new FileOutputStream(fTrain),"UTF-8");
		    BufferedWriter writerTrain = new BufferedWriter(writeTrain);
		    
		    File fTest = new File(fileTest);
		    if (!fTest.exists()) {
		    	fTest.createNewFile();
		    }
		    OutputStreamWriter writeTest = new OutputStreamWriter(new FileOutputStream(fTest),"UTF-8");
		    BufferedWriter writerTest = new BufferedWriter(writeTest);
		    for (int i = 0; i < user_num; i++) {
		    	for (int j = 1; j < item_num; j++) {
		    		
		    		if (upmat[i][j] > 0.0001) {
		    			String tmp =  String.valueOf(i+1) + "," + String.valueOf(j+1) + ","  +String.valueOf(upmat[i][j] + "\n");
		    			if (rd.nextDouble() > 0.2) {
		    				writerTrain.write(tmp);
		    			} else {
		    				writerTest.write(tmp);
		    			}
//			    		writer.write(tmp);
		    		}
		    		
		    	}
		    }
		    
		    writerTrain.close();
		    writerTest.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	
	public void ourMethod() throws IOException, TasteException {
		System.out.println("Starting...");
		long start = System.currentTimeMillis();
		String filePath = "/home/xv/DataForRecom/saveData/ua.base";
		
		initRatingMatrix();
		
//		trainData = getData(filePath);
		getTrainData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/ua.test";
		System.out.println("contain = " + trainData.containsKey(13));
		
//		testData = getData(filePath);
		getTestData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/simiMatrixTW.txt";
		
		fillMissingProg();
		computeItemAverage();
		
//		String testFile =  "/home/xv/DataForRecom/saveData/smoothData.csv";
//		writeSmoothData(testFile);

		String trainFile = "/home/xv/DataForRecom/saveData/ua.base";
//		computeCityBlockSimilarity(trainFile);
		
		filePath = "/home/xv/DataForRecom/saveData/simiMatrixByHandFill.txt";
//		writeSimiMatrixIntoFile(filePath);
//		
//		computeSimilary();
//		reComputeItemSim();
		
		computeSimilaryAllItems() ;
		
//		readSimi() ;
		
		writeSimiMatrixIntoFile(filePath);
		
//		readSimiMatrixFile(filePath);
		
		clustering(clusterNum);
		
//		saveClusterResult() ;
//		getClustersCenters();
		
//		buildSingleItemVector();
		
//		buildMultiItemVector();		
//		savePCmat() ;
//		buildMultiItemVector2();		
//		buildMultiItemVector3();
		buildSingleItemVector();
		
//		buildUserVectorBySum();
		
		buildUserVectorBySumAll();
		getRatingMatrix();
//		
//		saveUPrec();
//		
		
		
//		getRatingMatrixBySVD(30, 0.05, 0.01);
//		getRatingMatrixBySVDAllItems(30, 0.05, 0.01);
		
		sortItemsForUser();
		
		
//		updateTrainTestSetFromFile() ;
		
		
		getPreAndRecallAndF();
		
		System.out.println("cluster.size() = " + clusterResult.size());
		long end = System.currentTimeMillis();
		System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
//		testCnt();
	}
	
	
	public void testCnt() {
		int cnt = 0;
		for (int i = 0; i < item_num; i++) {
			if (testRec[i] != testRec2[i]) {
				cnt++;
			}
		}
		System.out.println("the ------------------------------>" + cnt);
	}
	
	
	
	
	public void saveUPrec() {
//		String filePath = "/home/xv/DataForRecom/saveData/singelUPrec.txt";
		String filePath = "/home/xv/DataForRecom/saveData/multiUPrec.txt";
		try {
		    File f = new File(filePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		    BufferedWriter writer = new BufferedWriter(write);
		    for (int i = 0; i < user_num; i++) {
		    	String tmp = String.valueOf(ratingMatrix[i][0]);
		    	for (int j = 1; j < item_num; j++) {
		    		tmp += "\t" + String.valueOf(ratingMatrix[i][j]);
		    	}
		    	tmp += "\n";
		    	writer.write(tmp);
		    }
		    
		    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	public void savePCmat() {
//		String filePath = "/home/xv/DataForRecom/saveData/singelUPrec.txt";
		String filePath = "/home/xv/DataForRecom/saveData/pcmat.txt";
		try {
		    File f = new File(filePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
		    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		    BufferedWriter writer = new BufferedWriter(write);
		    for (int i = 0; i < item_num; i++) {
		    	String tmp = String.valueOf(itemVector[i][0]);
		    	for (int j = 1; j < clusterNum; j++) {
		    		tmp += "\t" + String.valueOf(itemVector[i][j]);
		    	}
		    	tmp += "\n";
		    	writer.write(tmp);
		    }
		    
		    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	public void saveClusterResult() {
		String writePath = "/home/xv/DataForRecom/saveData/clusterResult.txt";
		try {
		    File f = new File(writePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
			    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
			    BufferedWriter writer = new BufferedWriter(write);
			    
			    Set<Integer> keys = clusterResult.keySet();
				Iterator<Integer> iterator = keys.iterator();
				while (iterator.hasNext()) {
					int key = iterator.next();
					ArrayList<Integer> al = clusterResult.get(key);
					String tmp = String.valueOf(al.get(0));
					for (int i = 1; i < al.size(); i++) {
						tmp += "\t" + al.get(i);
					}
					writer.write(tmp + "\n");
				}
			    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
	}
	
	
	public void getClustersCenters() {
		String filePath = "/home/xv/DataForRecom/saveData/centers.txt";
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){

//                    System.out.println("tmp.size() = " + tmp.length);
                    clusterResult.put(Integer.parseInt(lineTxt), new ArrayList<Integer>());
                    
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
	
	public void updateTrainTestSetFromFile() {
		String trainSet = "/home/xv/DataForRecom/saveData/my_trainset1.txt";
		String testSet = "/home/xv/DataForRecom/saveData/my_testset1.txt";
		trainData.clear();
		testData.clear();
		
		try {
            String encoding="GBK";
            File file=new File(trainSet);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int uid = 1;
                while((lineTxt = bufferedReader.readLine()) != null){
                	trainData.put(uid, new HashMap<Integer, Double>());
                    String[] tmp = lineTxt.split(" ");
//                    System.out.println("tmp.size() = " + tmp.length);
                    
                    for (int i = 0; i < tmp.length; i++) {
                    	if (tmp[i].compareTo("1") == 0) {
                    		trainData.get(uid).put(i+1, 1.0);
                    	}
                    }
                    
                    uid++;
                    
                }
                read.close();
	    }else{
	        System.out.println("找不到指定的文件");
	    }
    } catch (Exception e) {
        System.out.println("读取文件内容出错");
        e.printStackTrace();
    }
		
		try {
            String encoding="GBK";
            File file=new File(testSet);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int uid = 1;
                while((lineTxt = bufferedReader.readLine()) != null){
                	testData.put(uid, new HashMap<Integer, Double>());
                    String[] tmp = lineTxt.split(" ");
//                    System.out.println("tmp.size() = " + tmp.length);
                    
                    for (int i = 0; i < tmp.length; i++) {
                    	if (tmp[i].compareTo("1") == 0) {
                    		testData.get(uid).put(i+1, 1.0);
                    	}
                    }
                    
                    uid++;
                    
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
	
//	public void readUpmat() {
//		String filePath = "/home/xv/DataForRecom/saveData/my_upmat1.txt";
//		try {
//            String encoding="GBK";
//            File file=new File(filePath);
//            if(file.isFile() && file.exists()){ //判断文件是否存在
//                InputStreamReader read = new InputStreamReader(
//                new FileInputStream(file),encoding);//考虑到编码格式
//                BufferedReader bufferedReader = new BufferedReader(read);
//                String lineTxt = null;
//                int uid = 1;
//                while((lineTxt = bufferedReader.readLine()) != null){
//                	trainData.put(uid, new HashMap<Integer, Double>());
//                    String[] tmp = lineTxt.split(" ");
////                    System.out.println("tmp.size() = " + tmp.length);
//                    
//                    for (int i = 0; i < tmp.length; i++) {
//                    	if ()
//                    	simiMatrix[uid][i] = Double.valueOf(tmp[i]);
//                    }
//                    
//                    uid++;
//                    
//                }
//                read.close();
//	    }else{
//	        System.out.println("找不到指定的文件");
//	    }
//    } catch (Exception e) {
//        System.out.println("读取文件内容出错");
//        e.printStackTrace();
//    }
//	}
	
	public void readSimi() {
		String filePath = "/home/xv/DataForRecom/saveData/my_dismat2_tan1.txt";
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int uid = 0;
                while((lineTxt = bufferedReader.readLine()) != null){
                    String[] tmp = lineTxt.split(" ");
//                    System.out.println("tmp.size() = " + tmp.length);
                    
                    for (int i = 0; i < tmp.length; i++) {
                    	simiMatrix[uid][i] = Double.valueOf(tmp[i]);
                    }
                    
                    uid++;
                    
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
	
	public void getPreAndRecall() {
		
	}
	
	public static void main(String[] args) throws Exception {
		DirectComputeUserVector dh = new DirectComputeUserVector();
		dh.ourMethod();
		System.out.println("Done");
		
		
//		ArrayList<ArrayList<Integer>> tmp = new ArrayList<ArrayList<Integer>>();
//		ArrayList<Integer> t = new ArrayList<Integer>();
//		t.add(10);
//		t.add(5);
//		tmp.add(t);
//		ArrayList<Integer> t1 = new ArrayList<Integer>();
//		t1.add(4);
//		t1.add(2);
//		tmp.add(t1);
//		ArrayList<Integer> t2 = new ArrayList<Integer>();
//		t2.add(2);
//		t2.add(3);
//		tmp.add(t2);
//		Collections.sort(tmp, new Comparator<Object>(){
//				public int compare(Object obj1,Object obj2){
//			        @SuppressWarnings("unchecked")
//					ArrayList<Integer> a=(ArrayList<Integer>)obj1;
//			        @SuppressWarnings("unchecked")
//					ArrayList<Integer> b=(ArrayList<Integer>)obj2;
//			        int v1=a.get(0);
//			        int v2=b.get(0);
//			        if(v1==v2){return 0;}
//			        if(v1>v2){return 1;}
//			        return -1;
//			    }
//			});
//		Collections.reverse(tmp);
//		System.out.println(tmp);
		
		
//		List<String> tmp = new ArrayList<String>();
//		String a = "4.12340" + "\t" + "10";
//		tmp.add(a);
//		a = "0.1234" + "\t" + "3";
//		tmp.add(a);
//		a = "3.844" + "\t" + "7";
//		tmp.add(a);
//		Collections.sort(tmp);
//		Collections.reverse(tmp);
//		for (int i = 0; i < tmp.size(); i++) {
//			System.out.println(tmp.get(i));
//			String[] st = tmp.get(i).split("\t");
//			System.out.println(st[1]);
//		}
		
		
		
//		int [][] tt = new int[2][2];
//		int [] t = new int[2];
//		t[0] = 2;
//		t[1] = 3;
//		tt[0] = t;
//		System.out.println(tt[0][0] + "   " + tt[0][1]);
//		System.out.println(tt[1][0] + "   " + tt[1][1]);
		
		
	}

}
