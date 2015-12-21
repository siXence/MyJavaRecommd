/**
 * 
 */
package handleTWData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;


/**
 * @author xv
 *
 */
public class SVD extends MethodBasedOnSimilarityTW{
	public void getRatingMatrixBySVD(int iterNum, double learnRate, double lamda) {
		double[] bi = new double[item_num];
		double[] bu = new double[user_num];
		Random rd = new Random();
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < clusterNum; j++) {
				userVector[i][j] = rd.nextDouble();	
				itemVector[i][j] = rd.nextDouble();
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
						double tmp = userVector[uid-1][j];
						userVector[uid-1][j] += learnRate*(eui*itemVector[iid-1][j] - lamda*userVector[uid-1][j]);
						itemVector[iid-1][j] += learnRate*(eui*tmp - lamda*itemVector[iid-1][j]);
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
	
	
	
	public void ourMethod() {
		System.out.println("Starting...");
		long start = System.currentTimeMillis();
		String filePath = "/home/xv/DataForRecom/saveData/ua.base";
		trainData = getData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/ua.test";
		testData = getData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/simiMatrixTW.txt";
		
		getRatingMatrixBySVD(50, 0.01, 0.01);
		
		System.out.println("cluster.size() = " + clusterResult.size());
		System.out.println("cluster  = " + clusterResult);
		long end = System.currentTimeMillis();
		System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
	}
	
	public static void main(String[] args) throws Exception {
		SVD dh = new SVD();
		dh.ourMethod();
		
	}
}
