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
package gateModules; //Package for the different modules

import java.util.ArrayList;

import gate.*;
import gate.creole.ANNIETransducer;
import gate.creole.POSTagger;
import gate.creole.SerialAnalyserController;
import gate.creole.morph.Morph;
import gate.creole.splitter.SentenceSplitter;
import gate.creole.tokeniser.DefaultTokeniser;
import gate.util.ExtensionFileFilter;
import gate.creole.annotdelete.AnnotationDeletePR;
import gate.creole.annotransfer.AnnotationSetTransfer;
import gate.learning.EvaluationBasedOnDocs;
import gate.learning.LearningAPIMain;
import gate.learning.RunMode;

public class MachineLearningBasedAnalyzer{
	
	/**
	 * This is the analyser controler where we will add all the Processing Resources for this module. 
	 */
	private final SerialAnalyserController controller = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController");
	
	private LearningAPIMain batchLearningPR;
	
	private RunMode mlMode;

	/**
	 * Constructor of the base module called MachineLearningBasedAnalyzer.
	 * 
	 * 
	 * @param name name of the module
	 * @param mlConfiguration Location of paum.xml
	 * @param mlMode Learning mode
	 * @param mlOutput output annotation set
	 * @param list list of annotations to transfer
	 * @param listsURL location of the lists to set the gazetteer. In URL format.
	 * @throws Exception
	 */
	public MachineLearningBasedAnalyzer(String name, String mlConfiguration, RunMode mlMode, String mlOutput, ArrayList<String> list) throws Exception{
		this.controller.setName(name); // Set the module name
		this.mlMode = mlMode;
		//Delete PR.
		AnnotationDeletePR delete = this.getDeletePR(); 
		//Annie Tokeniser. 
		DefaultTokeniser tokeniser = this.getTokeniserPR();
		//Adding the different PR.
		this.add(delete);
		if(mlMode == RunMode.TRAINING  || this.mlMode == RunMode.EVALUATION){
		this.add(this.getAnnotationSetTransferPR(list));
		}
		if(this.mlMode == RunMode.APPLICATION){
		this.add(this.getTransducerPR());	
		}
		this.add(tokeniser);
		this.add(this.getSentenceSplitterPR());
		this.add(this.getPOSTaggerPR());
		this.add(this.getMorphologicalAnalyserPR());
		this.batchLearningPR = this.getMachineLearningPR(mlConfiguration, mlMode, mlOutput);
		this.add(batchLearningPR);
	}
	
