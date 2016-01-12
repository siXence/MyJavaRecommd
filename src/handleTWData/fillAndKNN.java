/**
 * 利用对用户聚类补充缺失值，最后利用节目聚类来预测喜爱度，即knn方法来实现预测
 */
package handleTWData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * @author xv
 *
 */
public class fillAndKNN extends MethodBasedOnSimilarityTW{
	
	protected final double lambda = 0.5;
	protected final int closedCluNum = 20;
	protected final int userNeigh = 20;
	
	
	/**
	 * 
	 * @param uid
	 * @return the most similar users, which start with 0
	 */
	public ArrayList<Integer>  neighborPreSelect(int uid) {
		ArrayList<Integer> users = new ArrayList<Integer>();
		ArrayList<String> simCluster = new ArrayList<String>();
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
				if (denom1 > 0 && denom2 > 0) {
					String tmp = String.valueOf(numerator/(Math.sqrt(denom1*denom2))) + "\t" + String.valueOf(key);
					simCluster.add(tmp);
				}
			}
		}
		
		Collections.sort(simCluster);
		Collections.reverse(simCluster);
		
		for (int i = 0; i < closedCluNum; i++) {
			String[] tmp = simCluster.get(i).split("\t");
			ArrayList<Integer> usersInCluster = userCluster.get(Integer.parseInt(tmp[1]));
			for (Integer u:usersInCluster) {
				if (!users.contains(u)) {
					users.add(u);
				}
			}
		}
		
		return users;
	}
	
	
	public ArrayList<String>  neighborSelection(int uid) {
		ArrayList<Integer> users = neighborPreSelect(uid);
//		uid--;
		ArrayList<String> simUserInCluster = new ArrayList<String>();
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
				
				String tmp = String.valueOf(sim) + "\t" + String.valueOf(u);		
				simUserInCluster.add(tmp); 
			}
			
			
		}
		Collections.sort(simUserInCluster);
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
			ArrayList<String> simUserInCluster  = neighborSelection(uid);
			for (int iid = 1; iid <= item_num; iid++) {
				double numerator = 0.0;
				double denom = 0.0;
				for (int i = 0; i < userNeigh && i < simUserInCluster.size(); i++) {
					String[] tmp = simUserInCluster.get(i).split("\t");
					int id = Integer.parseInt(tmp[1]);
					double wui = lambda;
					if (trainData.get(id).containsKey(iid)) {
						wui = 1-wui;
					}
					numerator += wui*getUserSim(uid, id)*(upmat[id-1][iid-1] - userAvg.get(id));
					denom += wui*getUserSim(uid, id);
				}
				ratingMatrix[uid-1][iid-1] = userAvg.get(uid) + numerator/denom;
			}
		}
	}
	
	
	
}
