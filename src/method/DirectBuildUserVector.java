/**
 * According to itemVector, to build userVector directly
 * 
 */
package method;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import dataHandle.DataHandle;

/**
 * @author xv
 *
 */
public class DirectBuildUserVector extends MethodBasedOnSimilarity{
	
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
		buildUserVectorBySum();
		getRatingMatrix();
		getRMSE();
		
		
		System.out.println("cluster.size() = " + clusterResult.size());
		long end = System.currentTimeMillis();
		System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
	}
	
	
	
	public static void main(String[] args) throws Exception {
		DirectBuildUserVector dh = new DirectBuildUserVector();
		dh.ourMethod();

	
		
		
	}

}
