/**
 * xue的方法思想，不过由基于用户改变为基于节目
 */
package handleTWData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class fillAndKNNItem  extends MethodBasedOnSimilarityTW{
//	double[][] itemSim = new double[item_num][item_num];
	
	protected final int closedItemNum = 50;
	protected final int closedItemCluNum = 50;
	protected final double lambda = 0.2;
	
	public void computeItemSimInter() {
		for (int i = 1; i <= item_num; i++) {
			for (int j = i; j <= item_num; j++) {
				HashMap<Integer, Double> itemI = itemByUsers.get(i);
				HashMap<Integer, Double> itemJ = itemByUsers.get(j);
				int cnt = 0;
				boolean isEnter = false;
				Set<Integer> keys = itemI.keySet();
				Iterator<Integer> iterator = keys.iterator();
				
				double d  = 0.0;
				while (iterator.hasNext()) {
					int key = iterator.next();
					if (itemJ.containsKey(key)) {
						isEnter = true;
						cnt++;
					}
				}
				if (isEnter) {
					simiMatrix[i-1][j-1]  = cnt*1.0/(itemI.size()*itemJ.size());
				}
			}
		}
	}
	
	public void itemKNNFill() {
		int allCnt = 0;
		long start = System.currentTimeMillis();
		System.out.println("Start to itemKNNFill...");
		for (int i = 0 ; i < item_num; i++) {
			ArrayList<Integer> items = new ArrayList<Integer>();
			Set<Integer> keys = clusterResult.keySet();
			Iterator<Integer> iterator = keys.iterator();
			boolean isEnter = false;
			while (iterator.hasNext()) {
				int key = iterator.next();
				if (clusterResult.get(key).contains(i)) {
					items = clusterResult.get(key);
					isEnter = true;
					break;
				}
			}
			
			if (!isEnter) {
				System.out.println("item.size() 0    i = " + i);
			}
			
			for (int j = 0; j < user_num; j++) {
				if (itemByUsers.get(i+1).containsKey(j+1)) {
					double deltaR = 0.0;
					int cnt = 0;
					for (Integer it:items) {
						if (itemByUsers.get(it+1).containsKey(j+1)) {
							cnt++;
							deltaR += (itemByUsers.get(it+1).get(j+1)-itemAvg.get(it+1));
						}
					}
					if (cnt > 0) {
						upmat[j][i] = deltaR/cnt + itemAvg.get(i+1);
					}
				}
			}
			
		}
	
		System.out.println("allCnt -------------------------------------------------> " + allCnt);
		long end = System.currentTimeMillis();
		System.out.println("itemKNNFill 运行时间：" + (end - start) + "毫秒");
	}
	
	
	
	public ArrayList<Integer>  neighborPreSelect(int iid) {
		ArrayList<Integer> items = new ArrayList<Integer>();
		ArrayList<ArrayList<Double>> simCluster = new ArrayList<ArrayList<Double>>();
		Set<Integer> keys = clusterResult.keySet();
		Iterator<Integer> iterator = keys.iterator();
		while (iterator.hasNext()) {
			int key = iterator.next();
			ArrayList<Integer> itemsInCluster = clusterResult.get(key);
			ArrayList<Integer> usersAll = new ArrayList<Integer>();
			for (Integer item:itemsInCluster) {
				HashMap<Integer, Double> users = itemByUsers.get(item+1);
				Set<Integer> uids = users.keySet();
				Iterator<Integer> iter = uids.iterator();
				while(iter.hasNext()) {
					int uid = iter.next();
					if (!usersAll.contains(uid)) {
						usersAll.add(uid);
					}
				}
				
			}
//			System.out.println("itemsAll = " + itemsAll);
			double numerator = 0.0;
			double denom1 = 0.0;
			double denom2 = 0.0;
			for (Integer uid:usersAll) {
				if (itemByUsers.get(iid).containsKey(uid)) {
					double deltaR = 0.0;
					int cnt = 0;
					for (Integer itemInClu:itemsInCluster) {
						if (itemByUsers.get(itemInClu+1).containsKey(uid)) {
							cnt++;
							deltaR +=( itemByUsers.get(itemInClu+1).get(uid) - itemAvg.get(itemInClu+1));
						}
					}
					if (cnt > 0) {
						deltaR /= cnt;
					}
					numerator += deltaR*(itemByUsers.get(iid).get(uid)-itemAvg.get(iid));
					denom1 += deltaR*deltaR;
					denom2 += (itemByUsers.get(iid).get(uid)-itemAvg.get(iid))* (itemByUsers.get(iid).get(uid)-itemAvg.get(iid));
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
		
		int itemCnt = 0;
		for (int i = 0; i < closedItemCluNum; i++) {
//			String[] tmp = simCluster.get(i).split("\t");
			int id = Integer.parseInt(new java.text.DecimalFormat("0").format(simCluster.get(i).get(1)));
			ArrayList<Integer> itemsInCluster = clusterResult.get(id);
			for (Integer u:itemsInCluster) {
				if (!items.contains(u)) {
					items.add(u);
					itemCnt++;
					if (itemCnt > closedItemNum) {
						break;
					}
				}
			}
		}
		
		return items;
	}
	
	
	
	
	public ArrayList<ArrayList<Double>>  neighborSelection(int iid) {
		ArrayList<Integer> items = neighborPreSelect(iid);
//		uid--;
		ArrayList<ArrayList<Double>> simItemInCluster = new ArrayList<ArrayList<Double>>();
		double numerator = 0.0;
		double denom1 = 0.0;
		double denom2 = 0.0;
		HashMap<Integer, Double> iidUsers = itemByUsers.get(iid);
		for (Integer u:items) {
			u++;
			Set<Integer> keys = iidUsers.keySet();
			Iterator<Integer> iterator = keys.iterator();
			while (iterator.hasNext()) {
				int key = iterator.next();
				double wui = lambda;
				if (itemByUsers.get(u).containsKey(key)) {
					wui = 1- wui;
				}
				numerator += wui*(upmat[key-1][u-1]-itemAvg.get(u))*(upmat[key-1][iid-1]-itemAvg.get(iid));
				denom1 +=  wui*(upmat[key-1][u-1]-itemAvg.get(u))* wui*(upmat[key-1][u-1]-itemAvg.get(u));
				denom2 += (upmat[key-1][iid-1]-itemAvg.get(iid))*(upmat[key-1][iid-1]-itemAvg.get(iid));
			}
			if (denom1 > 0 && denom2 > 0) {
				double sim = numerator/Math.sqrt(denom1*denom2);
				if (iid <= u) {
					simiMatrix[iid-1][u-1] = sim;
				} else {
					simiMatrix[u-1][iid-1] = sim;
				}
				
//				String tmp = String.valueOf(sim) + "\t" + String.valueOf(u);		
				ArrayList<Double> tmp = new ArrayList<Double>();
				tmp.add(sim);
				tmp.add((double) (u));
				simItemInCluster.add(tmp); 
			}
			
			
		}
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Collections.sort(simItemInCluster, new Comparator<Object>(){
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
		Collections.reverse(simItemInCluster);
		return simItemInCluster;

		
//		for (Integer u:trainData.get(uid)) {
//			for ()
//			if (trainData.get(u+1).containsKey(key))
//			
//		}

		
	}
	
	
	
	public void computePrediction() {
		for (int iid = 1; iid <= item_num; iid++) {
			ArrayList<ArrayList<Double>> simItemInCluster  = neighborSelection(iid);
			for (int uid = 1; uid <= user_num; uid++) {
				double numerator = 0.0;
				double denom = 0.0;
				for (int i = 0; i < closedItemNum && i < simItemInCluster.size(); i++) {
					int id = Integer.parseInt(new java.text.DecimalFormat("0").format(simItemInCluster.get(i).get(1)));
					double wui = lambda;
					if (itemByUsers.get(id).containsKey(uid)) {
						wui = 1-wui;
					}
					numerator += wui*getSim(iid-1, id-1)*(upmat[uid-1][id-1] - itemAvg.get(id));
					denom += wui*getSim(iid-1, id-1);
					
//					numerator += wui*getUserSim(uid-1, id-1)*(upmat[id-1][iid-1] - userAvg.get(id));
//					denom += wui*getUserSim(uid-1, id-1);
				}
				ratingMatrix[uid-1][iid-1] = itemAvg.get(iid) + numerator/denom;
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
		
		//simi s1  0.12
		//simi s2  0.11
		//simi s3  0.43
		//simi s4  0.20
		
//		fillMissingProg();
		
		computeItemAverage();

//		computeItemSimInter();
		reComputeItemSim();
		
		String path = "/home/xv/DataForRecom/saveData/itemSim.txt";
		writeSimiMatrixIntoFile(path);
		clustering(clusterNum);
		itemKNNFill();
		path = "/home/xv/DataForRecom/saveData/upmat.txt";
		saveUPmat(path);
		
		

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
		fillAndKNNItem fk = new fillAndKNNItem();
		fk.ourMethod();
	}
	
}
