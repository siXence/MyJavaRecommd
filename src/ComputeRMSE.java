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


public class ComputeRMSE {
	
//	private static final int user_num = user_num;
//	private static final int item_num = item_num;
// private static final int testNum = 20000;
	
	//topway dataset
	private static final int user_num = 2318;
	private static final int item_num = 4358;
	private static final int testNum = 151614;
	
	private static List<Double> testScore = new ArrayList<Double>();
	private static List<Double> preScore = new ArrayList<Double>();
	//user train list
	private static HashMap<Integer,HashMap<Integer, Boolean> >  trainLists = new HashMap<Integer,HashMap<Integer, Boolean> >();
	private static HashMap<Integer,HashMap<Integer, Boolean> >  watchLists = new HashMap<Integer,HashMap<Integer, Boolean> >();
	private static int[][] upmat = new int[user_num][item_num];
	private static double[][] upmat_result = new double[user_num][item_num];
	private static List<List<Integer>> recomPro= new ArrayList<List<Integer>>();
	
	static List<Double> precisionSVD = new ArrayList<Double>();
	static List<Double> recallSVD = new ArrayList<Double>();
	static List<Double> svdF1 = new ArrayList<Double>();
	
	

	
	public static  void getTestScore(String filePath){
    	System.out.println("read testScore data..................");
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
//                        System.out.println("tmp = " + tmp[0] + " ,     " + tmp[1] + ",   " + tmp[2]);
                        double tt = Double.valueOf(tmp[2]);
                        testScore.add(tt);
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
	
	
	public static  void getPreScore(String filePath){
    	System.out.println("read getPreScore data..................");
        try {
                String encoding="GBK";
                File file=new File(filePath);
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
//                        System.out.println("before = " + lineTxt);
//                        lineTxt = lineTxt.substring(0, lineTxt.length()-1);
//                        System.out.println("after = " + lineTxt);
//                        testScore.add(tt);
                    	preScore.add(Double.valueOf(lineTxt));
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
	
	
	public static  void removeTimeStamp(String filePath, String writePath){
    	System.out.println("read getPreScore data..................");
        try {
        	File f = new File(writePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
			    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
			    BufferedWriter writer = new BufferedWriter(write);
        	
                String encoding="GBK";
                File file=new File(filePath);
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
//                        System.out.println("before = " + lineTxt);
//                        lineTxt = lineTxt.substring(0, lineTxt.length()-1);
//                        System.out.println("after = " + lineTxt);
//                        testScore.add(tt);
                    	
                    	String[] tmp = lineTxt.split("\t");
                    	String tt = tmp[0] + "\t" + tmp[1] + "\t" + tmp[2] + "\n";
                    	writer.write(tt);
                    }
//                    System.out.println("watchlists = " + watchLists.get(1)  + ",  aaa = " + watchLists.get(462));
                    read.close();
                    writer.close();
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
     
    }
	
	
	public static  void addNoItems(String filePath){
    	System.out.println("read testScore data..................");
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
//                        System.out.println("tmp = " + tmp[0] + " ,     " + tmp[1] + ",   " + tmp[2]);
                        	upmat[Integer.parseInt(tmp[0])-1][Integer.parseInt(tmp[1])-1] = Integer.parseInt(tmp[2]);
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
	
	
    public static void writeAllUpmat(String writePath) {
	    try {
		    File f = new File(writePath);
		    if (!f.exists()) {
		    	f.createNewFile();
		    }
			    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
			    BufferedWriter writer = new BufferedWriter(write);
			    for (int i = 0; i < user_num; i++) {
			    	for (int j = 0; j < item_num; j++) {
			    		String tmp = String.valueOf(i+1) + "\t" + String.valueOf(j + 1) + "\t" + String.valueOf(upmat[i][j]) + "\n";
			    		writer.write(tmp);
			    	}
			    }
			   
			    writer.close();
		} catch (Exception e) {
		    e.printStackTrace();
	    }
    }
	
	
	//Get the items that user watch in training set
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
                        	trainLists.put(lastUserID, new HashMap<Integer, Boolean>());
                        }
                        int itemID = Integer.valueOf(tmp[1]);
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
    
    
    
    public static void getTestList(String filePath){
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
                        	watchLists.put(lastUserID, new HashMap<Integer, Boolean>());
                        }
                        int itemID = Integer.valueOf(tmp[1]);
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
    
    
    

    
    
    
    //Get Top-N lists
    public static void getRecommOrder(String filePath) {
    	try {
            String encoding="GBK";
            File file=new File(filePath);
            int lastUserID = -1;
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader( new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while (true) {
                	int  cnt = 0;
                	List<String> record = new ArrayList<String>();
                	while ((lineTxt = bufferedReader.readLine()) != null) {
                		cnt++;
//                		System.out.println("to check the last char --->" + lineTxt);
//                		System.out.println("to check the last char  without last char--->" + lineTxt.substring(0, lineTxt.length()-1));
                		String tmp = lineTxt + "\t" + cnt + "\t" + "xxxx";
                		record.add(tmp);
                		if (cnt == item_num) {
                			break;
                		}
                	}
                	
                	if (lineTxt == null) {
                		break;
                	}
                	
                	List<Integer> proIDs = new ArrayList<Integer>();
                	Collections.sort(record);
                	Collections.reverse(record);
                	for (String str:record) {
                		String[] infor = str.split("\t");
                		int itemID = Integer.valueOf(infor[1]);
                		proIDs.add(itemID);
                	}
                	recomPro.add(proIDs);
                	
                }
    }else{
        System.out.println("找不到指定的文件");
    }
    } catch (Exception e) {
        System.out.println("读取文件内容出错");
        e.printStackTrace();
    }
    }
    
    
    
    
    public static void getPrecisionAndRecallForSVD() {
    	System.out.println("getPrecisionAndRecall..................");
    	int cnt = 0;
    	System.out.println("watchLists = " + watchLists);
    	for (int j = 1; j <= 100; j++) { 
    		int userID = 0;
    		for (List<Integer> rl : recomPro) {
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
    		
//    		System.out.println("cnt = " + cnt);
			double p = ((double)cnt)/(j*user_num);
			double r = ((double)cnt)/testNum;
			precisionSVD.add(p);
			recallSVD.add(r);
			svdF1.add(2*p*r/(p+r));
    	}
    }
    
    
    
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
    
    
	public static  double  getRMSEfromAllPredition(String filePath){
    	System.out.println("read testScore data..................");
    	double rmse = 0.0;
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
//                        System.out.println("tmp = " + tmp[0] + " ,     " + tmp[1] + ",   " + tmp[2]);
                        double tt = Double.valueOf(tmp[2]);
                        int uid = Integer.parseInt(tmp[0])-1;
                        int iid  = Integer.parseInt(tmp[1])-1;
                        rmse += (upmat_result[uid][iid]-tt)*(upmat_result[uid][iid]-tt);
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
		rmse /= 20000;
		rmse = Math.sqrt(rmse);
        return rmse;
     
    }
    
	public static  void getAllPred(String filePath){
    	System.out.println("read testScore data..................");
        try {
                String encoding="GBK";
                File file=new File(filePath);
                if(file.isFile() && file.exists()){ //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file),encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
//                    while((lineTxt = bufferedReader.readLine()) != null){
//                        String[] tmp = lineTxt.split("\t");
////                        System.out.println("tmp = " + tmp[0] + " ,     " + tmp[1] + ",   " + tmp[2]);
//                        	upmat_result[Integer.parseInt(tmp[0])-1][Integer.parseInt(tmp[1])-1] =  Double.valueOf(tmp[2]);
//                    }
                    
                    int uid = 0;
                    while (true) {
                    	int  cnt = 0;
                    	while ((lineTxt = bufferedReader.readLine()) != null) {
                    		upmat_result[uid][cnt] = Double.valueOf(lineTxt);
                    		cnt++;
//                    		System.out.println("to check the last char --->" + lineTxt);
//                    		System.out.println("to check the last char  without last char--->" + lineTxt.substring(0, lineTxt.length()-1));
                    		
                    		if (cnt == item_num) {
                    			break;
                    		}
                    	}
                    	uid++;
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
	 * add all items to test set, if user has not rate item, then value is 0, use this file to get top-N list prediction
	 */
    public static void addAllItemsToTestSet() {
//		//Add 0 to user-item matrix
		String file = "/home/xv/DataForRecom/tw-data/ua.test";
		addNoItems(file);
		String fileout = "/home/xv/DataForRecom/tw-data/u1all.test";
		writeAllUpmat(fileout);
		System.out.println("Done");
    }
    
    /**
     * Get precision and recall from the prediction of SVDFeature, and calculate the RMSE
     */
    public static void getPrecisionAndRecall() {
    	String filePret = "result/pred.txt";
		String fileTrain = "ua.base";
		String fileTest = "ua.test";
		getTrainList(fileTrain);
		getTestList(fileTest);
		getRecommOrder(filePret);
		getPrecisionAndRecallForSVD();
        write("precisionSVDFeature.xls", precisionSVD);
        write("recallSVDFeature.xls", recallSVD);
      write("F1SVDFeature .xls", svdF1);

		getAllPred(filePret);
		double rmse = getRMSEfromAllPredition(fileTest);
		System.out.println("RMSE = " + rmse);
      
    }
	
	
	public static void main(String[] args) throws Exception {
//		String filePret = "/home/xv/DataForRecom/tw-data/pred.txt";
//		String fileTest = "/home/xv/DataForRecom/saveData/ua.test";
//		getPreScore(filePret);
//		getTestScore(fileTest);
//		
//		double rmse = 0.0;
//		for (int i = 0; i < preScore.size(); i++) {
//			rmse += (preScore.get(i)- testScore.get(i))*(preScore.get(i)- testScore.get(i));
//		}
//		rmse /= preScore.size();
//		rmse = Math.sqrt(rmse);
//		System.out.println("RMSE = " + rmse);
		
		
//		//remove timeStamp
//		String f1 = "u1.base";
//		String f1out = "ua1.base";
//		removeTimeStamp(f1, f1out);
//		f1 = "u1.test";
//		f1out = "ua1.test";
//		removeTimeStamp(f1, f1out);
//		System.out.println("Done");
		
		
			
//		//Add 0 to user-item matrix
//		String file ="/home/xv/DataForRecom/saveData/ua.test";
//		addNoItems(file);
//		String fileout = "/home/xv/DataForRecom/saveData/uall.test";
//		writeAllUpmat(fileout);
//		System.out.println("Done");
		

		//Compute precision and recall from pred.txt
		String filePret = "/home/xv/DataForRecom/tw-data/pred.txt";
		String fileTrain = "/home/xv/DataForRecom/saveData/ua.base";
		String fileTest = "/home/xv/DataForRecom/saveData/ua.test";
		getTrainList(fileTrain);
		getTestList(fileTest);
		getRecommOrder(filePret);
		getPrecisionAndRecallForSVD();
        write("/home/xv/DataForRecom/saveData/precisionSVDpp.xls", precisionSVD);
        write("/home/xv/DataForRecom/saveData/recallSVDpp.xls", recallSVD);
      write("/home/xv/DataForRecom/saveData/f1SVDpp.xls", svdF1);
		
		
		
//		//RMSE for all prediction
//		String filePret = "/home/xv/DataForRecom/tw-data/pred.txt";
//		getAllPred(filePret);
//		String fileTest = "/home/xv/DataForRecom/tw-data/u1.test";
//		double rmse = getRMSEfromAllPredition(fileTest);
//		System.out.println("RMSE = " + rmse);
		
		
		
		
//		addAllItemsToTestSet();
		
		
//		getPrecisionAndRecall();
      
      System.out.println("Done");
	}

}
