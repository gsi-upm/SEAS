/*******************************************************************************
 * Copyright (c) 2014 - David Moreno Briz - Grupo de Sistemas Inteligentes - Universidad Politécnica de Madrid. (GSI-UPM)
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
/**
 * Look at execute() to see what this PR does.
 * 
 * @author David Moreno Briz
 *
 */


package processingResources; //Package for the Processing Resources made by us.

import gate.Resource;
import gate.corpora.DocumentContentImpl;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;

public class TextValueAndPolarityGenerator extends AbstractLanguageAnalyser {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Only used in the Web Service, 
	 * because every GET petition will contain only one document (text) to analyze.
	 * 
	 * If at some point GET petitions include a set of documents,
	 * this variable will be of the type String[numberOfDocuments][2].
	 */
	private String[] analysisResult;
	

/**
 * In local mode: 
 * it adds to a given document its numeric sentiment value and polarity.
 * 
 * In web service mode:
 * it saves in analysisResult the numeric sentiment value and the polarity of a given document.
 */
@Override
public void execute() throws ExecutionException {
	//Count how many positive annotations are in the Sentiment set of annotations in each document in the corpus
	double positive = document.getAnnotations("Sentiment").get("positive").size(); 
	//Count how many negative annotations are in the Sentiment set of annotations in each document in the corpus
	double negative = document.getAnnotations("Sentiment").get("negative").size();
	//Calculate the sentiment value (Goes from -1 to 1)
	double sentiment = 0; 
	if((positive + negative) != 0){ 
	sentiment = (positive - negative)/(positive + negative);
	}
	//Add results to the array
	analysisResult = new String[2];
	analysisResult[0] = Double.toString(sentiment);
	//Calculates polarity of the document
	if(sentiment > 0){
		analysisResult[1] = "Positive";
	} else if(sentiment < 0){
		analysisResult[1] = "Negative";
	} else{
		analysisResult[1] = "Neutral";
	}
	//Sets the sentiment value and polarity at the end of the document
	document.setContent(new DocumentContentImpl(document.getContent().toString() + "\n\n This text has a " + sentiment + " value and " + analysisResult[1] + " polarity."));
}

/**
 * Initialize the Count Sentiment Language Analyser. 
 */
@Override
public Resource init() throws ResourceInstantiationException {
	System.out.println(getClass().getName() + " is added to the controller.");
	return this;
	}

/**
 * @return analysisResult
 */
public String[] getAnalysisResult(){
	return analysisResult;
}
}


