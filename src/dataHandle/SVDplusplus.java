package dataHandle;

public class SVDplusplus extends MyRecom {
	
	public void ourMethod() {
		System.out.println("Starting...");
//		long start = System.currentTimeMillis();
//		String filePath = "/home/xv/DataForRecom/ml-100k/u1.base";
//		trainData = getData(filePath);
//		filePath = "/home/xv/DataForRecom/ml-100k/u1.test";
//		testData = getData(filePath);
//		filePath = "/home/xv/DataForRecom/saveData/simiMatrix.txt";
//		
////		computeSimilary();
////		writeSimiMatrixIntoFile(filePath);
//		
//		readSimiMatrixFile(filePath);
//		clustering(clusterNum);
//		buildMultiItemVector();
//		
//		
////		buildUserVectorBySum();
////		getRatingMatrix();
////		getRMSE();
//		
//		getRatingMatrixBySVD(50, 0.01, 0.01);
//		
//		System.out.println("cluster.size() = " + clusterResult.size());
//		long end = System.currentTimeMillis();
//		System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
	}
	
	public static void main(String[] args) throws Exception {
		SVDplusplus dh = new SVDplusplus();
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
