import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.RMSRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
//import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorization;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDPlusPlusFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.RandomUtils;


public class Demo2 {
	static List<Double> precisionUser = new ArrayList<Double>();
	static List<Double> recallUser = new ArrayList<Double>();
	static List<Double> userF1 = new ArrayList<Double>();
	static List<Double> precisionItem = new ArrayList<Double>();
	static List<Double> recallItem = new ArrayList<Double>();
	static List<Double> itemF1 = new ArrayList<Double>();
	static List<Double> precisionSVD = new ArrayList<Double>();
	static List<Double> recallSVD = new ArrayList<Double>();
	static List<Double> svdF1 = new ArrayList<Double>();
	
	
	
	private static HashMap<Integer, Boolean> trainUser = new HashMap<Integer, Boolean>();
	
	private static List<List<Long>> recomPro= new ArrayList<List<Long>>();
//	private static List<List<Long>> recomProUser = new ArrayList<List<Long>>();
//	private static List<List<Long>> recomProItem = new ArrayList<List<Long>>();
//	private static List<List<Long>> recomProSVD = new ArrayList<List<Long>>();
	private static int Tu = 0;
	private static HashMap<Integer,HashMap<Long, Boolean> >  watchLists = new HashMap<Integer,HashMap<Long, Boolean> >();
	private static HashMap<Integer,HashMap<Long, Boolean> >  trainLists = new HashMap<Integer,HashMap<Long, Boolean> >();
	
	private static List<List<Integer>> userRecomProID = new ArrayList<List<Integer>>();
	private static List<List<Integer>> itemRecomProID = new ArrayList<List<Integer>>();
	private static List<List<Integer>> svdRecomProID = new ArrayList<List<Integer>>();
	
	private static int userNum = 943;
//	private static int userNum = 4000;
	
    public void userbased(DataModel model,int n) throws TasteException{
    	recomPro.clear();
        System.out.println("-----------------------------------------------------------------------------");
        RandomUtils.useTestSeed();
        final int N=n;
        
        UserSimilarity similarity=new PearsonCorrelationSimilarity(model); 
//        UserSimilarity similarity=new CityBlockSimilarity(model);
//        UserSimilarity similarity=new LogLikelihoodSimilarity(model);
//      UserSimilarity similarity = new TanimotoCoefficientSimilarity(model);
        
//      UserSimilarity similarity = new UncenteredCosineSimilarity(model);
        

      //选择邻居用户，使用NearestNUserNeighborhood实现UserNeighborhood接口，选择邻近的4个用户
      UserNeighborhood neighborhood = new NearestNUserNeighborhood(N, similarity, model);

      Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
      System.out.println("trainUser =" + trainUser);
      //给用户1推荐4个物品
      for (int i = 1; i <= userNum; i++) {
//    	  System.out.println("-------User " + i + "------");
    	  List<RecommendedItem> recommendations = recommender.recommend(i, 100);
    	  List<Long> proIDs = new ArrayList<Long>();
          for (RecommendedItem recommendation : recommendations) {
//              System.out.println(recommendation.getItemID());
              proIDs.add(recommendation.getItemID());
          }
          if (i == 1) {
        	  System.out.println("recom 1 = " + proIDs);
        	  System.out.println("recom 1 size= " + proIDs.size());
          }
          recomPro.add(proIDs);
      }
//      System.out.println("recomPro = " + recomPro);
      //precision recall
      getPrecisionAndRecall(0);


 
        
        
//        RecommenderEvaluator evaluator=new RMSRecommenderEvaluator();
//        RecommenderIRStatsEvaluator statsEvaluator = new GenericRecommenderIRStatsEvaluator();
//        RecommenderBuilder builder=new RecommenderBuilder(){
//        @Override
//            public Recommender buildRecommender(DataModel model)
//                    throws TasteException {
//                // TODO Auto-generated method stub
//                UserSimilarity similarity=new PearsonCorrelationSimilarity(model);
////                UserSimilarity similarity=new CityBlockSimilarity(model);
////        	 	UserSimilarity similarity=new EuclideanDistanceSimilarity(model);
//                
//                UserNeighborhood neighborhood=new NearestNUserNeighborhood(N,similarity,model);
//                Recommender recommend=new GenericUserBasedRecommender(model,neighborhood,similarity);
//                return recommend;
//            }
//        };
//        List<RecommendedItem> recommendations =((AbstractRecommender) builder).recommend(1, 4);
//
//        for (RecommendedItem recommendation : recommendations) {
//            System.out.println(recommendation);
//        }
//        
//        double score=evaluator.evaluate(builder, null, model, 0.8, 1.0);
//        System.out.println("UserBased "+N+"  score is"+score);
//        
////        IRStatistics stats11 = statsEvaluator.evaluate(builder,null, model, null, 20,
////                GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,1.0);
//////        IRStatistics stats11 = statsEvaluator.evaluate(builder,null, model, null, 30,
//////                0,1.0);
////        System.out.println(stats11.getPrecision());
////        System.out.println(stats11.getRecall());
//        
//        for (int i = 1; i <= 100; i++) {
//        	System.out.println("------"+i+"--------");
//        	 IRStatistics stats = statsEvaluator.evaluate(builder,null, model, null, i,
//                     GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,1.0);
//             System.out.println("precision = " + stats.getPrecision());
//             System.out.println("recall = " + stats.getRecall());
//             System.out.println("caclulate F1 = " + 2*stats.getPrecision()*stats.getRecall()/(stats.getRecall() + stats.getPrecision()));
//             System.out.println("F1 = " + stats.getF1Measure());
//             System.out.println("reach = " + stats.getReach());
//        	 precisionUser.add(stats.getPrecision());
//        	 recallUser.add(stats.getRecall());
//        	 userF1.add(stats.getF1Measure());
//        }
        
       
    }
    
