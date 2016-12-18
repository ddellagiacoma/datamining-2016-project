# Data Mining

## Four different ways to predict reviews' rating through text analysis

For the development we decided to use Java as programming language. We created a Java-Apache-Maven project using Apache Maven (version 4.0.0) and the Java Development Kit version 1.7. We wrote our project using Eclipse Mars 1 as development environment.

The main force of our algorithm is that it could be used for any reviews' dataset, irrespective of which kind of object, film or whatever the dataset is about. So we tried to adapt our project in order to use it in the maximum number of datasets. Our training set and dataset were composed with Amazon Product data a dataset which contains product reviews and metadata from Amazon, including 142.8 million reviews spanning May 1996 - July 2014. Amazon Product data is a set of 24 dataset categorized by object category (Books, electronics, Movies, Tv, etc.). Data in these datasets are represented in JSON format. So we decided to use gson 2.2.2 a Java serialization/deserialization library that can convert Java Objects into JSON and back in order to achieve our input phase.

Now we are going explain the implementation of the software in its entirety and our project works , after that step by step, we are going to explain how it is written showing the program's classes and looking further a little bit in their functionality. 

Basically our program requires as input a dataset, a list of positive and a list of negative words (opinion words), an input-review and its overall. The system starts scanning the input review and the reviews in the dataset looking for opinion words in the text, saving in a list which and how many positive and negative words each review scanned contains (opinion list).
 
 ![image](https://cloud.githubusercontent.com/assets/24565161/21294724/19584d04-c544-11e6-91b6-49a46a49ac2f.png)
 
We decided to let users choose which prediction method the software will use (i.e. using cosine similarity, Pearson correlation, Jaccard similarity, linear regression or all of them). According to the user's choice the system confronts the input-review-opinion list generated before with all the dataset review-opinion list. In the event that, the user, picked one of the collaborative filter methods, for each dataset's review will be calculated the similarity (using the similarity method chose) between the dataset-review-opinion list and the input-review-opinion list producing a set of similarity measures. As said before, for prediction we used a WkNN algorithm, using as weight 1/1-similarity (in this way, the more a review is similar to the input review the more will be its meaning).

In order to select which k neighbors take into consideration, the system, using a quicksort algorithm, sorts the generated list of similarity measures in a descending order, picking then the firsts k elements of the sorted list. In order to illustrate how our WkNN algorithm handle the k nearest elements selected to achieve a prediction result, we think that the formula can express the idea in the most meaningful, fastest and easiest way.

![image](https://cloud.githubusercontent.com/assets/24565161/21294727/330f9c98-c544-11e6-9507-984cb51fcc8e.png)

For linear regression, we don't have to compare each review of the dataset with the input one, but to find the right values that modelling a relationship for independent variables denoted x and scalar dependent variables y. Of course this relationship must express in the best way possible our problem 's need. We decided to use as dependent variable the overalls (rating review) and as x the Positive Predict Value (PPV) of the reviews.

![image](https://cloud.githubusercontent.com/assets/24565161/21294730/3eaa2b68-c544-11e6-81b4-957bfe988643.png)

As linear model we used an ordinary least squares regression model with one independent variable developed using Apache Commons Math 3.6.1 API and in particular the Class SimpleRegression.

The project has been built using three classes: Review. java, SimilarityOverall.java and Application.java. The first two classes mentioned are used mostly to store data useful during the whole run of the project. Application.java instead is the main classes and the core of our program.

### Review.java

This class is used to store all the fields of each review in the dataset as private variables. We build the class according to information about the reviews extrapolated in Amazon's dataset (i.e. ID of the reviewer, ID of the product reviewed, name of the reviewer, text of the review, rating of the product, summary of the review, time of the review (unix time and raw), but the class could be changed easily in order to use other review dataset with different structure and information.

The Review Class contains also the Get and Set method for the private class variable mentioned above. Data stored in this class are used in the first part of the Run system.

### SimilarityOverall.java

This class is used to store each review overall of the dataset and the similarity measure between the reviews and the input review.
Get and Set methods for the data stored has been implemented in this class as well. SimilartyOverall class also contains quicksort recursive methods() used to sort in a descending direction, by a quicksort algorithm.

This class is used only in the event that the program has to execute one or more of the collaborative filter methods available.

### Application.java

This is the main class of the project and provides all the methods used for the management of the program run. So now we're going to deepen the functionality of this class in order to understand how in practice the system works, following step by step the Application.java code.

At the beginning the program has to prepare and initialize input variables. First of all, the software scans all the files passed as input. Positive and negative words lists are filled by using a simple Scanner that read the words on the appropriate files then, save them. The dataset is read using a JSON parser and data are saved in a list of Review (Review.java class). Once saved all the reviews in the system, the program has to seeking, for each review, opinion words contained in the review text and then comparing it with those belonging to the input review.

Therefore, in order to count the opinion words occurrences in the review text we implemented the Counters() method which, taking as input a text, return a Map\<key,value> which contains as keys all the opinion words and as value the opinion words occurrences founded in the text. Before scanning, the methods transform the whole text in lower case character and splits, from the words, punctuation marks and special characters in order to avoid some text analysis issues. Then Counters() method starts to scan checking, thanks to Java.lang.String.contains() method, which and how many times the user used opinion words in his/her review text.

The Counters() method has to iterate on each dataset review and on the input review as well. Once finished this process, according to the similarity user's choice, the system must establish the similarity measure between the Map (opinionWords Map), returned by the Counters() method, of the input review and all the maps calculated on the dataset reviews. 

In order to do that, using collaborative filter predictions, we implemented three different methods, one method for each different similarity measure used(CosinSimilarity(), JaccardSimilarity(), PearsonCorrelationCoefficient()). These methods, taking as input the opinionWords map calculated on the input review and iteratively, the opinionWords map of the dataset review, quantify the similarity between these two input by using for each similarity measure the appropriate formula and return the result as Double. Then we will have three different arrays of Double containing the similarity results, obviously grouped according to the collaborative filter method used to quantify it.

Since linear regression uses a totally different approach in order to calculate its model and predict the value required, we need a different representation technique of our problem. In fact, the counters() method that we used for collaborative filter predictions, merges in a single map all the positive and negative opinionWords together (this is because the similarity is calculated taking into consideration a possible correlation between the words written itself and not their meaning). According to our linear model decision, we needed to count the opinion words while preserving their positive and negative division. So we developed counterLinearRegression () method, that counts the opinionWords presents in the review text passed as input and returns the Positive Predict Value of the review.

CounterLineraRegression is called iteratively passing it as input every review in the dataset in order to build the linear regression function. Thanks to the SimpleRegression () class of the Apache Commons Math library, it was pretty easy to implement a linear regression predictor method. In fact, once computed all the Positive Predict Value of every dataset review, we only have to pass them to the API method addData() (Apache Commons Math) on a simpleRegression variable initialized before. AddData() adds the observation (x,y) to the regression data set where x values will be the Positive Predict Value while y values will be the overall of the review.

At this point, the system has prepared all the necessary data both for collaborative filter predictions and for a simple linear regression prediction. For collaborative filter prediction, as said before we handle the prediction by a WkNN algorithm. So the program has to sort the similarity measure lists in a descending direction using quicksort() method written in the SimilarityOverall class in order to peak the most similar reviews to the input review. Then the sorted lists will be passed at the weightedNearestNeighbor() method which is in charge of calculate a prediction using the formula (Figure 2). For linear regression prediction we simply pass the Positive Predict Value to the predict(double x) API method which returns the "predicted" y value associated with the supplied x value, based on the data that has been added to the model when this method is activated.

### Root Mean Square Error

In the end the system prints the results measured on an output file text, plus stores all the runs results in four different txt files (each file cover only a single prediction method), saving the all the prediction given by the system in every run and the “real“ overall given by the user as input. These files (ErrorFiles) will be used by the program in order to calculate the RMSE for each prediction method. The RMSEs will be defined in the output file taking into account all the results written on the ErrorFiles. 

RMSE of course will be calculated on each different prediction method separately, and the values used will be got by scanning the appropriate ErrorFile. To restart from scratch the calculation of the RMSE, forgetting the previous results, for example in order to train the algorithm or to understand the results, it will be needed to delete the ErrorFiles. Doing that the error calculation will start from 0.
