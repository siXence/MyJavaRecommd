import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.RandomUtils;

public class TestIRStatsEvaluator {

	/**
	 * @param args
	 * @throws IOException
	 * @throws TasteException
	 */
	public static void main(String[] args) throws IOException, TasteException {
		// TODO Auto-generated method stub
		RandomUtils.useTestSeed();//保证每次运行时训练数据和测试数据选取的都一样，保证每次评估结果都一样
		String filepath="info.csv";
        DataModel dataModel=new FileDataModel(new File(filepath));
		//创建评估器
		RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();
		//构建推荐引擎
		RecommenderBuilder recommenderBuilder = new RecommenderBuilder() {

			public Recommender buildRecommender(DataModel dataModel)
					throws TasteException {
				// TODO Auto-generated method stub
				UserSimilarity similarity = new PearsonCorrelationSimilarity(
						dataModel);
				UserNeighborhood neighborhood = new NearestNUserNeighborhood(80,
						similarity, dataModel);

				return new GenericUserBasedRecommender(dataModel, neighborhood,
						similarity);
			}
		};
		int at = 2;// 评估推荐两个ITEM时的查准率和查全率
		//开始评估查准率和查全率
		IRStatistics statistics = evaluator.evaluate(recommenderBuilder, null,
				dataModel, null, at,
				GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 1.0);

		System.out.println("precision at " + at + " 查准率:"
				+ statistics.getPrecision());
		System.out
				.println("recall at " + at + " 查全率:" + statistics.getRecall());

	}
}
