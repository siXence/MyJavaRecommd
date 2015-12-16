import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.hadoop.item.RecommenderJob;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;//EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


/*
 * 
 * 
 * 基于用户的协同过滤
 * 
 * 利用apache mahout开源项目
 * 
 * 通过基于用户的协同过滤算法，来给用户推荐商品
 * 
 */
public class UserBaseCFMain {

    final static int NEIGHBORHOOD_NUM =20;//邻居数目
    final static int RECOMMENDER_NUM = 100;//推荐物品数目
    private static List<List<Integer>> recomResults = new ArrayList<List<Integer>>();
    private static List<Integer> watchNum = new ArrayList<Integer>();
    private static List<HashMap<Integer, Boolean>> watchLists = new ArrayList<HashMap<Integer, Boolean>>(); //最后多了一个空格
    private static List<List<Double>> precisions = new ArrayList<List<Double>>();
    private static List<List<Double>> recalls = new ArrayList<List<Double>>();
    private static List<Double> p = new ArrayList<Double>();
    private static List<Double> reca = new ArrayList<Double>();
    
    
    

    public static void main(String[] args) throws Exception {
//        String file = "test2.txt";
        String file = "item.txt";
       
      DataModel model =new FileDataModel(new File(file));//FileDataModel要求文件数据存储格式（必须用“，”分割）：userID,itemID[,preference[,timestamp]]
      UserSimilarity user = new PearsonCorrelationSimilarity(model);
//      UserSimilarity user = new PearsonCorrelationSimilarity(model);
        NearestNUserNeighborhood neighbor = new NearestNUserNeighborhood(NEIGHBORHOOD_NUM, user, model);
        Recommender r = new GenericUserBasedRecommender(model, neighbor, user);
        LongPrimitiveIterator iter = model.getUserIDs();

        while (iter.hasNext()) {
        	List<Integer> recomRes = new ArrayList<Integer>();
            long uid = iter.nextLong();
            List<RecommendedItem> list = r.recommend(uid, RECOMMENDER_NUM);
//            System.out.printf("uid:%s", uid);
            for (RecommendedItem ritem : list) {
            	if (uid == 310) {
            		System.out.printf("(%s,%f)", ritem.getItemID(), ritem.getValue());
            	}
//                System.out.printf("(%s,%f)", ritem.getItemID(), ritem.getValue());
            	recomRes.add((int) ritem.getItemID());
            }
//            System.out.println();
            recomResults.add(recomRes);
        }
        
       
		String encoding="GBK";
		File fileCnt=new File("my_cntnum1.txt");
		if(fileCnt.isFile() && fileCnt.exists()){ //判断文件是否存在
		    InputStreamReader read = new InputStreamReader(
		    new FileInputStream(fileCnt),encoding);//考虑到编码格式
		    BufferedReader bufferedReader = new BufferedReader(read);
		    String lineTxt = null;
		    while((lineTxt = bufferedReader.readLine()) != null){
		        watchNum.add(Integer.valueOf(lineTxt));
		    }
//		    System.out.println("watchNum = " + watchNum);
		    read.close();
		}
		
		
		fileCnt=new File("my_cntrecord1.txt");
		if(fileCnt.isFile() && fileCnt.exists()){ //判断文件是否存在
		    InputStreamReader read = new InputStreamReader(
		    new FileInputStream(fileCnt),encoding);//考虑到编码格式
		    BufferedReader bufferedReader = new BufferedReader(read);
		    String lineTxt = null;
		    while((lineTxt = bufferedReader.readLine()) != null){
		    	String[] proIDs = lineTxt.split(" ");
//		    	List<Integer> watchList = new ArrayList<Integer>();
		    	HashMap<Integer, Boolean> watchList = new HashMap<Integer, Boolean>();
//		    	System.out.println("proIDs.length = " + proIDs.length);
		    	for (int i = 0; i < proIDs.length-1; i++) {
		    		watchList.put(Integer.valueOf(proIDs[i]), true);
		    	}
		    	watchLists.add(watchList);
//		    	System.out.println("watchList = " + watchLists.get(watchLists.size()-1));
		    }
		    read.close();
		}
		
		
		int cnt = 0;
		int userID = 0;
		for (int ide = 0; ide < recomResults.size(); ide++) {
			List<Integer> res = recomResults.get(ide);
			if (ide == 3999) {
				System.out.println("size = " + res);
			}
//		for (List<Integer> res : recomResults) {
			cnt = 0;
			int wN = watchNum.get(userID);
			HashMap<Integer, Boolean> watchList = watchLists.get(userID);
			List<Double> precision = new ArrayList<Double>();
			List<Double> recall = new ArrayList<Double>();
			int i = 0;
			for (int id:res) {
				if (watchList.containsKey(id)) {
					cnt++;
				}
				precision.add(((double)cnt)/(i+1));
				recall.add(((double)cnt)/wN);
				i++;
			}
//			System.out.println("cnt = " + cnt);
			if (ide == 3999) {
				System.out.println("precision = " + precision);
			}
			userID++;
//			System.out.println("precision :" + precision);
			precisions.add(precision);
			recalls.add(recall);
		}
//		System.out.println("precision 0  :" + precisions.get(0));
//		System.out.println("precision 0  :" + precisions.get(1));
//		System.out.println("precision 0  :" + precisions.get(precisions.size()-1).get(0));
		//平均准确率和召回率
		int userNum = recomResults.size();
		for (int i = 0; i < RECOMMENDER_NUM; i++) {
			double tmp = 0.0;
//			System.out.println("precisions.size() = " + precisions.size());
			
			for (List<Double> precision:precisions) {
				if (precision.size() > i) {
					tmp += precision.get(i);
				}
			}
			p.add(tmp/userNum);
			tmp = 0.0;
			for (List<Double> recall:recalls) {
				if (recall.size() > i) {
					tmp += recall.get(i);
				}
			}
			reca.add(tmp/userNum);
		}
		
		write("precisionUser.txt", p);
		write("recallUser.txt", reca);
		
		System.out.println("Done");
		
		
		
    }
    
    
	    /**
	     * 写入文件
	    *
	     * @param writePath
	     * 文件路径
	    */
	    public static void write(String writePath, List<Double> list) {
		    try {
			    File f = new File(writePath);
			    if (!f.exists()) {
			    	f.createNewFile();
			    }
				    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
				    BufferedWriter writer = new BufferedWriter(write);
				    int userID = 1;
				    for (Double ele:list) {
				    	String tmp = String.valueOf(userID++) + "," + String.valueOf(ele) + "\r\n";
				    	writer.write(tmp);
				    }
				    writer.close();
			} catch (Exception e) {
			    e.printStackTrace();
		    }
	    }
    
    
        
    
    
}








