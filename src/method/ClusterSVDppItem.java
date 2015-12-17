/**
 * 
 */
package method;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * @author xv
 *
 */
public class ClusterSVDppItem extends MethodBasedOnSimilarity{
	protected double[][] impUserVector = new double[user_num][clusterNum];
	//Record the users that rated on a certain item
	HashMap<Integer, ArrayList<Integer> > imItemToUser = new HashMap<Integer, ArrayList<Integer>>();
	
	public void getItemUserList(String filePath) {
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
                    if (!imItemToUser.containsKey(iid)) {
                    	imItemToUser.put(iid, new ArrayList<Integer>());
                    } 
                    imItemToUser.get(iid).add(uid);
                    
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
	
	public double[] getImplictInfo(int iid) {
		ArrayList<Integer> users = imItemToUser.get(iid);
		double[] imPu = new double[clusterNum];
		double weight = (double)1/Math.sqrt(users.size());
		for (int i = 0; i < users.size(); i++) {
			int uid = users.get(i);
			for (int j = 0; j < clusterNum; j++) {
				imPu[j] += impUserVector[uid-1][j];
			}
		}
		for (int i = 0; i < clusterNum; i++) {
			imPu[i] *= weight;
		}
		return imPu;
	}
	
	public void updateImplicitItemVector(int iid, double eui, double learnRate, double lamda) {
		ArrayList<Integer> users = imItemToUser.get(iid);
		double weight = (double)1/Math.sqrt(users.size());
		for (int i = 0; i < users.size(); i++) {
			int uid = users.get(i);
			for (int j = 0; j < clusterNum; j++) {
				impUserVector[uid-1][j] += learnRate*(eui*weight*userVector[uid-1][j]- lamda*impUserVector[uid-1][j]);
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
				impUserVector[i][j] = rd.nextDouble();
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
					double[] imPu = getImplictInfo(iid);
					for (int k = 0; k < clusterNum; k++) {
						prod += userVector[uid-1][k] * (itemVector[iid-1][k] + imPu[k]);
					}
					double y = prod + averg  + bu[uid-1] + bi[iid-1];
					double eui = x - y;
					
					bu[uid-1] += learnRate*(eui - lamda*bu[uid-1]);
					bi[iid-1] += learnRate*(eui - lamda*bi[iid-1]);

					for (int j = 0; j < clusterNum; j++) {
						userVector[uid-1][j] += learnRate*(eui* (itemVector[iid-1][j] + imPu[j]) - lamda*userVector[uid-1][j]);
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
					double[] imPu = getImplictInfo(jj+1);
					for (int k = 0; k < clusterNum; k++) {
						p += userVector[ii][k]*(itemVector[jj][k] + imPu[k]);
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
		getItemUserList(filePath);
		filePath = "/home/xv/DataForRecom/ml-100k/u1.test";
		testData = getData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/simiMatrix.txt";
		
//		computeSimilary();
//		writeSimiMatrixIntoFile(filePath);
		
		readSimiMatrixFile(filePath);
		clustering(clusterNum);
		buildMultiItemVector();
		
		
		getRatingMatrixBySVD(50, 0.01, 0.01);
		
		System.out.println("cluster.size() = " + clusterResult.size());
//		System.out.println("cluster  = " + clusterResult);
		long end = System.currentTimeMillis();
		System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
	}
	
	public static void main(String[] args) throws Exception {
		ClusterSVDppUser dh = new ClusterSVDppUser();
		dh.ourMethod();

		
	}
	
}
