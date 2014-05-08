/*******************************************************************************
 * Copyright (c) 2014 - David Moreno Briz - Grupo de Sistemas Inteligentes - Universidad Politecnica de Madrid. (GSI-UPM)
 * http://www.gsi.dit.upm.es/
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 *  
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *  
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and  limitations under the License.
 ******************************************************************************/

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gateModules.DictionaryBasedSentimentAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * Servlet implementation class Service
 */
@WebServlet("/RestrictedService")
public class RestrictedService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * To know if GATE has been initialized.
	 */
	private static boolean gateInited = false; 
	
	/**
	 * service.jsp
	 */
	private static String SERVICE_JSP = "/service.jsp";
	
	/**
	 * response.jsp
	 */
	private static String RESPONSE_JSP = "/response.jsp";
	
	/**
	 * A HashMap with the configured sentiment analysis modules that are going be provided by this service.
	 */
	private static HashMap<String,DictionaryBasedSentimentAnalyzer> modules = new HashMap<String, DictionaryBasedSentimentAnalyzer>();
	
	/**
	 * The Mongo data base.
	 */
	private static DB db;
	
    /**
     * Default constructor. 
     */
    public RestrictedService() {
        super();
    }
    
    public void init() throws ServletException { 
        if(!gateInited) { 
          try { 
        	 	// GATE home is setted, so it can be used by SAGA.
              ServletContext ctx = getServletContext(); 
              File gateHome = new File(ctx.getRealPath("/WEB-INF")); 
              Gate.setGateHome(gateHome); 
              // GATE user configuration file is setted. 
              Gate.setUserConfigFile(new File(gateHome, "user-gate.xml")); 
              //GATE is initialized as non visible.
              Gate.init(); 
              // We load the plugins that are going to be used by the SAGA modules.
              // Load ANNIE.
              Gate.getCreoleRegister().registerDirectories( 
                      ctx.getResource("/WEB-INF/plugins/ANNIE")); 
              // Load processingResources (from SAGA)
              Gate.getCreoleRegister().registerDirectories( 
                      ctx.getResource("/WEB-INF/plugins/processingResources"));
              // Load webProcessingResources (from SAGA)
              Gate.getCreoleRegister().registerDirectories( 
                      ctx.getResource("/WEB-INF/plugins/webProcessingResources")); 
              // Now we create the sentiment analysis modules that are going to be provided by the service.
              // English financial module.
            ArrayList<URL> dictionaries = new ArrayList<URL>();
			dictionaries.add((new RestrictedService()).getClass().getResource("/LoughranMcDonald/lists.def"));
            modules.put("enFinancial", new DictionaryBasedSentimentAnalyzer("SAGA - Sentiment Analyzer", dictionaries));
            // English financial + emoticon module.
            ArrayList<URL> dictionaries2 = new ArrayList<URL>();
			dictionaries2.add((new RestrictedService()).getClass().getResource("/LoughranMcDonald/lists.def"));
			dictionaries2.add((new RestrictedService()).getClass().getResource("/resources/gazetteer/emoticon/lists.def")); 
            modules.put("enFinancialEmoticon", new DictionaryBasedSentimentAnalyzer("SAGA - Sentiment Analyzer", dictionaries2));
            // Mongo login
            String user = "";
            String password = "";
            MongoCredential credential = MongoCredential.createMongoCRCredential(user,"RestrictedDictionaries",password.toCharArray());
    		MongoClient mongoClient = new MongoClient(new ServerAddress("localhost"), Arrays.asList(credential));
    		db = mongoClient.getDB("RestrictedDictionaries");
            
            gateInited = true; 
          
        } 
        catch(Exception ex) { 
            throw new ServletException("Exception initialising GATE", ex); 
          } 
        } 
    } 


    /**
	 * When the servlet receives a GET request.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher view = request.getRequestDispatcher(SERVICE_JSP);
	    view.forward(request, response);
	}

	/**
	 * When the servlet receives a POST request.
	 * 
	 * SIMILIAR DOCUMENTATION AS SAGAtoNIF PROJECT
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String forward="";
		String eurosentiment="";
		HttpEntity entity = null;
		//HttpResponse responseMARL = null;
		HttpSession session =request.getSession();
		RequestDispatcher view;
	    // Get a map of the request parameters
	    Map parameters = request.getParameterMap();
	    if (parameters.containsKey("input")){
	    	if(parameters.containsKey("intype") && parameters.containsKey("informat") && parameters.containsKey("outformat")){
	    		if(!request.getParameter("intype").equalsIgnoreCase("direct")){
	    			  forward = RESPONSE_JSP;
	    		      eurosentiment = "intype should be direct";
	    	  		  session.setAttribute("eurosentiment", eurosentiment);
	    	  		  view = request.getRequestDispatcher(forward);
	    	  		  view.forward(request, response);
	    	  		  return;
	    		}
	    		if(!request.getParameter("informat").equalsIgnoreCase("text")){
	    			  forward = RESPONSE_JSP;
	    		      eurosentiment = "informat should be text";
	    	  		  session.setAttribute("eurosentiment", eurosentiment);
	    	  		  view = request.getRequestDispatcher(forward);
	    	  		  view.forward(request, response);
	    	  		  return;
	    		}
	    		if(!request.getParameter("outformat").equalsIgnoreCase("json-ld")){
	    			  forward = RESPONSE_JSP;
	    		      eurosentiment = "outformat should be json-ld";
	    	  		  session.setAttribute("eurosentiment", eurosentiment);
	    	  		  view = request.getRequestDispatcher(forward);
	    	  		  view.forward(request, response);
	    	  		  return;
	    		}
	    	//Check that in not url or the other type
		  forward = RESPONSE_JSP;
		  String textToAnalize = request.getParameter("input");
		  try{
  			if(parameters.containsKey("algo")){
  				if(request.getParameter("algo").equalsIgnoreCase("enFinancial")){
  		  			entity = callSAGA(textToAnalize, "enFinancial");
  				} else if(request.getParameter("algo").equalsIgnoreCase("enFinancialEmoticon")){
  					entity = callSAGA(textToAnalize, "enFinancialEmoticon");
  				} else if(request.getParameter("algo").equalsIgnoreCase("ANEW2010All")){
  					entity = callANEW(textToAnalize, "ANEW2010All");
  				}else if(request.getParameter("algo").equalsIgnoreCase("ANEW2010Men")){
  					entity = callANEW(textToAnalize, "ANEW2010Men");
  				}else if(request.getParameter("algo").equalsIgnoreCase("ANEW2010Women")){
  					entity = callANEW(textToAnalize, "ANEW2010Women");
  				}
  			}

            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                	BufferedReader in = new BufferedReader(new InputStreamReader(instream));
            		String inputLine;
            		StringBuffer marl = new StringBuffer();
             
            		while ((inputLine = in.readLine()) != null) {
            			marl.append(inputLine);
            			marl.append("\n");
            		}
            		in.close();
            		eurosentiment = marl.toString();
            		session.setAttribute("eurosentiment", eurosentiment);
           
                } finally {
                    instream.close();
                }
            }
  			} catch(Exception e){
  				System.err.println(e);
  			}
	    	} else {
	    		forward = RESPONSE_JSP;
	  	      	eurosentiment = "There is no intype, informat or outformat especified";
	    		session.setAttribute("eurosentiment", eurosentiment);
	    	}
	    } else {
	      forward = RESPONSE_JSP;
	      eurosentiment = "There is no input";
  		  session.setAttribute("eurosentiment", eurosentiment);
	    }
	    view = request.getRequestDispatcher(forward);
	    view.forward(request, response);

	}
	
	/**
	 * Call MARL generator
	 * 
	 * @param textToAnalize The analyzed text
	 * @param words A bidimensional array with the words in the text, their position, polarity and value.
	 * @param polarityAndValue Polarity and value of the text.
	 * @return MARL entity.
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static HttpEntity callMarl(String textToAnalize, String[][] words, String[] polarityAndValue) throws UnsupportedEncodingException, IOException, ClientProtocolException{
		//Calling MARL generator
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://demos.gsi.dit.upm.es/eurosentiment/marlgenerator/process");

        // Request parameters and other properties.
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(4);
        params.add(new BasicNameValuePair("intype", "direct"));
        params.add(new BasicNameValuePair("informat", "GALA"));
        params.add(new BasicNameValuePair("outformat", "jsonld"));
        StringBuffer input = new StringBuffer();
        input.append(textToAnalize);
        input.append("	");
        input.append(polarityAndValue[1]);
        input.append("	");
        input.append(polarityAndValue[0]);
        for(int i = 0; i < words.length; i++){
        	if(words[i][4].equals("Neutral") == false){
        		input.append("	");
        		input.append(words[i][0]);
        		input.append("	");
        		input.append(words[i][1]);
        		input.append("	");
        		input.append(words[i][2]);
        		input.append("	");
        		input.append(words[i][4]);
        		input.append("	");
        		input.append(words[i][3]);
        	}
        }
        params.add(new BasicNameValuePair("input", input.toString()));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse responseMARL = httpclient.execute(httppost);
        return responseMARL.getEntity();
	}
	
	/**
	 * Execute a SAGA dictionary module and call MARL generator.
	 * 
	 * @param textToAnalize Text to be analyzed.
	 * @param algorithmName The name of the sentiment analysis module.
	 * @return MARL entity.
	 * @throws Exception
	 */
	public static HttpEntity callSAGA(String textToAnalize, String algorithmName) throws Exception{
		DictionaryBasedSentimentAnalyzer module = modules.get(algorithmName);
		Corpus corpus = Factory.newCorpus("Texto web");
		Document textoWeb = Factory.newDocument(textToAnalize + " ");
		corpus.add(textoWeb);
		module.setCorpus(corpus);
		module.execute();
		return callMarl(textToAnalize, module.getWordsAndValues(), module.getAnalysisResult());
	}
	
	/**
	 * @param textToAnalize
	 * @param collectionName
	 * @return
	 * @throws Exception
	 */
	public static HttpEntity callANEW(String textToAnalize, String collectionName) throws Exception{
		String[] wordsInText = (textToAnalize + " ").split(" ");
		String wordAndValues[][] = new String[wordsInText.length][5]; //initialize the wordAndValues
		int position = 0; //Aux variable to see the position of each word inside the document
		String polarityAndValue[] = new String[2];

		DBCollection coll = db.getCollection(collectionName);
		Double positive = 0.0;
		Double negative = 0.0;
		for(int i = 0; i < wordsInText.length; i++){ //For each word
			wordAndValues[i][0] = wordsInText[i]; //We save the word
			wordAndValues[i][1] = Integer.toString(position); //add its initial 
			position += (wordsInText[i].length() - 1);
			wordAndValues[i][2] = Integer.toString(position);//and final position
			position += 2; //This is where the next word starts
			//We check if this word has a positive or negative annotation and set its value and polarity.
			BasicDBObject query = new BasicDBObject("Word", wordsInText[i].toLowerCase());
			DBCursor cursor = coll.find(query);
			Double value = 5.0;
				try {
				   while(cursor.hasNext()) {
					   value = (Double) cursor.next().get("ValMn");
				   }
				} finally {
				   cursor.close();
				}
			if(value > 5){
				wordAndValues[i][3] = "1.0";
				wordAndValues[i][4] = "Positive";
				positive++;
			} else if (value < 5){
				wordAndValues[i][3] = "-1.0";
				wordAndValues[i][4] = "Negative";
				negative++;
			} else{
				wordAndValues[i][3] = "0.0";
				wordAndValues[i][4] = "Neutral";
			}
			polarityAndValue[0] = Double.toString(0);
			if((positive + negative) != 0){ 
				polarityAndValue[0] = Double.toString((positive - negative)/(positive + negative));
				}
			if(new Double(polarityAndValue[0]) > 0){
				polarityAndValue[1] = "Positive";
			} else if(new Double(polarityAndValue[0]) < 0){
				polarityAndValue[1] = "Negative";
			} else{
				polarityAndValue[1] = "Neutral";
			}
		}
			 
		return callMarl(textToAnalize, wordAndValues, polarityAndValue);
	}

}
