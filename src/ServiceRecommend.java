import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceRecommend {
	
	DAO dao = new DAO();
	public ArrayList<String> recommend(String serviceName, String user) throws Exception{
		ArrayList<String> services = dao.getAllServices();
		ArrayList<String> result = new ArrayList<String>();
		HashMap<String, Double> servicePredictValues = new HashMap<String, Double>();
		for(int i = 0; i < services.size(); i++){
			servicePredictValues.put(services.get(i), getPredictValue(services.get(i), user));
		}
		List<Map.Entry<String, Double>> list_Data = new ArrayList<Map.Entry<String, Double>>(servicePredictValues.entrySet());  
	    Collections.sort(list_Data, new Comparator<Map.Entry<String, Double>>()  
	      {   
	          public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2)  
	          {  
	           if(o2.getValue()!=null&&o1.getValue()!=null&&o1.getValue() > o2.getValue()){  
	            return 1;  
	           }else{  
	            return -1;  
	           }  
	              
	          }  
	      });
	    
	    result.add(list_Data.get(0).getKey());
	    result.add(list_Data.get(1).getKey());
	    return result;
	}
	
	public HashMap<String,Double> getSimilarServices(String service) throws Exception{
		ArrayList<String> services = dao.getAllServices();
		HashMap<String,Double> similarServices = new HashMap<String, Double>();
		WADLMatcher match = new WADLMatcher();
		double tempsimilarity;
		for(int i = 0; i < services.size(); i++){
			if(services.get(i).equals(service)) continue;
			tempsimilarity = match.match(service+".WADL", services.get(i)+".WADL");
			if(tempsimilarity > 0.7 ){
				similarServices.put(services.get(i), tempsimilarity);
			}
		}
		return similarServices;
	}
	
	public HashMap<String,Double> getSimilarUsers(String user){
		ArrayList<String> users = dao.getAllUsers();
		HashMap<String, Double> similarUsers = new HashMap<String, Double>();
		UserSimilarity us = new UserSimilarity();
		double tempsimilarity;
		for(int i = 0; i < users.size(); i++){
			if(users.get(i).equals(user)) continue;
			tempsimilarity = 0.6 * us.userSimilarityBasedOnScores(user, users.get(i)) + 0.4 * us.userSimilarityBasedOnHistory(user, users.get(i));
			if(tempsimilarity > 0.7 ){
				similarUsers.put(users.get(i), tempsimilarity);
			}
		}
		return similarUsers;
		
	}
	public double getPredictValue(String user, String service) throws Exception{
		HashMap<String, Integer> serviceHistory = dao.getServiceHistory(service);
		HashMap<String, Integer> userHistory = dao.getUserHistory(user);
		double predictValue = 0;
		
		double averageUserScore = 0, averageServiceScore = 0;
		int countForService = 0, countForUser = 0;
		
		for (Map.Entry<String, Integer> entry : serviceHistory.entrySet()) {
			averageServiceScore += entry.getValue();
			countForService ++;
		}
		averageServiceScore = averageServiceScore / countForService;
		
		for (Map.Entry<String, Integer> entry : userHistory.entrySet()) {
			averageUserScore += entry.getValue();
			countForUser ++;
		}
		averageUserScore = averageUserScore / countForUser;
		
		HashMap<String, Double> similarUsers = getSimilarUsers(user);
		HashMap<String, Double> similarServices = getSimilarServices(service);
		double userNumerator = 0, serviceNumerator = 0, userDenominator = 0, serviceDenominator = 0;
		double tempUserSimilarity, tempUserAverageScore = 0, tempServiceSimilarity, tempServiceAverageScore = 0;
		HashMap<String, Integer> tempSimilarUserHistory, tempSimilarServiceHistory;
		int tempUserCount = 0, tempServiceCount = 0;
		
		for (Map.Entry<String, Double> entry : similarUsers.entrySet()) {
			tempUserSimilarity = entry.getValue();
			tempSimilarUserHistory = dao.getUserHistory(entry.getKey());
			for (Map.Entry<String, Integer> tempentry : tempSimilarUserHistory.entrySet()){
				tempUserCount ++;
				tempUserAverageScore += tempentry.getValue();
			}
			tempUserAverageScore = tempUserAverageScore / tempUserCount;
			userNumerator += tempUserSimilarity * (tempSimilarUserHistory.get(service) - tempUserAverageScore) > 0 ? tempSimilarUserHistory.get(service) - tempUserAverageScore : tempUserAverageScore - tempSimilarUserHistory.get(service) ;
			userDenominator += tempUserSimilarity;
			tempUserCount = 0;
			tempUserAverageScore = 0;
		}
		
		for (Map.Entry<String, Double> entry : similarServices.entrySet()) {
			tempServiceSimilarity = entry.getValue();
			tempSimilarServiceHistory = dao.getServiceHistory(entry.getKey());
			for (Map.Entry<String, Integer> tempentry : tempSimilarServiceHistory.entrySet()){
				tempServiceCount ++;
				tempServiceAverageScore += tempentry.getValue();
			}
			tempServiceAverageScore = tempServiceAverageScore / tempServiceCount;
			serviceNumerator += tempServiceSimilarity * (tempSimilarServiceHistory.get(user) - tempServiceAverageScore) > 0 ? tempSimilarServiceHistory.get(user) - tempServiceAverageScore : tempServiceAverageScore - tempSimilarServiceHistory.get(user);
			serviceDenominator += tempServiceSimilarity;
			tempServiceCount = 0;
			tempServiceAverageScore = 0;
		}
		
		predictValue = 0.5 * (averageUserScore + userNumerator / userDenominator) + 0.5 * (averageServiceScore + serviceNumerator / serviceDenominator);
		
		return predictValue;
		
		
	}
	
	
}
