import java.util.HashMap;
import java.util.Map;

public class UserSimilarity {
	DAO dao = new DAO();
	HashMap<String, HashMap<String, Integer>> userHistory = dao.getAllHistory();
	
	public double userSimilarityBasedOnScores(String user1, String user2){
		HashMap<String, Integer> user1History = userHistory.get(user1);
		HashMap<String, Integer> user2History = userHistory.get(user2);
		double similarity = 0;
		double min,max;
		Integer user2score, user1score;
		int count = 0;
		
		for (Map.Entry<String, Integer> entry : user1History.entrySet()) {
			user2score = user2History.get(entry.getKey());
			if(user2score!=null){
				user1score = entry.getValue();
				min = user1score > user2score ? user2score: user1score;
				max = user1score > user2score ? user1score: user2score;
				similarity += min / max;
				count ++;
			}
		}
		
		similarity = similarity / count;
		return similarity;
	}

	public double userSimilarityBasedOnHistory(String user1, String user2){
		HashMap<String, Integer> user1History = userHistory.get(user1);
		HashMap<String, Integer> user2History = userHistory.get(user2);
		double similarity = 0;
		int count = 0;
		
		for (Map.Entry<String, Integer> entry : user1History.entrySet()) {
			if(user2History.containsKey(entry.getKey()))
				count++;
		}
		similarity = count / (user1History.size() + user2History.size());
		return similarity;
	}

}
