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
 * This is a module that extends the capabilities of the base module.
 * It adds the TextValueAndPolarityGenerator and WordValueAndPolarityGenerator PRs.
 * 
 * @author David Moreno Briz
 *
 */

package gateModules;

import java.net.URL;
import java.util.ArrayList;

import gate.Gate;
import processingResources.TextValueAndPolarityGenerator;
import webProcessingResources.WordValueAndPolarityGenerator;

public class DictionaryBasedSentimentAnalyzer extends DictionaryBasedInformationExtractor{
	
	/**
	 * The new processing resources.
	 */
	
	TextValueAndPolarityGenerator textAnalizer;
	WordValueAndPolarityGenerator wordAnalizer;
	
	/**
	 * Constructor of the module called DictionaryBasedSentimentAnalyzer based on DictionaryBasedInformationExtractor.
	 * It adds the TextValueAndPolarityGenerator and WordValueAndPolarityGenerator PRs.
	 * 
	 * @param name name of the module
	 * @param listsURL location of the lists to set the gazetteer. In URL format.
	 * @throws Exception
	 */
	public DictionaryBasedSentimentAnalyzer(String name, ArrayList<URL> listsURL) throws Exception {
		super(name,listsURL);
		this.textAnalizer = getCountTokens();
		this.add(this.textAnalizer);
		this.wordAnalizer = getWordsSentimets();
		this.add(this.wordAnalizer);
		
	}
	
	/**
	 * Used only in the local mode.
	 * 
	 * Register our own plugins located in bin/processingResources/
	 * so we can use it in our controller.
	 *  
	 * @throws Exception
	 */
	public void registerPrPlugin() throws Exception{
		Gate.getCreoleRegister().registerDirectories(this.getClass().getResource("/processingResources/"));
		Gate.getCreoleRegister().registerDirectories(this.getClass().getResource("/webProcessingResources/"));
	}

	/**
	 * Get the configured TextValueAndPolarityGenerator PR, 
	 * which counts the number of times a sentiment word is said
	 * in a document according with our dictionaries and generates the document value.
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public static TextValueAndPolarityGenerator getCountTokens() throws Exception{
		TextValueAndPolarityGenerator count = new TextValueAndPolarityGenerator(); //Create the PR
		count.setName("Sentiment analyzer"); //Set its name
		return count;
	}
	
	/**
	 * Used for the web service, where a GET petition only have one document to analyze.
	 * 
	 * @return an array with the value and polarity of an analyzed document.
	 */
	public String[] getAnalysisResult(){
		return this.textAnalizer.getAnalysisResult();
	}
	
	/**
	 * Get the configured Count Sentiment For Each WOrd PR, 
	 * which analyze each word in a given document.
	 *
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public static WordValueAndPolarityGenerator getWordsSentimets() throws Exception{
		WordValueAndPolarityGenerator count = new WordValueAndPolarityGenerator(); //Create the PR
		count.setName("Sentiment analyzer"); //Set its name
		return count;
	}
	
	/**
	 * Used for the web service, where a GET petition only have one document to analyze.
	 * 
	 * @return an array with each word in a given document and its associated values. See the PR for more infromation.
	 */
	public String[][] getWordsAndValues(){
		return this.wordAnalizer.getWordsAndValues();
	}
}