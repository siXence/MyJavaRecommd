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
import java.util.Set;

import org.apache.mahout.cf.taste.common.TasteException;

/**
 * @author xv
 *
 */
public class DirectComputeUserVector extends MethodBasedOnSimilarityTW{
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
		
//		fillMissingProg();
		computeItemAverage();

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
		
		buildMultiItemVector();		
//		buildMultiItemVector2();		
//		buildMultiItemVector3();
//		buildSingleItemVector();
		
//		buildUserVectorBySum();
		buildUserVectorBySumAll();
		getRatingMatrix();
		
		saveUPrec();
		
		
		
//		getRatingMatrixBySVD(50, 0.5, 0.01);
//		getRatingMatrixBySVDAllItems(50, 0.5, 0.01);
		
		sortItemsForUser();
		
		
//		updateTrainTestSetFromFile() ;
		
		
		getPreAndRecallAndF();
		
		System.out.println("cluster.size() = " + clusterResult.size());
		long end = System.currentTimeMillis();
		System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
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
