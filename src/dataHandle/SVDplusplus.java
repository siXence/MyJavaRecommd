/**
 * Based on distance to cluster items
 * Item vector is multi-dimension
 * Using the thought of SVD++ to build rating matrix 
 */
package dataHandle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class SVDplusplus extends ClusterSVDDist {
	protected double[][] impItemVector = new double[item_num][clusterNum];
	
	public double[] getImplictInfo(int uid) {
		HashMap<Integer, Double> items = trainData.get(uid);
		double[] imPu = new double[clusterNum];
		Set<Integer> keys_item = items.keySet();
		Iterator<Integer> iterator_item = keys_item.iterator();
		double weight = (double)1/Math.sqrt(items.size());
		while (iterator_item.hasNext())  {
			int iid = iterator_item.next();
			for (int i = 0; i < clusterNum; i++) {
				imPu[i] += impItemVector[iid-1][i];
			}
		}
		for (int i = 0; i < clusterNum; i++) {
			imPu[i] *= weight;
		}
		return imPu;
	}
	
	public void updateImplicitItemVector(int uid, double eui, double learnRate, double lamda) {
		HashMap<Integer, Double> items = trainData.get(uid);
		Set<Integer> keys_item = items.keySet();
		Iterator<Integer> iterator_item = keys_item.iterator();
		double weight = (double)1/Math.sqrt(items.size());
		while (iterator_item.hasNext())  {
			int iid = iterator_item.next();
			for (int i = 0; i < clusterNum; i++) {
				impItemVector[iid-1][i] += learnRate*(eui*weight*itemVector[iid-1][i] - lamda*impItemVector[iid-1][i]);
			}
		}
	}
	
	
	public void getRatingMatrixBySVD(int iterNum, double learnRate, double lamda) {

		double[] bi = new double[item_num];
		double[] bu = new double[user_num];
		//implicit item factor vector
		Random rd = new Random();
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < clusterNum; j++) {
				userVector[i][j] = rd.nextDouble();	
				impItemVector[i][j] = rd.nextDouble();
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
					double[] imPu = getImplictInfo(uid);
					for (int k = 0; k < clusterNum; k++) {
						prod += (userVector[uid-1][k] + imPu[k])*itemVector[iid-1][k];
					}
					double y = prod + averg  + bu[uid-1] + bi[iid-1];
					double eui = x - y;
					
					bu[uid-1] += learnRate*(eui - lamda*bu[uid-1]);
					bi[iid-1] += learnRate*(eui - lamda*bi[iid-1]);

					for (int j = 0; j < clusterNum; j++) {
						userVector[uid-1][j] += learnRate*(eui*itemVector[iid-1][j] - lamda*userVector[uid-1][j]);
					}
					updateImplicitItemVector(uid, eui, learnRate, lamda);
				}
			}
			learnRate *= 0.9;
			System.out.println("The test_RMSE in " + (i+1) + "time :---------------------------------------------------------------------");
			for (int ii = 0; ii < user_num; ii++) {
				for (int jj = 0; jj < item_num; jj++) {
//					ratingMatrix[ii][jj] = 0.0;
					double p = 0.0;
					double[] imPu = getImplictInfo(ii+1);
					for (int k = 0; k < clusterNum; k++) {
						p += (userVector[ii][k] + imPu[k])*itemVector[jj][k];
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
		SVDplusplus dh = new SVDplusplus();
		dh.ourMethod();

		
	}
}
