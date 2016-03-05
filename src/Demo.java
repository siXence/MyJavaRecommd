import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.RandomUtils;


public class Demo {
	static List<Double> precisionUser = new ArrayList<Double>();
	static List<Double> recallUser = new ArrayList<Double>();
	static List<Double> userF1 = new ArrayList<Double>();
	static List<Double> precisionItem = new ArrayList<Double>();
	static List<Double> recallItem = new ArrayList<Double>();
	static List<Double> precisionSVD = new ArrayList<Double>();
	static List<Double> recallSVD = new ArrayList<Double>();
	
	private static List<List<Integer>> userRecomProID = new ArrayList<List<Integer>>();
	private static List<List<Integer>> itemRecomProID = new ArrayList<List<Integer>>();
	private static List<List<Integer>> svdRecomProID = new ArrayList<List<Integer>>();
	
    public void userbased(DataModel model,int n) throws TasteException{
        System.out.println("-----------------------------------------------------------------------------");
        RandomUtils.useTestSeed();
        final int N=n;
        
        
//        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
//
//      //选择邻居用户，使用NearestNUserNeighborhood实现UserNeighborhood接口，选择邻近的4个用户
//      UserNeighborhood neighborhood = new NearestNUserNeighborhood(N, similarity, model);
//
//      Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
//
//      //给用户1推荐4个物品
//      List<RecommendedItem> recommendations = recommender.recommend(1, 4);
//
//      for (RecommendedItem recommendation : recommendations) {
//          System.out.println(recommendation.getItemID());
//          
//      }
        
        
        
        
        RecommenderEvaluator evaluator=new RMSRecommenderEvaluator();
        RecommenderIRStatsEvaluator statsEvaluator = new GenericRecommenderIRStatsEvaluator();
        RecommenderBuilder builder=new RecommenderBuilder(){
        @Override
            public Recommender buildRecommender(DataModel model)
                    throws TasteException {
                // TODO Auto-generated method stub
                UserSimilarity similarity=new PearsonCorrelationSimilarity(model);
//                UserSimilarity similarity=new CityBlockSimilarity(model);
//        	 	UserSimilarity similarity=new EuclideanDistanceSimilarity(model);
                
                UserNeighborhood neighborhood=new NearestNUserNeighborhood(N,similarity,model);
                Recommender recommend=new GenericUserBasedRecommender(model,neighborhood,similarity);
                return recommend;
            }
        };
//        List<RecommendedItem> recommendations =((AbstractRecommender) builder).recommend(1, 4);
//
//        for (RecommendedItem recommendation : recommendations) {
//            System.out.println(recommendation);
//        }
        
        double score=evaluator.evaluate(builder, null, model, 0.8, 1.0);
        System.out.println("UserBased "+N+"  score is"+score);
        
//        IRStatistics stats11 = statsEvaluator.evaluate(builder,null, model, null, 20,
//                GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,1.0);
////        IRStatistics stats11 = statsEvaluator.evaluate(builder,null, model, null, 30,
////                0,1.0);
//        System.out.println(stats11.getPrecision());
//        System.out.println(stats11.getRecall());
        
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
        System.out.println("-----------------------------------------------------------------------------");

        
        RecommenderEvaluator evaluator=new RMSRecommenderEvaluator();
        RecommenderBuilder builder=new RecommenderBuilder(){
            @Override
            public Recommender buildRecommender(DataModel model)
                    throws TasteException {
                // TODO Auto-generated method stub
//                ItemSimilarity similarity=new PearsonCorrelationSimilarity(model); 
            	ItemSimilarity similarity=new CityBlockSimilarity(model); 
//            	ItemSimilarity similarity=new EuclideanDistanceSimilarity(model); 
            	
                Recommender recommend=new GenericItemBasedRecommender(model,similarity);
                return recommend;
            }
        };   	
        double score=evaluator.evaluate(builder, null, model, 0.8, 1.0);
        System.out.println("ItemBased score is "+score);
        
//        RecommenderIRStatsEvaluator statsEvaluator = new GenericRecommenderIRStatsEvaluator();
//
//	     IRStatistics stats11 = statsEvaluator.evaluate(builder,
//	             null, model, null, 20,
//	             GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,
//	             1.0);
////	        IRStatistics stats11 = statsEvaluator.evaluate(builder,null, model, null, 30,
////	                0,1.0);
//	     System.out.println(stats11.getPrecision());
//	     System.out.println(stats11.getRecall());
//	     
//	     for (int i = 1; i <= 100; i++) {
//	    	 System.out.println("------"+i+"--------");
//        	 IRStatistics stats = statsEvaluator.evaluate(builder,null, model, null, i,
//                     GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,1.0);
////             System.out.println(stats.getPrecision());
////             System.out.println(stats.getRecall());
//             System.out.println("precision = " + stats.getPrecision());
//             System.out.println("recall = " + stats.getRecall());
//        	 precisionItem.add(stats.getPrecision());
//        	 recallItem.add(stats.getRecall());
//        }
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
        RecommenderEvaluator evaluator=new RMSRecommenderEvaluator();
        RecommenderBuilder builder=new RecommenderBuilder(){

            @Override
            public Recommender buildRecommender(DataModel model)
                    throws TasteException {
                // TODO Auto-generated method stub
                return new SVDRecommender(model,new ALSWRFactorizer(model,8,10,50));
            }
            
        };
        double score=evaluator.evaluate(builder, null, model, 0.8, 1.0);
        System.out.println("SVD score is "+score);
        
        
        RecommenderIRStatsEvaluator statsEvaluator = new GenericRecommenderIRStatsEvaluator();
        
        IRStatistics stats11 = statsEvaluator.evaluate(builder,null, model, null, 20,
                GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,1.0);
        System.out.println(stats11.getPrecision());
        System.out.println(stats11.getRecall());

        for (int i = 1; i <= 100; i++) {
        	System.out.println("------"+i+"--------");
	       	 IRStatistics stats = statsEvaluator.evaluate(builder,null, model, null, i,
	                    GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,1.0);
	//            System.out.println(stats.getPrecision());
	//            System.out.println(stats.getRecall());
	       	 precisionSVD.add(stats.getPrecision());
	       	 recallSVD.add(stats.getRecall());
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
    
    
    public static void getPrecisionAndRecall() {
    	
    }
    
    
    public static void main(String[] args) throws Exception{
//        String filepath="itemAll.txt";
//    	String filepath="u.csv";
    	String filepath= "/home/xv/DataForRecom/saveData/smoothData.csv";
    	
//    	String filepath="ratings.txt";
        DataModel model=new FileDataModel(new File(filepath));
        Demo demo=new Demo();
        
        demo.userbased(model, 50);
        write("/home/xv/DataForRecom/saveData/precisionUser.txt", precisionUser);
        write("/home/xv/DataForRecom/saveData/recallUser.txt", recallUser);
        write("/home/xv/DataForRecom/saveData/F1User.txt", userF1);
        
//        write("precisionUserTW.txt", precisionUser);
//        write("recallUserTW.txt", recallUser);
  
//        demo.itembased(model);
////        write("precisionItemML.txt", precisionItem);
////        write("recallItemML.txt", recallItem);
//        
////        write("precisionItemTW.txt", precisionItem);
////        write("recallItemTW.txt", recallItem);
//        
////        demo.slope_one(model);
//        demo.SVD(model);
////        write("precisionSVDML.txt", precisionSVD);
////        write("recallSVDML.txt", recallSVD);
//        
////        write("precisionSVDTW.txt", precisionSVD);
////        write("recallSVDTW.txt", recallSVD);
//        
        System.out.println("Done");
    }
}