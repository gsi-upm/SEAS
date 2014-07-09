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
 * This is the base module. It sets the basics ANNIE's PR.
 * 
 * @author David Moreno Briz
 *
 */

package gateModules; //Package for the different modules

import java.net.URL;
import java.util.ArrayList;

import gate.*;
import gate.creole.SerialAnalyserController;
import gate.creole.tokeniser.DefaultTokeniser;
import gate.util.ExtensionFileFilter;
import gate.creole.annotdelete.AnnotationDeletePR;
import gate.creole.gazetteer.DefaultGazetteer;
import gate.creole.ANNIETransducer;

public class DictionaryBasedInformationExtractor{
	
	/**
	 * This is the analyser controler where we will add all the Processing Resources for this module. 
	 */
	private final SerialAnalyserController controller = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController");
	
	/**
	 * Constructor of the base module called DictionaryBasedInformationExtractor.
	 * It adds the basic ANNIE PR we will use as a start point.
	 * 
	 * @param name name of the module
	 * @param listsURL location of the lists to set the gazetteer. In URL format.
	 * @throws Exception
	 */
	public DictionaryBasedInformationExtractor(String name, ArrayList<URL> listsURL) throws Exception{
		this.controller.setName(name); // Set the module name
		//Delete PR.
		AnnotationDeletePR delete = this.getDeletePR(); 
		this.add(delete);
		//Annie Tokeniser. 
		DefaultTokeniser tokeniser = this.getTokeniserPR();
		this.add(tokeniser);
		//Annie Gazetter.
		for(URL list : listsURL){
		DefaultGazetteer gazetteer = this.getGazetteerPR(list);
		this.add(gazetteer);
		}
		//Annie NE Transducer.
		ANNIETransducer transducer = this.getTransducerPR();
		this.add(transducer);
	}
	
	/**
	 * Add any Processing Resource designed 
	 * or configured by the user to the controller.
	 * 
	 * @param processingResources
	 * @throws Exception
	 */
	public void add(ProcessingResource pr) throws Exception{
		this.controller.add(pr);
	}
	
	/**
	 * Execute all the PRs in the controller.
	 * 
	 * @throws Exception
	 */
	public void execute() throws Exception{
		this.controller.execute();
	}
	
	/**
	 * Set the corpus of documents over which the controller will work. 
	 * @param corpus of xml documents to analyse.
	 * @throws Exception
	 */
	public void setCorpus(Corpus corpus) throws Exception{
		this.controller.setCorpus(corpus);
	}
	
	
	/**
	 * Create a corpus and populate it with the XML documents 
	 * in the directory resources/data/input.
	 * 
	 * @return the populated corpus
	 * @throws Exception
	 */
	public Corpus createCorpusAndPupulateItExample() throws Exception{
		Corpus corpus = Factory.newCorpus("Tweets"); //Create a corpus name Tweets
		ExtensionFileFilter filter = new ExtensionFileFilter("XML files", "xml"); //A filter to add XML documents
		corpus.populate(this.getClass().getResource("/resources/data/input"), filter,"UTF-8", true); //Populate it from /resource/data/input directory
		return corpus;
	}
	
	/**
	 * Get the configurated ANNIE Annotation Delete PR.
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public AnnotationDeletePR getDeletePR() throws Exception{
		AnnotationDeletePR delete = new AnnotationDeletePR(); //Create the PR
		delete.setName("Delete PR"); //Set its name
		delete.setKeepOriginalMarkupsAS(new Boolean(true)); //Keep the original XML markups of the documents in the corpus 
		ArrayList<String> list = new ArrayList<String>(); //List of sets to keep
		list.add("Key"); //Keeps Key set.
		delete.setSetsToKeep(list);
		delete.init(); // The PR is initialized.
		return delete;
	}
	
	/**
	 * Get the configurated ANNIE Tokeniser PR.
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public DefaultTokeniser getTokeniserPR() throws Exception{
		DefaultTokeniser tokeniser = new DefaultTokeniser(); //Create the PR
		tokeniser.setName("Tokenizator"); //Set its name
		tokeniser.setEncoding("UTF-8"); //Set encoding
		//Set tonekiser rules from the file in /resources/tokeniser/DefaultTokeniser.rules
		tokeniser.setTokeniserRulesURL(this.getClass().getResource("/resources/tokeniser/DefaultTokeniser.rules"));
		//And the grammar from /resources/tokeniser/postprocess.jape
		tokeniser.setTransducerGrammarURL(this.getClass().getResource("/resources/tokeniser/postprocess.jape"));
		tokeniser.setAnnotationSetName("Annotations"); //Set annotation set name for the token annotation.
		tokeniser.init(); //The PR is initialized
		return tokeniser;
	}
	
	/**
	 * Get the configurated ANNIE Gazetteer PR.
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public DefaultGazetteer getGazetteerPR(URL listsURL) throws Exception{
		DefaultGazetteer gazetteer = new DefaultGazetteer(); //Create the PR
		gazetteer.setName("Gazetter"); //Set its name
		gazetteer.setCaseSensitive(new Boolean(false)); 
		gazetteer.setEncoding("UTF-8");
		//Set the list of the dictionaries that are going to be used by the Gazetteer
		gazetteer.setListsURL(listsURL);
		//Set annotation set name for the gazetteer features.
		gazetteer.setAnnotationSetName("Annotations");
		gazetteer.setLongestMatchOnly(new Boolean(true));
		gazetteer.setWholeWordsOnly(new Boolean(true));
		gazetteer.init(); //The PR is initialized
		return gazetteer;
	}
	
	/**
	 * Get the configurated ANNIE Transducer PR.
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public ANNIETransducer getTransducerPR() throws Exception{
		ANNIETransducer transducer = new ANNIETransducer(); //Create the PR
		transducer.setName("NE Transducer"); //Set its name
		transducer.setEncoding("UTF-8");
		//Set the grammar to transduce the features into annotations.
		transducer.setGrammarURL(this.getClass().getResource("/resources/jape/main.jape"));
		transducer.setInputASName("Annotations"); //Input set of annotations to run the transducer
		transducer.setOutputASName("Sentiment"); //Output annotation
		transducer.init(); //The PR is initialized
		return transducer;
	}

}