    public void itembased(DataModel model) throws TasteException{
        System.out.println("-----------------------------------------------------------------------------item");
        recomPro.clear();

        
        RecommenderEvaluator evaluator=new RMSRecommenderEvaluator();
        
//        ItemSimilarity similarity=new PearsonCorrelationSimilarity(model); 
        ItemSimilarity similarity=new CityBlockSimilarity(model); 
//    	ItemSimilarity similarity=new EuclideanDistanceSimilarity(model); 
    	
        Recommender recommend=new GenericItemBasedRecommender(model,similarity);
      //给用户1推荐4个物品
        int j = 1;
        System.out.println("trainUser =" + trainUser);
        for (int i = 1; i <= userNum; i++) {
//      	  System.out.println("-------User " + i + "------");
        	if (trainUser.containsKey(i) == false) {
        		System.out.println("-----" + j++);
        		continue;
        	}
        	
      	  List<RecommendedItem> recommendations = recommend.recommend(i, 100);
        
      	  
      	  List<Long> proIDs = new ArrayList<Long>();
            for (RecommendedItem recommendation : recommendations) {
//                System.out.println(recommendation.getItemID());
                proIDs.add(recommendation.getItemID());
            }
            if (i == 1) {
          	  System.out.println("recom 1 = " + proIDs);
            }
            recomPro.add(proIDs);
        }
//        System.out.println("recomPro = " + recomPro);
        //precision recall
        getPrecisionAndRecall(1);
    }
    
//    public void slope_one(DataModel model) throws TasteException{
//        System.out.println("-----------------------------------------------------------------------------");
//        RecommenderEvaluator evaluator=new RMSRecommenderEvaluator();
//        RecommenderBuilder builder=new RecommenderBuilder(){
//
//            @Override
//            public Recommender buildRecommender(DataModel arg0)
//                    throws TasteException {
//                // TODO Auto-generated method stub
//                return new SlopeOneRecommender(arg0);
//            }
//            
//        };
//        double score=evaluator.evaluate(builder, null, model, 0.7, 1);
//        System.out.println("Slope  one score is "+score);
//    }
    
