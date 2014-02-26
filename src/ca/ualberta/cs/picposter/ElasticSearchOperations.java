package ca.ualberta.cs.picposter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.util.Log;
import ca.ualberta.cs.picposter.controller.PicPosterController;
import ca.ualberta.cs.picposter.model.PicPostModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ElasticSearchOperations {

	public static void pushPicPostModel(final PicPostModel model) {
			Thread thread = new Thread() {
				
				@Override
				public void run() {
					Gson gson = new Gson();
					HttpClient client = new DefaultHttpClient();
					HttpPost request = new HttpPost("http://cmput301.softwareprocess.es:8080/testing/jhodson/");
					
					try{
						String jsonString = gson.toJson(model);
						request.setEntity(new StringEntity(jsonString));
						
						
						HttpResponse response = client.execute(request);
						Log.w("ElasticSearch",response.getStatusLine().toString());
						
						response.getStatusLine().toString();
						HttpEntity entity = response.getEntity();
						
						BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
						String output = reader.readLine();
						while(output != null) {
							output = reader.readLine();
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					
				}
			};
			
			thread.start();
	}
	
	public static void searchPicPost(final String str, final PicPosterController controller, final Activity activity) {
		Thread thread = new Thread() {
			
			@Override
			public void run() {
				try{
				Gson gson = new Gson();
				HttpClient client = new DefaultHttpClient();
				HttpGet searchRequest = new HttpGet("http://cmput301.softwareprocess.es:8080/testing/jhodson/_search?q=" + str);
				
				searchRequest.setHeader("Accept","application/json");


				HttpResponse response = client.execute(searchRequest);
				HttpEntity entity = response.getEntity();
					
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
				String json = "";
				String output;
				while ((output = reader.readLine()) != null) {
					json += output;
					Log.w("ElasticeSearch", json);
				}
				
				
				Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<PicPostModel>>(){}.getType();
				ElasticSearchSearchResponse<PicPostModel> esResponse = gson.fromJson(json, elasticSearchSearchResponseType);
				
				for (ElasticSearchResponse<PicPostModel> r : esResponse.getHits()) {
					final PicPostModel picPostModel = r.getSource();
					//Ui thread code taken from Luky on Feb 25 2014 at
					//http://stackoverflow.com/questions/12850143/android-basics-running-code-in-the-ui-thread
					activity.runOnUiThread(new Runnable(){
						
						@Override
						public void run(){
							controller.addSearchedPicPost(picPostModel);
						}
					});
					
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					
				}
				
			}
		};
		
		thread.start();
}
}
