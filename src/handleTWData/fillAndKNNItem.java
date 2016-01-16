/**
 * xue的方法思想，不过由基于用户改变为基于节目
 */
package handleTWData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * @author xv
 *
 */
public class fillAndKNNItem  extends MethodBasedOnSimilarityTW{
//	double[][] itemSim = new double[item_num][item_num];
	
	public void itemKNNFill() {
		int allCnt = 0;
		long start = System.currentTimeMillis();
		System.out.println("Start to itemKNNFill...");
		for (int i = 0; i < user_num; i++) {
			ArrayList<Integer> users = new ArrayList<Integer>();
			Set<Integer> keys = userCluster.keySet();
			Iterator<Integer> iterator = keys.iterator();
			boolean isEnter = false;
			while (iterator.hasNext()) {
				int key = iterator.next();
//				if (i == 4) {
//					ArrayList<Integer> tt = userCluster.get(key);
//					for (int j = 0; j < tt.size(); j++ ) {
//						if (i == tt.get(j)) {
//							System.out.println("contain i=  4  key :" + key);
//						}
//					}
//				}
				if (userCluster.get(key).contains(i)) {
					users = userCluster.get(key);
					isEnter = true;
					break;
				}
			}
//			System.out.println("users.size() = " + users.size());
			if (isEnter == false) {
				System.out.println("users.size() 0    i = " + i);
			}
			for (int j = 0; j < item_num; j++) {
				if (!trainData.get(i+1).containsKey(j+1)) {
					double deltaR = userAvg.get(i+1);
//					ArrayList<Integer> users = new ArrayList<Integer>();
//					Set<Integer> keys = userCluster.keySet();
//					Iterator<Integer> iterator = keys.iterator();
//					while (iterator.hasNext()) {
//						int key = iterator.next();
//						if (userCluster.get(key).contains(i)) {
//							users = userCluster.get(key);
//							break;
//						}
//					}

					
					
					
					
					
//					int cnt = 0;
//					for (int uid = 0; uid < users.size(); uid++) {
//						if (trainData.get(users.get(uid)+1).containsKey(j+1)) {
//							cnt++;
//							deltaR += (trainData.get(users.get(uid)+1).get(j+1) - userAvg.get(users.get(uid)+1));
//						}
//					}
//					if (cnt > 0) {
//						deltaR /= cnt;
//						//linear
//						if (deltaR > 1) {
//							deltaR = 1;
//						}
					
					
					//xiangliang ' compute preference
					int cnt = 0;
					deltaR = 0.0;
					for (int uid = 0; uid < users.size(); uid++) {
						if (trainData.get(users.get(uid)+1).containsKey(j+1)) {
							cnt++;
							deltaR += userSim[i][users.get(uid)]*trainData.get(users.get(uid)+1).get(j+1);
						}
					}
					if (cnt > 0) {
						//linear
						if (deltaR > 1) {
							deltaR = 1;
						}
			

//						upmat[i][j] = 0.0;   //0.2446
//						upmat[i][j] = 0.5;  //0.1708
//						upmat[i][j] = Math.random(); //0.1018
//						upmat[i][j] =  userAvg.get(i+1);  //0.1108
						upmat[i][j] = deltaR;  //0.0798(s1)     0.1328(s2)      0.096(s3)     0.1566(prefer2, s3)   0.1350(s2, p2)　　　0.1553(s1, p2)
						allCnt++;
					}

				}
			}
		}
		System.out.println("allCnt -------------------------------------------------> " + allCnt);
		long end = System.currentTimeMillis();
		System.out.println("itemKNNFill 运行时间：" + (end - start) + "毫秒");
	}
	
}