    public void SVD(DataModel model) throws TasteException{
        System.out.println("-----------------------------------------------------------------------------");
        recomPro.clear();
//        RecommenderEvaluator evaluator=new RMSRecommenderEvaluator();
//        RecommenderBuilder builder=new RecommenderBuilder(){
//
//            @Override
//            public Recommender buildRecommender(DataModel model)
//                    throws TasteException {
//                // TODO Auto-generated method stub
//                return new SVDRecommender(model,new ALSWRFactorizer(model,10,0.05,10));
//            }
//            
//        };
        
        SVDPlusPlusFactorizer rrr = new SVDPlusPlusFactorizer(model, 100, 100);
        Factorization rrrd = rrr.factorize();
     
//        SVDPlusPlusFactorizer rdd = new SVDPlusPlusFactorizer(dataModel, numFeatures, learningRate, preventOverfitting, randomNoise, numIterations, learningRateDecay)
        
        Recommender recommender = new SVDRecommender(model,new ALSWRFactorizer(model,8,10,50));
        //给用户1推荐4个物品\
        int j = 0;
        System.out.println("trainUser =" + trainUser);
        for (int i = 1; i <= userNum; i++) {
//      	  System.out.println("-------User " + i + "------");
        	if (trainUser.containsKey(i) == false) {
        		System.out.println("-----" + j++);
        		continue;
        	}
        	
      	  List<RecommendedItem> recommendations = recommender.recommend(i, 100);
      	  List<Long> proIDs = new ArrayList<Long>();
            for (RecommendedItem recommendation : recommendations) {
//                System.out.println(recommendation.getItemID());
                proIDs.add(recommendation.getItemID());
            }
            if (i == 1) {
          	  System.out.println("recom 1 = " + proIDs);
            }
            recomPro.add(proIDs);
        }
//        System.out.println("recomPro = " + recomPro);
        //precision recall
        getPrecisionAndRecall(2);
        
    }
    
    
    
