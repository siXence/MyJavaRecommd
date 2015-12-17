/**
 * Based on distance to cluster items
 * Item vector is multi-dimension
 * Using the thought of SVD to build rating matrix 
 */
package dataHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * @author xv
 *
 */
public class ClusterSVDDist extends MyRecom{
	
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
				simiMatrix[i-1][j-1] = d;
			}
		}
//		System.out.println("simiMatrix = " + simiMatrix);
		long end = System.currentTimeMillis();
		System.out.println("computeSimilary 运行时间：" + (end - start) + "毫秒");
		System.out.println("Already get the similarity matrix.");
	}
	
	
	public void clustering(int K) {
		System.out.println("50 = " + simiMatrix[50][50]);
		long start = System.currentTimeMillis();
		System.out.println("Start to cluster...");
		ArrayList<Integer> itemList = new ArrayList<Integer>();
		for (int i = 0; i < item_num; i++) {
			itemList.add(i);
		}
		
		while (true) {
			int x = 0;
			int y = 0;
			double sim =0.0;
			for (int i = 0; i < itemList.size(); i++) {
				int iTerm = itemList.get(i);
				for (int j = i+1; j < itemList.size(); j++) {
					int jTerm = itemList.get(j);
					double s = getSim(iTerm, jTerm);
					if (s > sim) {
						sim = s;
						x = iTerm;
						y = jTerm;
					}
				}
			}
			clusterResult.put(x, new ArrayList<Integer>());
			clusterResult.put(y, new ArrayList<Integer>());
//			System.out.println("x = " + x);
//			System.out.println("y = " + y);
//			System.out.println("clusterResult size() 1= " + clusterResult.size());
			for (Integer id:itemList) {
				if (getSim(x, id) <=  getSim(y, id)) {
					clusterResult.get(x).add(id);
				} else {
					clusterResult.get(y).add(id);
				}
			}
			
			if (clusterResult.size() == K) {
				break;
			}
			
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
//		System.out.println("clusterResult = " + clusterResult);
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
		
//		try {
//            String encoding="GBK";
//            String filePath = "/home/xv/DataForRecom/saveData/centers.txt";
//            File file=new File(filePath);
//            if(file.isFile() && file.exists()){ //判断文件是否存在
//                InputStreamReader read = new InputStreamReader(
//                new FileInputStream(file),encoding);//考虑到编码格式
//                BufferedReader bufferedReader = new BufferedReader(read);
//                String lineTxt = null;
//                int idx = 0;
//                while((lineTxt = bufferedReader.readLine()) != null){
////                    String[] tmp = lineTxt.split("\t");
////                    System.out.println("lineTxt = " + lineTxt);
//                    centers[idx++] = Integer.parseInt(lineTxt);
//                }
//                read.close();
//		    }else{
//		        System.out.println("找不到指定的文件");
//		    }
//	    } catch (Exception e) {
//	        System.out.println("读取文件内容出错");
//	        e.printStackTrace();
//	    }
		
		for (int i = 0; i < item_num; i++) {
			double sum = 0.0;
			for (int j = 0; j < clusterNum; j++) {
				double tmp = getSim(i, centers[j]);
//				if (i == 1460 && j == 198) {
//					System.out.println("i = " + i);
//					System.out.println("center = " + centers[j]);
//					System.out.println("dis = "  + tmp);
//				}
				
//				if (tmp == 0) {
////					itemVector[i][j] = 0.00000001;
//					tmp = 0.00000001;
//				} 
//				tmp =(double) 1/tmp;
//				sum += tmp;
//				itemVector[i][j] = tmp;
				
				if (i == centers[j]) {
					tmp = 0.0000001;
				}
				
				if (tmp == 0.0) {
//					itemVector[i][j] = 0.00000001;
					tmp = 0.0000001;
					itemVector[i][j] = tmp;
				} 
				tmp =(double) 1/tmp;
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
	
	public void getRatingMatrixBySVD(int iterNum, double learnRate, double lamda) {
//		for (int i = 0; i < item_num; i++) {
//			for (int j = 0; j < clusterNum; j++) {
//				System.out.println("itemVector =  i =  " + i + " , j = " + j + "    " + itemVector[i][j]);
//			}
//		}
		
		
		
		double[] bi = new double[item_num];
		double[] bu = new double[user_num];
		Random rd = new Random();
//		System.out.println(rd.nextDouble());
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < clusterNum; j++) {
				userVector[i][j] = rd.nextDouble();	
			}
		}
//		getRatingMatrix();
		
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
//						System.out.println("prod = " + prod);
					}
//					System.out.println("prod = " + prod);
					double y = prod + averg  + bu[uid-1] + bi[iid-1];
					double eui = x - y;
//					System.out.println("eui = " + eui);
//					System.out.println("bu = " + bu[uid-1]);
//					System.out.println("bi = " + bi[iid-1]);
					
					bu[uid-1] += learnRate*(eui - lamda*bu[uid-1]);
					bi[iid-1] += learnRate*(eui - lamda*bi[iid-1]);

//					System.out.println("bu = " + bu[uid-1]);
//					System.out.println("bi = " + bi[iid-1]);
					for (int j = 0; j < clusterNum; j++) {
						userVector[uid-1][j] += learnRate*(eui*itemVector[iid-1][j] - lamda*userVector[uid-1][j]);
//						System.out.println("userVector[uid-1][j] = " + userVector[uid-1][j]);
					}
					
				}
			}
			learnRate *= 0.9;
			System.out.println("The test_RMSE in " + (i+1) + "time :---------------------------------------------------------------------");
			for (int ii = 0; ii < user_num; ii++) {
				for (int jj = 0; jj < item_num; jj++) {
//					ratingMatrix[ii][jj] = 0.0;
					double p = 0.0;
					for (int k = 0; k < clusterNum; k++) {
						p += (userVector[ii][k]*itemVector[jj][k]);
					}
					ratingMatrix[ii][jj] = p + averg + bu[ii] + bi[jj];
				}
			}
//			getRatingMatrix();
			getRMSE();
		}
	}
	
	
	
	public void ourMethod() {
		System.out.println("Starting...");
		long start = System.currentTimeMillis();
		String filePath = "/home/xv/DataForRecom/ml-100k/u1.base";
		trainData = getData(filePath);
		filePath = "/home/xv/DataForRecom/ml-100k/u1.test";
		testData = getData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/simiMatrix.txt";
		
		
//		computeSimilary();
//		writeSimiMatrixIntoFile(filePath);
		
		readSimiMatrixFile(filePath);
		
		clustering(clusterNum);
		buildMultiItemVector();		
		
//		buildUserVectorBySum();
//		getRatingMatrix();
//		getRMSE();
		
		getRatingMatrixBySVD(50, 0.01, 0.01);
		
		System.out.println("cluster.size() = " + clusterResult.size());
		long end = System.currentTimeMillis();
		System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
	}
	
	public static void main(String[] args) throws Exception {
		ClusterSVDDist dh = new ClusterSVDDist();
		dh.ourMethod();

		
		
//		int [][] tt = new int[2][2];
//		int [] t = new int[2];
//		t[0] = 2;
//		t[1] = 3;
//		tt[0] = t;
//		System.out.println(tt[0][0] + "   " + tt[0][1]);
//		System.out.println(tt[1][0] + "   " + tt[1][1]);
		
		
	}
	
}
