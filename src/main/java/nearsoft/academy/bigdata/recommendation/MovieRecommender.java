package nearsoft.academy.bigdata.recommendation;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


import javax.xml.crypto.Data;
import java.io.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
*/

import static javax.imageio.ImageIO.getCacheDirectory;
import static javax.imageio.ImageIO.read;

public class MovieRecommender
{

    private final GenericUserBasedRecommender recommender;
    private HashMap<String, Integer> products = new HashMap<>();
    private HashMap<String, Integer> users = new HashMap<>();
    private int totalReviews = 0;


    public static void main(String[] args)
    {
        System.out.println("Started...");

        try
        {
            MovieRecommender recommendation = new MovieRecommender();
            recommendation.getRecommendationsForUser("A141HP4LYPWMSR");


        }
        catch (IOException | TasteException e)
        {
            e.printStackTrace();
        }

    }


    MovieRecommender() throws IOException, TasteException
    {
       // source = src;
        File cvs1 = new File(generateCsv());
        DataModel model = new FileDataModel(cvs1);

        UserSimilarity similarity;
        similarity = new PearsonCorrelationSimilarity(model);

        UserNeighborhood neighborhood;
        neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

        recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    }

    private String generateCsv()
    {
        // timer
        long starttime = System.currentTimeMillis();

        // some stuff needed
        String productId = null, userId = null, score = null, line, nextline;
        int product = 0, user = 0;
        boolean isproduct = false, isuser = false, isscore = false;

        try
        {
            //read file

            File csv = new File(getCacheDirectory(), "/home/brian/IdeaProjects/big-data-exercises/src/main/resources/results.csv");

            if (csv.exists())
            {
                csv.delete();
                csv.createNewFile();
            }
            else
            {
                csv.createNewFile();
            }

            FileWriter fw = new FileWriter(csv);
            Writer writer = new BufferedWriter(fw);

            String source = "/home/brian/IdeaProjects/big-data-exercises/src/main/resources/movies.txt";
            File movies = new File(source);
            FileReader fr = new FileReader(movies);
            BufferedReader reader = new BufferedReader(fr);
            line = reader.readLine();

            int i=0;
            System.out.println("Generating csv file...");
            while (line != null)
            {

                if (line.startsWith("product/productId"))
                {
                    productId = getValue(line);

                    if (!products.containsKey(productId))           //If the key is new, it's saved
                    {
                        products.put(productId, products.size());
                    }

                    product = products.get(productId);

                    i=1;                                            // i==1 means next element should be 'userID'


                    if ((nextline = reader.readLine()) != null) line = nextline;
                    //System.out.println(i+" "+line);
                }

                if (line.startsWith("review/userId"))
                {

                    userId = getValue(line);

                    if (!users.containsKey(userId))                 //If the key is new, it's saved
                    {
                        users.put(userId, users.size());
                    }
                    user = users.get(userId);

                    i=2;                                            // i==2 means next element should be 'score'


                    reader.readLine();
                    reader.readLine();

                }

                if (line.startsWith("review/score")) {

                    score = getValue(line);

                    writer.write(user + "," + product + "," + score + "\n");   //writes user, product and score in the csv file
                    totalReviews++;
                    i = 0;                                          // i==0 means next element should be 'productID'

                    reader.readLine();
                    reader.readLine();
                    reader.readLine();
                    reader.readLine();
                }


                line = reader.readLine();

            }


            writer.close();

            long finishtime = System.currentTimeMillis();
            long totaltime = starttime - finishtime;

            System.out.println("It took " + (totaltime/-1000) + " seconds to create the csv file...");
            System.out.println("Total Reviews: "+getTotalReviews());
            System.out.println("Total Products: "+getTotalProducts());
            System.out.println("Total Users: "+getTotalUsers());
            return csv.getAbsolutePath();

        }

        catch (IOException e)
        {
        e.printStackTrace();
        }

        return "";
    }
    private String getValue(String line)
    {
        return line.substring(line.indexOf(" ") + 1);
    }

    public int getTotalReviews()
    {
        return totalReviews;
    }

    public int getTotalProducts()
    {
        return products.size();
    }

    public int getTotalUsers()
    {
        return users.size();
    }

    public List<String> getRecommendationsForUser(String userId) throws TasteException
    {

        List<String> recomendedMovies = new ArrayList<>();

        List<RecommendedItem> recommendations = recommender.recommend(users.get(userId), 3);

        for (RecommendedItem recommendation : recommendations)
        {
            System.out.println("User: "+userId+ " Movie recommended: "+recommendation.getItemID());

            for(Map.Entry<String, Integer> entry : products.entrySet())
            {
                if(entry.getValue() == recommendation.getItemID())
                {
                    System.out.println(entry);
                    recomendedMovies.add(entry.getKey());
                }
            }
        }

        return recomendedMovies;
    }
}
