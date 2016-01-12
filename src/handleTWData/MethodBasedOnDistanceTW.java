/**
 * 
 */
package handleTWData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * @author xv
 *
 */
public class MethodBasedOnDistanceTW  extends MethodBasedOnSimilarityTW{
	
	public void computeSimilary() {
		System.out.println("Start to compute similarity");
		long start = System.currentTimeMillis();
		for (int i = 1; i <= item_num; i++) {
			System.out.println("The " + (i) + " item...");
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
		System.out.println("simiMatrix = " + simiMatrix);
		long end = System.currentTimeMillis();
		System.out.println("computeSimilary 运行时间：" + (end - start) + "毫秒");
		System.out.println("Already get the similarity matrix.");
	}
	
	public void computeSimilaryAllItems() {
		System.out.println("Start to compute similarity");
		long start = System.currentTimeMillis();
		for (int i = 1; i <= item_num; i++) {
			System.out.println("The " + (i) + " item...");
			for (int j = i; j <= item_num; j++) {
				double d = 0;
				for (int uid = 1; uid <= user_num; uid++) {
					double x = upmat[uid-1][i-1];
					double y = upmat[uid-1][j-1];
					
//					double x = 0.0;
//					double y = 0.0;
//					if (trainData.containsKey(uid)) {
//						if (trainData.get(uid).containsKey(i)) {
//							x = trainData.get(uid).get(i);
//						}
//						if (trainData.get(uid).containsKey(j)) {
//							y = trainData.get(uid).get(j);
//						}
//					}
					d += Math.abs(x-y);
				}
				simiMatrix[i-1][j-1] = d;
			}
		}
		System.out.println("simiMatrix = " + simiMatrix);
		long end = System.currentTimeMillis();
		System.out.println("computeSimilary 运行时间：" + (end - start) + "毫秒");
		System.out.println("Already get the similarity matrix.");
	}
	
	public void clustering(int K) {
		long start = System.currentTimeMillis();
		System.out.println("Start to cluster...");
		ArrayList<Integer> itemList = new ArrayList<Integer>();
		for (int i = 0; i < item_num; i++) {
			itemList.add(i);
		}
		
		for (int k = 0; k < K; k++) {
			int x = 0;
			int y = 0;
			double sim = -1.0;
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
			for (Integer id:itemList) {
				if (getSim(x, id) < getSim(y, id)) {
					clusterResult.get(x).add(id);
				} else {
					clusterResult.get(y).add(id);
				}
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
		System.out.println("cluster  = " + clusterResult);
		System.out.println("cluster.size()  = " + clusterResult.size());
	}
	
	
	public void buildMultiItemVector() {
		System.out.println("Start to build itemVector...");
		long start = System.currentTimeMillis();		
		
		
		int[] centers = new int[clusterNum];
		
		Set<Integer> keys = clusterResult.keySet();
		Iterator<Integer> iterator = keys.iterator();
		int idx = 0;
		while (iterator.hasNext()) {
			int key = iterator.next();
			centers[idx] = key;
			idx++;
		}
		
		for (int i = 0; i < item_num; i++) {
			double sum = 0.0;
			for (int j = 0; j < clusterNum; j++) {
				double tmp = getSim(i, centers[j]);
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
	
	public void buildSingleItemVector() {
		System.out.println("Start to build itemVector...");
		long start = System.currentTimeMillis();		
		
		
		int[] centers = new int[clusterNum];
		
		Set<Integer> keys = clusterResult.keySet();
		Iterator<Integer> iterator = keys.iterator();
		int idx = 0;
		while (iterator.hasNext()) {
			int key = iterator.next();
			centers[idx] = key;
			idx++;
		}
		
		for (int i = 0; i < item_num; i++) {
			for (int j = 0; j < clusterNum; j++) {
				if (clusterResult.get(centers[j]).contains(i)) {
					itemVector[i][j] = 1.0;
					break;
				}
			}

			
		}
		long end = System.currentTimeMillis();
		System.out.println("buildItemVector 运行时间：" + (end - start) + "毫秒");

	}
}
