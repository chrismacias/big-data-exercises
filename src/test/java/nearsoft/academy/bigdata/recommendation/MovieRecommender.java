package nearsoft.academy.bigdata.recommendation;


import org.apache.commons.io.LineIterator;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created by root on 19/08/15.
 */
public class MovieRecommender {
    private static HashMap products = new HashMap();
    private static HashMap users = new HashMap();
    private static PrintWriter writer;
    private static UserBasedRecommender recommender;
    private static int userID;
    private static int productID;
    private static int reviewCount;
    public static void main ( String args[]){
        File file = new File("movies.txt.gz");
        //File file = new File("testing.txt.gz");
        userID = 1;
        productID = 1;
        reviewCount = 0;
        try {
            writer = new PrintWriter("the-file-name.txt", "UTF-8");
            BufferedReader gzis = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new FileInputStream(file))));

            int i=0;
            //lineIterator = FileUtils.lineIterator(file);
            String reviewArray[] = new String[3];
            String line ;
            while (/*lineIterator.hasNext()*/(line = gzis.readLine()) != null) {
                Pattern pattern = Pattern.compile("((product\\/productId):\\s)|((review\\/userId):\\s)|((review\\/score):\\s)");
                Matcher matcher = pattern.matcher(line);
                if(matcher.find()){
                    reviewArray[i]=line.substring(matcher.group().length(),line.length());
                    if(i==2){
                        processReview(reviewArray);
                        reviewArray = new String[3];
                        i=-1;
                    }
                    i++;
                }
            }
        }catch(IOException e){
            System.out.printf(e.getMessage());
        }
        finally {
            writer.close();
        }
        System.out.println("Product count: " + productID);
        System.out.println("Review count: " + reviewCount);
        System.out.println("User count: " + userID);
        try {
            test();
        } catch (TasteException e) {
            e.printStackTrace();
        }
    }

    public static void test() throws TasteException {
        machineLearning();
        List lista = getRecommendationsForUser("A141HP4LYPWMSR");
        System.out.println(lista);
    }

    //=======
    public int getTotalReviews(){
        int totalReviews = reviewCount;

        return totalReviews;
    }

    private static void processReview(String reviewArray[]){
        String user;
        String product;
        reviewCount++;
        if (!products.containsKey(reviewArray[0])) {//check if product exists
            products.put(reviewArray[0], productID);//new product
            product = Integer.toString(productID);
            productID++;
        } else {
            product = products.get(reviewArray[0]).toString();
        }

        if (!users.containsKey(reviewArray[1])) {//check if user
            users.put(reviewArray[1],userID);
            user = Integer.toString(userID);
            userID++;
        } else {//
            user = users.get(reviewArray[1]).toString();
        }
        writer.println(user +"," +product + "," + reviewArray[2]);
    }

    public int getTotalProducts(){
        int totalProducts = productID-1;


        return totalProducts;
    }

    public int getTotalUsers(){
        int totalUsers = userID-1;

        return totalUsers;
    }

    public static List getRecommendationsForUser(String userString) throws TasteException {
        int user = Integer.parseInt(users.get(userString).toString());
        List recommendationsRaw = recommender.recommend(user, 3);
        List recommendations = null;
       for(int i = 0; i<recommendationsRaw.size();i++){
           GenericRecommendedItem temp = (GenericRecommendedItem) recommendationsRaw.get(i);
            Long itemID = temp.getItemID();
            System.out.println(itemID);
            recommendations.add(products.get(itemID.toString()));
        }
        return recommendations;
    }

    public static void machineLearning(){
        try {
            DataModel model = new FileDataModel(new File("the-file-name.txt"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);


        }catch(IOException e){
            System.out.println(e.getMessage());
        } catch (TasteException e) {
            e.printStackTrace();
        }
    }
}