	public LearningAPIMain getBatchLearningPR() {
		return batchLearningPR;
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
		if(this.mlMode == RunMode.EVALUATION){
			EvaluationBasedOnDocs crossValidation = this.batchLearningPR.getEvaluation(); 
			System.out.println( 
					crossValidation.macroMeasuresOfResults.precision + "," + 
					crossValidation.macroMeasuresOfResults.recall + "," + 
					crossValidation.macroMeasuresOfResults.f1 + "," + 
					crossValidation.macroMeasuresOfResults.precisionLenient + "," + 
					crossValidation.macroMeasuresOfResults.recallLenient + "," + 
					crossValidation.macroMeasuresOfResults.f1Lenient + "\n");
		}
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
	 * in the directory /resources/machineLearning/corpora/training.
	 * 
	 * @param name Name of the corpus
	 * @param corpusDir location of the corpus
	 * 
	 * @return the populated corpus
	 * @throws Exception
	 */
	public Corpus createCorpusAndPupulateIt(String name, String corpusDir) throws Exception{
		Corpus corpus = Factory.newCorpus(name); //Create a corpus name Tweets
		ExtensionFileFilter filter = new ExtensionFileFilter("XML files", "xml"); //A filter to add XML documents
		corpus.populate(this.getClass().getResource(corpusDir), filter,"UTF-8", true); //Populate it from /resource/data/input directory
		return corpus;
	}
	
	/**
	 * Get the configured ANNIE Annotation Delete PR.
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public AnnotationDeletePR getDeletePR() throws Exception{
		AnnotationDeletePR delete = new AnnotationDeletePR(); //Create the PR
		delete.setName("Document Reset PR"); //Set its name
		delete.setKeepOriginalMarkupsAS(new Boolean(true)); //Keep the original XML markups of the documents in the corpus 
		ArrayList<String> list = new ArrayList<String>(); //List of sets to keep
		list.add("Key"); //Keeps Key set.
		delete.setSetsToKeep(list);
		delete.init(); // The PR is initialized.
		return delete;
	}
	
	/**
	 * Get the configured Annotation Set Transfer PR.
	 * 
	 * @param list List of annotations to transfer from one annotation set to another.
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public AnnotationSetTransfer getAnnotationSetTransferPR(ArrayList<String> list) throws Exception{
		AnnotationSetTransfer pr = new AnnotationSetTransfer(); //Create the PR
		pr.setName("Annotation Set Transfer PR"); //Set its name
		//Set tonekiser rules from the file in /resources/tokeniser/DefaultTokeniser.rules
		pr.setCopyAnnotations(true);
		pr.setTransferAllUnlessFound(true);
		pr.setAnnotationTypes(list);
		pr.setInputASName("Key");
		pr.setOutputASName("");
		pr.setTagASName("");
		pr.setTextTagName("");
		pr.init();
		return pr;
	}
	
	public ANNIETransducer getTransducerPR() throws Exception{
		ANNIETransducer transducer = new ANNIETransducer(); //Create the PR
		transducer.setName("NE Transducer"); //Set its name
		transducer.setEncoding("UTF-8");
		//Set the grammar to transduce the features into annotations.
		transducer.setGrammarURL(this.getClass().getResource("/resources/machineLearning/reviews/copy_comment_spans.jape"));
		transducer.setInputASName("Key"); //Input set of annotations to run the transducer
		transducer.setOutputASName(""); //Output annotation
		transducer.init(); //The PR is initialized
		return transducer;
	} 
	
	/**
	 * Get the configured ANNIE Tokeniser PR.
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
		//tokeniser.setAnnotationSetName(""); //Set annotation set name for the token annotation.
		tokeniser.init(); //The PR is initialized
		return tokeniser;
	}
	
	
	/**
	 * Get the configured ANNIE Sentence Splitter PR.
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public SentenceSplitter getSentenceSplitterPR() throws Exception{
		SentenceSplitter pr = new SentenceSplitter(); //Create the PR
		pr.setName("Sentence Splitter PR"); //Set its name 
		pr.setEncoding("UTF-8");
		pr.setTransducerURL(this.getClass().getResource("/resources/sentenceSplitter/grammar/main.jape"));
		pr.setGazetteerListsURL(this.getClass().getResource("/resources/sentenceSplitter/gazetteer/lists.def"));
		pr.init(); //The PR is initialized
		return pr;
	}
	
	/**
	 * Get the configured ANNIE POS Tagger PR.
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public POSTagger getPOSTaggerPR() throws Exception{
		POSTagger pr = new POSTagger(); //Create the PR
		pr.setName("POS Tagger PR"); //Set its name
		pr.setEncoding("UTF-8");
		pr.setLexiconURL(this.getClass().getResource("/resources/sentenceSplitter/gazetteer/lists.def"));
		pr.setRulesURL(this.getClass().getResource("/resources/heptag/ruleset"));
		pr.setBaseSentenceAnnotationType("Sentence");
		pr.setBaseTokenAnnotationType("Token");
		pr.setFailOnMissingInputAnnotations(true);
		pr.setInputASName("");
		pr.setOutputASName("");
		pr.setOutputAnnotationType("Token");
		pr.setPosTagAllTokens(true);
		pr.init(); //The PR is initialized
		return pr;
	}
	
	/**
	 * Get the configured GATE MorphologicalAnalyser PR.
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
	public Morph getMorphologicalAnalyserPR() throws Exception{
		Morph pr = new Morph(); //Create the PR
		pr.setName("MorphologicalAnalyser PR"); //Set its name
		pr.setRulesFile(this.getClass().getResource("/resources/morph/default.rul"));
		pr.setCaseSensitive(false);
		pr.setAffixFeatureName("affix");
		pr.setAnnotationSetName("");
		pr.setConsiderPOSTag(true);
		pr.setFailOnMissingInputAnnotations(true);
		pr.setRootFeatureName("root");
		pr.init(); //The PR is initialized
		return pr;
	}
	
	/**
	 * Get the configured GATE Batch PR.
	 * 
	 * @param congiguration Location of the paum.xml
	 * @param mode Learning mode
	 * @param output Name of the output annotation set
	 * 
	 * @return the initialized PR.
	 * @throws Exception
	 */
  public LearningAPIMain getMachineLearningPR(String configuration, RunMode mode, String output) throws Exception{
	  LearningAPIMain pr = new LearningAPIMain();
	  pr.setConfigFileURL(this.getClass().getResource(configuration));
	  pr.setName("Batch Learning PR");
	  pr.setInputASName("");
	  pr.setOutputASName(output);
	  pr.setLearningMode(mode);
	  pr.init();
	  return pr;
	}

}
