/**
 * 利用对用户聚类补充缺失值，最后利用节目聚类来预测喜爱度，即knn方法来实现预测
 */
package handleTWData;

import java.io.IOException;
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
public class fillAndKNN extends MethodBasedOnSimilarityTW{
	
	protected final double lambda = 0.35;
	protected final int closedCluNum = 50;
	protected final int userNeigh = 30;
	
	
	/**
	 * 
	 * @param uid
	 * @return the most similar users, which start with 0
	 */
	public ArrayList<Integer>  neighborPreSelect(int uid) {
		ArrayList<Integer> users = new ArrayList<Integer>();
		ArrayList<ArrayList<Double>> simCluster = new ArrayList<ArrayList<Double>>();
		Set<Integer> keys = userCluster.keySet();
		Iterator<Integer> iterator = keys.iterator();
		while (iterator.hasNext()) {
			int key = iterator.next();
			ArrayList<Integer> usersInCluster = userCluster.get(key);
			ArrayList<Integer> itemsAll = new ArrayList<Integer>();
			for (Integer user:usersInCluster) {
				HashMap<Integer, Double> items = trainData.get(user+1);
				Set<Integer> iids = items.keySet();
				Iterator<Integer> iter = iids.iterator();
				while(iter.hasNext()) {
					int iid = iter.next();
					if (!itemsAll.contains(iid)) {
						itemsAll.add(iid);
					}
				}
				
			}
//			System.out.println("itemsAll = " + itemsAll);
			double numerator = 0.0;
			double denom1 = 0.0;
			double denom2 = 0.0;
			for (Integer iid:itemsAll) {
				if (trainData.get(uid).containsKey(iid)) {
					double deltaR = 0.0;
					int cnt = 0;
					for (Integer userInClu:usersInCluster) {
						if (trainData.get(userInClu+1).containsKey(iid)) {
							cnt++;
							deltaR +=( trainData.get(userInClu+1).get(iid) - userAvg.get(userInClu+1));
						}
					}
					if (cnt > 0) {
						deltaR /= cnt;
					}
					numerator += deltaR*(trainData.get(uid).get(iid)-userAvg.get(uid));
					denom1 += deltaR*deltaR;
					denom2 += (trainData.get(uid).get(iid)-userAvg.get(uid))*(trainData.get(uid).get(iid)-userAvg.get(uid));
				}
				
			}
			
			ArrayList<Double> tmp = new ArrayList<Double>();
			if (denom1 > 0 && denom2 > 0) {
				tmp.add(numerator/(Math.sqrt(denom1*denom2)));
			} else {
				tmp.add(0.0);
			}
			
			tmp.add((double) (key));
			simCluster.add(tmp);
			
//			if (denom1 > 0 && denom2 > 0) {
////				String tmp = String.valueOf(numerator/(Math.sqrt(denom1*denom2))) + "\t" + String.valueOf(key);
//				ArrayList<Double> tmp = new ArrayList<Double>();
//				tmp.add(numerator/(Math.sqrt(denom1*denom2)));
//				tmp.add((double) (key));
//				simCluster.add(tmp);
//			}
		}
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Collections.sort(simCluster, new Comparator<Object>(){
			public int compare(Object obj1,Object obj2){
		        @SuppressWarnings("unchecked")
				ArrayList<Double> a=(ArrayList<Double>)obj1;
		        @SuppressWarnings("unchecked")
				ArrayList<Double> b=(ArrayList<Double>)obj2;
		        Double v1=a.get(0);
		        Double v2=b.get(0);
		        if(v1==v2){return 0;}
		        if(v1>v2){return 1;}
		        return -1;
		    }
		});
		Collections.reverse(simCluster);
//		System.out.println("simCluster = " + simCluster);
		
		int userCnt = 0;
		for (int i = 0; i < closedCluNum; i++) {
//			String[] tmp = simCluster.get(i).split("\t");
			int id = Integer.parseInt(new java.text.DecimalFormat("0").format(simCluster.get(i).get(1)));
			ArrayList<Integer> usersInCluster = userCluster.get(id);
			for (Integer u:usersInCluster) {
				if (!users.contains(u)) {
					users.add(u);
					userCnt++;
					if (userCnt > userNeigh) {
						break;
					}
				}
			}
		}
		
		return users;
	}
	
	
	public ArrayList<ArrayList<Double>>  neighborSelection(int uid) {
		ArrayList<Integer> users = neighborPreSelect(uid);
//		uid--;
		ArrayList<ArrayList<Double>> simUserInCluster = new ArrayList<ArrayList<Double>>();
		double numerator = 0.0;
		double denom1 = 0.0;
		double denom2 = 0.0;
		HashMap<Integer, Double> uidItems = trainData.get(uid);
		for (Integer u:users) {
			u++;
			Set<Integer> keys = uidItems.keySet();
			Iterator<Integer> iterator = keys.iterator();
			while (iterator.hasNext()) {
				int key = iterator.next();
				double wui = lambda;
				if (trainData.get(u).containsKey(key)) {
					wui = 1- wui;
				}
				numerator += wui*(upmat[u-1][key-1]-userAvg.get(u))*(upmat[uid-1][key-1]-userAvg.get(uid));
				denom1 +=  wui*(upmat[u-1][key-1]-userAvg.get(u))* wui*(upmat[u-1][key-1]-userAvg.get(u));
				denom2 += (upmat[uid-1][key-1]-userAvg.get(uid))*(upmat[uid-1][key-1]-userAvg.get(uid));
			}
			if (denom1 > 0 && denom2 > 0) {
				double sim = numerator/Math.sqrt(denom1*denom2);
				if (uid <= u) {
					userSim[uid-1][u-1] = sim;
				} else {
					userSim[u-1][uid-1] = sim;
				}
				
//				String tmp = String.valueOf(sim) + "\t" + String.valueOf(u);		
				ArrayList<Double> tmp = new ArrayList<Double>();
				tmp.add(sim);
				tmp.add((double) (u));
				simUserInCluster.add(tmp); 
			}
			
			
		}
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Collections.sort(simUserInCluster, new Comparator<Object>(){
			public int compare(Object obj1,Object obj2){
		        @SuppressWarnings("unchecked")
				ArrayList<Double> a=(ArrayList<Double>)obj1;
		        @SuppressWarnings("unchecked")
				ArrayList<Double> b=(ArrayList<Double>)obj2;
		        Double v1=a.get(0);
		        Double v2=b.get(0);
		        if(v1==v2){return 0;}
		        if(v1>v2){return 1;}
		        return -1;
		    }
		});
		Collections.reverse(simUserInCluster);
		return simUserInCluster;

		
//		for (Integer u:trainData.get(uid)) {
//			for ()
//			if (trainData.get(u+1).containsKey(key))
//			
//		}

		
	}
	
	
//	public void prediction(int uid, int iid) {
//		ArrayList<String> simUserInCluster  = neighborSelection(uid);
//		double numerator = 0.0;
//		double denom = 0.0;
//		for (int i = 0; i < userNeigh && i < simUserInCluster.size(); i++) {
//			String[] tmp = simUserInCluster.get(i).split("\t");
//			int id = Integer.parseInt(tmp[1]);
//			double wui = lambda;
//			if (trainData.get(id).containsKey(iid)) {
//				wui = 1-wui;
//			}
//			numerator += wui*getUserSim(uid, id)*(upmat[id-1][iid-1] - userAvg.get(id));
//			denom += wui*getUserSim(uid, id);
//		}
//		ratingMatrix[uid-1][iid-1] = userAvg.get(uid) + numerator/denom;
//	}
	
	
	public void computePrediction() {
		for (int uid = 1; uid <= user_num; uid++) {
			ArrayList<ArrayList<Double>> simUserInCluster  = neighborSelection(uid);
			for (int iid = 1; iid <= item_num; iid++) {
				double numerator = 0.0;
				double denom = 0.0;
				for (int i = 0; i < userNeigh && i < simUserInCluster.size(); i++) {
					int id = Integer.parseInt(new java.text.DecimalFormat("0").format(simUserInCluster.get(i).get(1)));
					double wui = lambda;
					if (trainData.get(id).containsKey(iid)) {
						wui = 1-wui;
					}
					numerator += wui*getUserSim(uid-1, id-1)*(upmat[id-1][iid-1] - userAvg.get(id));
					denom += wui*getUserSim(uid-1, id-1);
				}
				ratingMatrix[uid-1][iid-1] = userAvg.get(uid) + numerator/denom;
			}
		}
	}
	
	
	public void fillMissingUPmat() {
		for (int uid = 1; uid <= user_num; uid++) {
			ArrayList<ArrayList<Double>> simUserInCluster  = neighborSelection(uid);
			for (int iid = 1; iid <= item_num; iid++) {
				if (!trainData.get(uid).containsKey(iid)) {
					double numerator = 0.0;
					double denom = 0.0;
					for (int i = 0; i < userNeigh && i < simUserInCluster.size(); i++) {
						int id = Integer.parseInt(new java.text.DecimalFormat("0").format(simUserInCluster.get(i).get(1)));
						double wui = lambda;
						if (trainData.get(id).containsKey(iid)) {
							wui = 1-wui;
						}
						numerator += wui*getUserSim(uid-1, id-1)*(upmat[id-1][iid-1] - userAvg.get(id));
						denom += wui*getUserSim(uid-1, id-1);
					}
					ratingMatrix[uid-1][iid-1] = userAvg.get(uid) + numerator/denom;
				}
			}
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
		computePrediction();

//		String trainFile = "/home/xv/DataForRecom/saveData/ua.base";
////		computeCityBlockSimilarity(trainFile);
//		
//		filePath = "/home/xv/DataForRecom/saveData/simiMatrixByHandFill.txt";
////		writeSimiMatrixIntoFile(filePath);
////		
////		computeSimilary();
////		reComputeItemSim();
//		
//		computeSimilaryAllItems() ;
////		readSimi() ;
//		
//		writeSimiMatrixIntoFile(filePath);
//		
////		readSimiMatrixFile(filePath);
//		
//		clustering(clusterNum);
//		
////		saveClusterResult() ;
////		getClustersCenters();
//		
////		buildMultiItemVector();		
////		buildMultiItemVector2();		
//		buildMultiItemVector3();
////		buildSingleItemVector();
//		
////		buildUserVectorBySum();
//		buildUserVectorBySumAll();
//		getRatingMatrix();
//		
////		saveUPrec();
//		
//		
//		
////		getRatingMatrixBySVD(50, 0.5, 0.01);
////		getRatingMatrixBySVDAllItems(50, 0.5, 0.01);
		
		sortItemsForUser();
		
		
//		updateTrainTestSetFromFile() ;
		
		
		getPreAndRecallAndF();
		
		System.out.println("cluster.size() = " + clusterResult.size());
		long end = System.currentTimeMillis();
		System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
	}
	
	
	public static void main(String[] args) throws Exception {
		fillAndKNN fk = new fillAndKNN();
		fk.ourMethod();
	}
	
	
	
}
