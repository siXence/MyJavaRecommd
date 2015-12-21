/**
 * 
 */
package handleTWData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * @author xv
 *
 */
public class ClusterSVD extends MethodBasedOnSimilarityTW{
	
	public void computePearsonCorrelationSimilarity(String trainFile) throws IOException, TasteException {
		System.out.println("Starting PearsonCorrelationSimilarity...");
		long start = System.currentTimeMillis();
		int i =0;
		int j = 0;
		DataModel model = new FileDataModel(new File(trainFile));
		ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
		 long[] itemID2s = new long[item_num];
		 for (i = 0; i < item_num; i++) {
			 itemID2s[i] = i;
		 }
		 double si[] = similarity.itemSimilarities(782, itemID2s);
		 for (i = 0; i < si.length; i++) {
			 System.out.println("782 and " + i + " :" + si[i]);
		 }
		
		for (i = 0; i < item_num; i++) {
			for (j = i; j < item_num; j++) {
				double tmp = similarity.itemSimilarity(i+1, j+1);
				if (Double.isNaN(tmp)) {
					this.simiMatrix[i][j] = 0.0;
				} else {
					this.simiMatrix[i][j] = tmp;
				}
				
			}
		}
//		try {
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}	catch (TasteException e) {
//			e.printStackTrace();
//			this.simiMatrix[i][j] = 0.0;
//		} 
		long end = System.currentTimeMillis();
		System.out.println("PearsonCorrelationSimilarity 运行时间：" + (end - start) + "毫秒");
	}
	
	public void computeCityBlockSimilarity(String trainFile) throws IOException, TasteException {
		System.out.println("Starting computeCityBlockSimilarity...");
		long start = System.currentTimeMillis();
		DataModel model=new FileDataModel(new File(trainFile));
		ItemSimilarity similarity=new CityBlockSimilarity(model); 
		for (int i = 0; i < item_num; i++) {
			for (int j = i; j < item_num; j++) {
				if (Double.isNaN(similarity.itemSimilarity(i+1, j+1))) {
					System.out.println("There is a NaN.");
				}
				int c1 = 1;
				if (itemCnt.containsKey(i+1)) {
					c1 = itemCnt.get(i+1);
				}
				int c2 = 1;
				if (itemCnt.containsKey(j+1)) {
					c2 = itemCnt.get(j+1);
				}
				this.simiMatrix[i][j] = similarity.itemSimilarity(i+1, j+1)/(c1*c2);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("computeCityBlockSimilarity 运行时间：" + (end - start) + "毫秒");
	}
	
	public void computeEuclideanDistanceSimilarity(String trainFile) throws IOException, TasteException {
		System.out.println("Starting EuclideanDistanceSimilarity...");
		long start = System.currentTimeMillis();
		DataModel model=new FileDataModel(new File(trainFile));
		System.out.println("item num " + model.getNumItems());
		System.out.println("user num " + model.getNumUsers());
		ItemSimilarity similarity=new EuclideanDistanceSimilarity(model); 
		for (int i = 0; i < item_num; i++) {
			for (int j = i; j < item_num; j++) {
				if (Double.isNaN(similarity.itemSimilarity(i+1, j+1))) {
					System.out.println("There is a NaN.");
				}
				this.simiMatrix[i][j] = similarity.itemSimilarity(i+1, j+1);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("EuclideanDistanceSimilarity 运行时间：" + (end - start) + "毫秒");
	}
	
	
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
	
	
	
	
	
	
	public void ourMethod() throws IOException, TasteException {
		System.out.println("Starting...");
		long start = System.currentTimeMillis();
		String filePath = "/home/xv/DataForRecom/saveData/ua.base";
		
//		trainData = getData(filePath);
		getTrainData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/ua.test";
//		testData = getData(filePath);
		getTestData(filePath);
//		filePath = "/home/xv/DataForRecom/saveData/simiMatrixTW.txt";

		String trainFile = "/home/xv/DataForRecom/saveData/ua.base";
		computeCityBlockSimilarity(trainFile);
		filePath = "/home/xv/DataForRecom/saveData/simiMatrixCity.txt";
//		writeSimiMatrixIntoFile(filePath);
//		
//		computeSimilary();
//		writeSimiMatrixIntoFile(filePath);
		
//		readSimiMatrixFile(filePath);
		
		clustering(clusterNum);
		buildMultiItemVector();		
		getRatingMatrixBySVD(100, 0.5, 0.01);
		
		System.out.println("cluster.size() = " + clusterResult.size());
		long end = System.currentTimeMillis();
		System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
	}
	
	public void getPreAndRecall() {
		
	}
	
	public static void main(String[] args) throws Exception {
		ClusterSVD dh = new ClusterSVD();
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
