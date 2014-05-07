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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * 
 * Servlet implementation class Controller
 */
@WebServlet("/Controller")
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public Controller() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session =request.getSession();
		session.setAttribute("eurosentiment", "The result will be here. Analyze the example!");
		session.setAttribute("textToAnalyze", "I feel good :)");
		session.setAttribute("alert", "alert alert-info");
		RequestDispatcher view;
		view = request.getRequestDispatcher("/SentimentAnalysis.jsp");
	    view.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String eurosentiment="";
		HttpSession session =request.getSession();
		//Calling SAGAtoNIF generator
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://localhost:8080/SAGAtoNIF/Service"); //Default
        session.setAttribute("alert", "alert alert-info");
        // Request parameters and other properties.
        Map parameters = request.getParameterMap();
        if (parameters.containsKey("input") && parameters.containsKey("intype") && parameters.containsKey("informat") && parameters.containsKey("outformat") && parameters.containsKey("algo")){
        	String algo = request.getParameter("algo");
        	ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(4);
        	params.add(new BasicNameValuePair("input", request.getParameter("input")));
        	params.add(new BasicNameValuePair("intype", request.getParameter("intype")));
        	params.add(new BasicNameValuePair("informat", request.getParameter("informat")));
        	params.add(new BasicNameValuePair("outformat", request.getParameter("outformat")));
        	params.add(new BasicNameValuePair("algo", request.getParameter("algo")));
        	if (algo.equalsIgnoreCase("spFinancial") || algo.equalsIgnoreCase("spFinancialEmoticon") || algo.equalsIgnoreCase("Emoticon")){
        		httppost = new HttpPost("http://localhost:8080/SAGAtoNIF/Service");
        	} else if (algo.equalsIgnoreCase("enFinancial") || algo.equalsIgnoreCase("enFinancialEmoticon") || algo.equalsIgnoreCase("ANEW2010All") || algo.equalsIgnoreCase("ANEW2010Men") || algo.equalsIgnoreCase("ANEW2010Women")){
        		httppost = new HttpPost("http://localhost:8080/RestrictedToNIF/RestrictedService");
        	}
        	
        	httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        	//Execute and get the response.
        	HttpResponse responseService = httpclient.execute(httppost);
        	HttpEntity entity = responseService.getEntity();
        
        	if (entity != null) {
        		InputStream instream = entity.getContent();
        		try {
            	BufferedReader in = new BufferedReader(new InputStreamReader(instream));
        		String inputLine;
        		StringBuffer marl = new StringBuffer();
        		boolean knowPolarity = false; 
        		while ((inputLine = in.readLine()) != null) {
        			marl.append(inputLine);
        			marl.append("\n");
        			if(inputLine.contains("marl:Positive") && !knowPolarity){
        				session.setAttribute("alert", "alert alert-success");
        				knowPolarity = true;
        			}else if(inputLine.contains("marl:Negative") && !knowPolarity){
        				session.setAttribute("alert", "alert alert-danger");
        				knowPolarity = true;
        			}else if(inputLine.contains("marl:Neutral") && !knowPolarity){
        				session.setAttribute("alert", "alert alert-info");
        				knowPolarity = true;
        			}
        			
        		}
        		in.close();
        		eurosentiment = marl.toString();
        		session.setAttribute("eurosentiment", eurosentiment);
       
        		} finally {
                instream.close();
        		}
        	}
        }
        session.setAttribute("textToAnalyze", request.getParameter("input"));
		RequestDispatcher view;
		view = request.getRequestDispatcher("/SentimentAnalysis.jsp");
	    view.forward(request, response);
	}

}
