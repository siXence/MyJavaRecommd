/**
 * 
 */
package handleTWData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * @author xv
 *
 */
public class Ensemble extends MethodBasedOnSimilarityTW{
	double[][] tmpMatrix = new double[user_num][item_num];
	
	
	public void ensemble() {
			initRatingMatrix();
			String filePath = "/home/xv/DataForRecom/saveData/ua.base";
//		trainData = getData(filePath);
		getTrainData(filePath);
		filePath = "/home/xv/DataForRecom/saveData/ua.test";
		System.out.println("contain = " + trainData.containsKey(13));
		
//		testData = getData(filePath);
		getTestData(filePath);
		
		
		filePath = "/home/xv/DataForRecom/saveData/singelUPrec.txt";
		updateRatingMatrix(filePath);
		filePath = "/home/xv/DataForRecom/saveData/multiUPrec.txt";
		updateRatingMatrix(filePath);
		filePath =  "/home/xv/DataForRecom/saveData/smoothKNNUPrec.txt";
		 updateRatingMatrix(filePath);
		 filePath = "/home/xv/DataForRecom/saveData/SVDUPrec.txt";
		 updateRatingMatrix(filePath);
		 
		 sortItemsForUser();
			
			
//			updateTrainTestSetFromFile() ;
			
			
			getPreAndRecallAndF();
			
//			System.out.println("cluster.size() = " + clusterResult.size());
			long end = System.currentTimeMillis();
//			System.out.println("Our Method 运行时间：" + (end - start) + "毫秒");
	}
	
	public void readUPrec(String filePath) {
		try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int uid = 0;
                double maxV = -2.0;
                while((lineTxt = bufferedReader.readLine()) != null){
                    String[] tmp = lineTxt.split("\t");
//                    System.out.println("tmp.size() = " + tmp.length);
                    
                    for (int i = 0; i < tmp.length; i++) {
                    	double val = Double.valueOf(tmp[i]);
                    	maxV = Math.max(maxV, val);
                    	tmpMatrix[uid][i] = val;
                    }
                    
                    if (maxV > 0) {
                        for (int i = 0; i < tmp.length; i++) {
                        	tmpMatrix[uid][i] /= maxV;
                        }
                    }

                    
                    uid++;
                    
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
	
	public void updateRatingMatrix(String filePath) {
		readUPrec(filePath);
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < item_num; j++) {
				ratingMatrix[i][j] += tmpMatrix[i][j];
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Ensemble ens = new Ensemble();
		ens.ensemble();
		System.out.println("Done!");
	}

}