    public static void getRecomFromFile(String filePath) {
    	System.out.println("read test data..................");
    	recomPro.clear();
        try {
                String encoding="GBK";
                File file=new File(filePath);
                int lastUserID = -1;
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
   
//                    List<List<String>> strTmp = new ArrayList<List<String>>();
                    while(true) {
                    	List<String> record = new ArrayList<String>();
                    	while((lineTxt = bufferedReader.readLine()) != null && lineTxt.startsWith("xxx") == false) {
                    		record.add(lineTxt);
                    	}
                    	List<Long> proIDs = new ArrayList<Long>();
                    	Collections.sort(record);
                    	Collections.reverse(record);
                    	for (String str:record) {
                    		String[] infor = str.split("\t");
                    		long itemID = Integer.valueOf(infor[1]);
                    		proIDs.add(itemID);
                    	}
                    	recomPro.add(proIDs);
                    	if (lineTxt == null) {
                    		break;
                    	}
                    }
//                    System.out.println("watchlists = " + watchLists.get(1)  + ",  aaa = " + watchLists.get(462));
                    read.close();
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
     
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
    
    
    public static void readTxtFile(String filePath){
    	System.out.println("read test data..................");
        try {
                String encoding="GBK";
                File file=new File(filePath);
                int lastUserID = -1;
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
                    	Tu++;
//                        System.out.println(lineTxt);
                        String[] tmp = lineTxt.split("\t");
//                        System.out.println("tmp = " + tmp[2]);
                        int userID = Integer.valueOf(tmp[0]);
                        if (userID != lastUserID) {
                        	lastUserID = userID;
                        	watchLists.put(lastUserID, new HashMap<Long, Boolean>());
                        }
                        long itemID = Integer.valueOf(tmp[1]);
                        watchLists.get(userID).put(itemID, true);
                    }
//                    System.out.println("watchlists = " + watchLists.get(1)  + ",  aaa = " + watchLists.get(462));
                    read.close();
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
     
    }
    
    
    public static void getTrainList(String filePath){
    	System.out.println("read test data..................");
        try {
                String encoding="GBK";
                File file=new File(filePath);
                int lastUserID = -1;
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
//                        System.out.println(lineTxt);
                        String[] tmp = lineTxt.split("\t");
//                        System.out.println("tmp = " + tmp[2]);
                        int userID = Integer.valueOf(tmp[0]);
                        if (userID != lastUserID) {
                        	lastUserID = userID;
                        	trainLists.put(lastUserID, new HashMap<Long, Boolean>());
                        }
                        long itemID = Integer.valueOf(tmp[1]);
                        trainLists.get(userID).put(itemID, true);
                    }
//                    System.out.println("watchlists = " + watchLists.get(1)  + ",  aaa = " + watchLists.get(462));
                    read.close();
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
     
    }
    
    
    
    public static void getTrainUser(String filePath){
    	System.out.println("read test data..................");
        try {
                String encoding="GBK";
                File file=new File(filePath);
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
                    	Tu++;
//                        System.out.println(lineTxt);
                        String[] tmp = lineTxt.split("\t");
//                        System.out.println("tmp.size() = " + tmp.length);
//                        System.out.println("tmp = " + tmp[0] + " ,     " + tmp[1] + ",   " + tmp[2]);
                        int userID = Integer.valueOf(tmp[0]);
                        if (!trainUser.containsKey(userID)) {
                        	trainUser.put(userID, true);
                        }
                    }
//                    System.out.println("watchlists = " + watchLists.get(1)  + ",  aaa = " + watchLists.get(462));
                    read.close();
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
     
    }
    
    
    public static void getPrecisionAndRecall(int method) {
    	System.out.println("getPrecisionAndRecall..................");
    	int cnt = 0;
    	for (int j = 1; j <= 100; j++) {
    		int userID = 0;
    		for (List<Long> rl : recomPro) {
    			userID++;
//    			if (j == 3) {
//    				if (userID == 1) {
//    					System.out.println("watchlist = " + watchLists.get(userID));
//    					System.out.println("item 3 , user 1 = " + rl.get(j-1) + ",  isContain = " + watchLists.get(userID).containsKey(rl.get(j-1)));
//    				}
//    			}
    			if (watchLists.containsKey(userID) ) {
//    				System.out.println("true");
    				if (j < rl.size()) {
    					if (watchLists.get(userID).containsKey(rl.get(j-1))) {
        					cnt++;
        				}
    				}
    			}
    		}
//    		System.out.println("cnt = " + cnt);
    		Tu = 20000;
    		
    		if (method == 0) {
    			double p = ((double)cnt)/(j*943);
    			double r = ((double)cnt)/Tu;
    			precisionUser.add(p);
    			recallUser.add(r);
    			userF1.add(2*p*r/(p+r));
    		} else if (method == 1) {
    			double p = ((double)cnt)/(j*943);
    			double r = ((double)cnt)/Tu;
    			precisionItem.add(p);
    			recallItem.add(r);
    			itemF1.add(2*p*r/(p+r));
    		} else if (method == 2) {
    			double p = ((double)cnt)/(j*943);
    			double r = ((double)cnt)/Tu;
    			precisionSVD.add(p);
    			recallSVD.add(r);
    			svdF1.add(2*p*r/(p+r));
    		}
    	}
    }
    
    
    public static void getPrecisionAndRecallForSVD(int method) {
    	System.out.println("getPrecisionAndRecall..................");
    	int cnt = 0;
    	for (int j = 1; j <= 100; j++) {
    		int userID = 0;
    		for (List<Long> rl : recomPro) {
    			userID++;
//    			if (j == 3) {
//    				if (userID == 1) {
//    					System.out.println("watchlist = " + watchLists.get(userID));
//    					System.out.println("item 3 , user 1 = " + rl.get(j-1) + ",  isContain = " + watchLists.get(userID).containsKey(rl.get(j-1)));
//    				}
//    			}
    			if (watchLists.containsKey(userID) ) {
//    				System.out.println("true");
    				if (j < rl.size()) {
    					if (watchLists.get(userID).containsKey(rl.get(j-1)) ) {
    						if (trainLists.containsKey(userID) && trainLists.get(userID).containsKey(rl.get(j-1)) ) {
    							continue;
    						}
        					cnt++;
        				}
    				}
    			}
    		}
//    		System.out.println("cnt = " + cnt);
    		
    		if (method == 0) {
    			double p = ((double)cnt)/(j*943);
    			double r = ((double)cnt)/Tu;
    			precisionUser.add(p);
    			recallUser.add(r);
    			userF1.add(2*p*r/(p+r));
    		} else if (method == 1) {
    			double p = ((double)cnt)/(j*943);
    			double r = ((double)cnt)/Tu;
    			precisionItem.add(p);
    			recallItem.add(r);
    			itemF1.add(2*p*r/(p+r));
    		} else if (method == 2) {
    			double p = ((double)cnt)/(j*943);
    			double r = ((double)cnt)/Tu;
    			precisionSVD.add(p);
    			recallSVD.add(r);
    			svdF1.add(2*p*r/(p+r));
    		}
    	}
    }
    
    
    public static void main(String[] args) throws Exception{
//        String filepath="itemAll.txt";
//    	String filepath="u.csv";
//    	String filepath="ratings.txt";
    	String filepath="u1.base";
    	readTxtFile("u1.test");
    	getTrainList(filepath);
    	getTrainUser(filepath);
    	
        DataModel model=new FileDataModel(new File(filepath));
        Demo2 demo=new Demo2();
        
//        MovieLens
//        demo.userbased(model, 8);
//        write("precisionUserML.txt", precisionUser);
//        write("recallUserML.txt", recallUser);
//        write("F1UserML.txt", userF1);
        
//        write("precisionUserTW.txt", precisionUser);
//        write("recallUserTW.txt", recallUser);
  
//        demo.itembased(model);
//        write("precisionItemML.xls", precisionItem);
//        write("recallItemML.xls", recallItem);
//        write("F1ItemML.xls", itemF1);
//        
////        write("precisionItemTW.txt", precisionItem);
////        write("recallItemTW.txt", recallItem);
//        
////        demo.slope_one(model);
        
        
////        demo.SVD(model);
//        getRecomFromFile("prediction.txt");
//        getPrecisionAndRecallForSVD(2);
//        
//        write("precisionSVDML.xls", precisionSVD);
//        write("recallSVDML.xls", recallSVD);
//      write("F1SVDML.xls", svdF1);
//        
////        write("precisionSVDTW.txt", precisionSVD);
////        write("recallSVDTW.txt", recallSVD);
        
        
        
////      Topway--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
////      String filepath="itemAll.txt";
////  	String filepath="u.csv";
////  	String filepath="ratings.txt";
//  	String filepath="dataTWTrain.txt";
//  	readTxtFile("dataTWTest.txt");
//  	getTrainUser(filepath);
//  	
//      DataModel model=new FileDataModel(new File(filepath));
//      Demo2 demo=new Demo2();
//      
////      demo.userbased(model, 4);
////      write("precisionUserTW.xls", precisionUser);
////      write("recallUserTW.xls", recallUser);
////      write("F1UserTW.xls", userF1);
////
////      demo.itembased(model);
////      write("precisionItemTW.xls", precisionItem);
////      write("recallItemTW.xls", recallItem);
////      write("F1ItemTW.xls", itemF1);
//////
//////////      demo.slope_one(model);
//////      
//      demo.SVD(model);
//      write("precisionSVDTW.xls", precisionSVD);
//      write("recallSVDTW.xls", recallSVD);
//      write("F1SVDTW.xls", svdF1);

        System.out.println("Tu = " + Tu);
        System.out.println("Done");
    }
}