/**
 * 
 */
package handleTWData;

import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;

/**
 * @author xv
 *
 */
public class DirectComputeUserVector extends MethodBasedOnDistanceTW{
	public void ourMethod() throws IOException, TasteException {
		System.out.println("Starting...");
		long start = System.currentTimeMillis();
		String filePath = "/home/xv/DataForRecom/saveData/ua.base";
		
		initRatingMatrix();
		
//		trainData = getData(filePath);
		getTrainData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/ua.test";
//		testData = getData(filePath);
		getTestData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/simiMatrixTW.txt";

		String trainFile = "/home/xv/DataForRecom/saveData/ua.base";
//		computeCityBlockSimilarity(trainFile);
		
//		filePath = "/home/xv/DataForRecom/saveData/simiMatrixByHand.txt";
//		writeSimiMatrixIntoFile(filePath);
//		
//		computeSimilary();
		computeSimilaryAllItems() ;
//		writeSimiMatrixIntoFile(filePath);
		
//		readSimiMatrixFile(filePath);
		
		clustering(clusterNum);
		buildMultiItemVector();		
//		buildUserVectorBySum();
		buildUserVectorBySumAll();
		getRatingMatrix();
//		getRatingMatrixBySVD(50, 0.5, 0.01);
//		getRatingMatrixBySVDAllItems(50, 0.5, 0.01);
		
		sortItemsForUser();
		getPreAndRecallAndF();
		
		System.out.println("cluster.size() = " + clusterResult.size());
		long end = System.currentTimeMillis();
		System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
	}
	
	public void getPreAndRecall() {
		
	}
	
	public static void main(String[] args) throws Exception {
		DirectComputeUserVector dh = new DirectComputeUserVector();
		dh.ourMethod();
		System.out.println("Done");
		
		
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
