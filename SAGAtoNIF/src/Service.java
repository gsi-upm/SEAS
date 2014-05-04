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


/**
 * Servlet implementation class Service
 */
@WebServlet("/Service")
public class Service extends HttpServlet {
	
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
     * Default constructor. 
     */
    public Service() {
        super();
    }
    
    /**
     * Method that initializes the server variables. 
     */
    public void init() throws ServletException { 
        if(!gateInited) { //If GATE has been not initialized.
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
            // Spanish financial module.
            ArrayList<URL> dictionaries = new ArrayList<URL>();
			dictionaries.add((new Service()).getClass().getResource("/resources/gazetteer/finances/spanish/paradigma/lists.def"));
            modules.put("spFinancial", new DictionaryBasedSentimentAnalyzer("SAGA - Sentiment Analyzer", dictionaries));
            // Emoticon module.
            ArrayList<URL> dictionaries2 = new ArrayList<URL>();
            dictionaries2.add((new Service()).getClass().getResource("/resources/gazetteer/emoticon/lists.def"));           
            modules.put("emoticon", new DictionaryBasedSentimentAnalyzer("SAGA - Sentiment Analyzer", dictionaries2));
            // Spanish financial + emoticon module.
            ArrayList<URL> dictionaries3 = new ArrayList<URL>();
			dictionaries3.add((new Service()).getClass().getResource("/resources/gazetteer/finances/spanish/paradigma/lists.def"));
            dictionaries3.add((new Service()).getClass().getResource("/resources/gazetteer/emoticon/lists.def"));           
            modules.put("spFinancialEmoticon", new DictionaryBasedSentimentAnalyzer("SAGA - Sentiment Analyzer", dictionaries3));
            // GATE has been initialized.
            gateInited = true; 
          }catch(Exception ex) { 
            throw new ServletException("Exception initialising GATE", ex); 
          } 
        } 
    } 


	/**
	 * When the servlet receives a GET request.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//It always returns the service.jps page.
		RequestDispatcher view = request.getRequestDispatcher(SERVICE_JSP);
	    view.forward(request, response);
	}

	/**
	 * When the servlet receives a POST request.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String forward=""; // Which jsp page is going to be return.
		String eurosentiment=""; // This is the analysis response.
		// Auxiliar variables.
		HttpEntity entity = null;
		HttpSession session =request.getSession();
		RequestDispatcher view;
	    // Get a map of the request parameters
	    Map parameters = request.getParameterMap();
	    if (parameters.containsKey("input")){ // If the request contains a parameter named input
	    	if(parameters.containsKey("intype") && parameters.containsKey("informat") && parameters.containsKey("outformat")){ // If the request contains a parameter named intype, informat and outformat.
	    		if(!request.getParameter("intype").equalsIgnoreCase("direct")){ // If intype is not direct.
	    			  // The response contains only the following message.
	    			  forward = RESPONSE_JSP;
	    		      eurosentiment = "intype should be direct";
	    	  		  session.setAttribute("eurosentiment", eurosentiment);
	    	  		  view = request.getRequestDispatcher(forward);
	    	  		  view.forward(request, response);
	    	  		  return;
	    		}
	    		if(!request.getParameter("informat").equalsIgnoreCase("text")){ // If informat is not text
	    			  // The response contains only the following message.
	    			  forward = RESPONSE_JSP;
	    		      eurosentiment = "informat should be text";
	    	  		  session.setAttribute("eurosentiment", eurosentiment);
	    	  		  view = request.getRequestDispatcher(forward);
	    	  		  view.forward(request, response);
	    	  		  return;
	    		}
	    		if(!request.getParameter("outformat").equalsIgnoreCase("json-ld")){ // If outformat is not json-ld
	    			  // The response contains only the following message.
	    			  forward = RESPONSE_JSP;
	    		      eurosentiment = "outformat should be json-ld";
	    	  		  session.setAttribute("eurosentiment", eurosentiment);
	    	  		  view = request.getRequestDispatcher(forward);
	    	  		  view.forward(request, response);
	    	  		  return;
	    		}
	      // If there is input, intype = direct, informat = text and outformat = json-ld,
		  forward = RESPONSE_JSP; // response.jsp
		  String textToAnalize = request.getParameter("input"); // Text to be analyzed.
		  try{
  			if(parameters.containsKey("algo")){ // If the request contains a parameter named algo (algorithm)
  				if(request.getParameter("algo").equalsIgnoreCase("spFinancial")){ // If algo = spFinancial
  					entity = callSAGA(textToAnalize, "spFinancial"); // The corresponding GATE module is called and a MARL entity is generated.
  				} else if(request.getParameter("algo").equalsIgnoreCase("emoticon")){ // If algo = Emoticon
  					entity = callSAGA(textToAnalize, "emoticon"); // The corresponding GATE module is called and a MARL entity is generated.
  				} else if(request.getParameter("algo").equalsIgnoreCase("spFinancialEmoticon")){ // If algo = spFinancialEmoticon
  					entity = callSAGA(textToAnalize, "spFinancialEmoticon"); // The corresponding GATE module is called and a MARL entity is generated.
  				} else{ // If the request contains a non-valid algorithm.
  					forward = RESPONSE_JSP;
	    		    eurosentiment = "Introduce a valid algorithm";
	    	  		session.setAttribute("eurosentiment", eurosentiment);
	    	  		view = request.getRequestDispatcher(forward);
	    	  		view.forward(request, response);
	    	  		return;
  				}
  			}
  			// If a GATE module has been called and a MARL entity has been generated.
            if (entity != null) {
            	// The MARL entity is processed to be added to the response.jsp
                InputStream instream = entity.getContent();
                try {
                	// The entity is parsed into a StringBuffer.
                	BufferedReader in = new BufferedReader(new InputStreamReader(instream));
            		String inputLine;
            		StringBuffer marl = new StringBuffer();
            		while ((inputLine = in.readLine()) != null) {
            			marl.append(inputLine);
            			marl.append("\n");
            		}
            		in.close();
            		// The variable eurosentiment (String) is setted with the MARL response.
            		eurosentiment = marl.toString();
            		session.setAttribute("eurosentiment", eurosentiment);
                } finally {
                    instream.close();
                }
            }
  			} catch(Exception e){
  				System.err.println(e);
  			}
	    	} else { // If there is no intype, informat or outformat specified.
	    		forward = RESPONSE_JSP;
	  	      	eurosentiment = "There is no intype, informat or outformat specified";
	    		session.setAttribute("eurosentiment", eurosentiment);
	    	}
	    } else { // If there is no input.
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
        // The input parameter.
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
		DictionaryBasedSentimentAnalyzer module = modules.get(algorithmName); // The module is selected.
		Corpus corpus = Factory.newCorpus("Texto web"); // A new corpus is created.
		Document textoWeb = Factory.newDocument(textToAnalize + " "); // A new document is created from the text.
		corpus.add(textoWeb); // The document is added to the corpus.
		module.setCorpus(corpus); // The corpus is setted into the module.
		module.execute(); // The module is executed.
		// MARL is called.
		return callMarl(textToAnalize, module.getWordsAndValues(), module.getAnalysisResult());
	}

}
