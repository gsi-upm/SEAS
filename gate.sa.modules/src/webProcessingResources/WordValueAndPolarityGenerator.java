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


package webProcessingResources; //Package for the Processing Resources made by us.

import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;


public class WordValueAndPolarityGenerator extends AbstractLanguageAnalyser {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Only used in the Web Service, 
	 * because every GET petition will contain only one document (text) to analyze.
	 * 
	 * If at some point GET petitions include a set of documents,
	 * this variable will be of the type String[numberOfDocuments][][].
	 */
	private String[][] wordAndValues;

	/**
	 * In local mode: 
	 * this PR is executed as invisible.
	 * 
	 * In web service mode:
	 * it saves in wordAndValues the numeric sentiment value, polarity and position inside the document of each word.
	 */
@Override
public void execute() throws ExecutionException {
	String text = document.getContent().toString(); //Take the content of the document
	String wordsInText[] = text.split(" "); //and slit it into its words.
	wordAndValues = new String[wordsInText.length][5]; //initialize the wordAndValues
	int position = 0; //Aux variable to see the position of each word inside the document
	for(int i = 0; i < wordsInText.length; i++){ //For each word
		wordAndValues[i][0] = wordsInText[i]; //We save the word
		wordAndValues[i][1] = Integer.toString(position); //add its initial 
		position += (wordsInText[i].length() - 1);
		wordAndValues[i][2] = Integer.toString(position);//and final position
		position += 2; //This is where the next word starts
		//We check if this word has a positive or negative annotation and set its value and polarity.
		int positive = document.getAnnotations("Sentiment").get(new Long(wordAndValues[i][1]), new Long(wordAndValues[i][2])).get("positive").size();
		int negative = document.getAnnotations("Sentiment").get(new Long(wordAndValues[i][2]), new Long(wordAndValues[i][2])).get("negative").size();
		if(positive > 0){
			wordAndValues[i][3] = "1.0";
			wordAndValues[i][4] = "Positive";
		} else if (negative > 0){
			wordAndValues[i][3] = "-1.0";
			wordAndValues[i][4] = "Negative";
		} else{
			wordAndValues[i][3] = "0.0";
			wordAndValues[i][4] = "Neutral";
		}
	}
}

/**
 * Initialize the Count Sentiment Of Each Word Language Analyser. 
 */
@Override
public Resource init() throws ResourceInstantiationException {
	System.out.println(getClass().getName() + " is added to the controller.");
	return this;
	}

public String[][] getWordsAndValues(){
	return wordAndValues;
}
}


