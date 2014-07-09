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
 * An example that execute the module called MachineLearningBasedAnalyzer over an example corpus in local/graphic mode.
 *
 * @author David Moreno Briz
 *
 */

package examples;

import java.io.File;
import java.util.ArrayList;

import gate.Corpus;
import gate.Gate;
import gate.gui.MainFrame;
import gate.learning.RunMode;
import gateModules.MachineLearningBasedAnalyzer;

public class MachineLearningBasedReviewAnalyzer{
	
	/**
	 * Execute "MachineLearningBasedAnalyzer" module in GATE graphic/local mode.
	 * 
	 * @param args not used
	 * @throws Exception
	 */
	
	public static void main(String[] args) throws Exception{
		Gate.init(); // Prepare the library
		MainFrame.getInstance().setVisible(true); //Set GATE app visible
		
		// Get the root plugins dir
		File pluginsDir = Gate.getPluginsHome();
		
		// Load the Annie, Tools and Laerning plugins
		File aPluginDir = new File(pluginsDir, "ANNIE");
		File aPluginDir2 = new File(pluginsDir, "Tools");
		File aPluginDir3 = new File(pluginsDir, "Learning");
		
		// Register the plugins
		Gate.getCreoleRegister().registerDirectories(aPluginDir.toURI().toURL());
		Gate.getCreoleRegister().registerDirectories(aPluginDir2.toURI().toURL());
		Gate.getCreoleRegister().registerDirectories(aPluginDir3.toURI().toURL());
		
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("comment");
		
		//Training module
		MachineLearningBasedAnalyzer trainingModule = new MachineLearningBasedAnalyzer("Training","/resources/machineLearning/reviews/paum.xml", RunMode.TRAINING, "", list);
		//Create the corpus and populate it
		Corpus corpus = trainingModule.createCorpusAndPupulateIt("Training", "/resources/machineLearning/reviews/corpora/training");
		trainingModule.setCorpus(corpus); // Set corpus into the controller. 
		trainingModule.execute();
		
		//Application module
		MachineLearningBasedAnalyzer applicationModule = new MachineLearningBasedAnalyzer("Testing","/resources/machineLearning/reviews/paum.xml", RunMode.APPLICATION, "output", list);
		//Create the corpus and populate it.
		Corpus corpus2 = applicationModule.createCorpusAndPupulateIt("Testing", "/resources/machineLearning/reviews/corpora/testing");
		applicationModule.setCorpus(corpus2); // Set corpus into the controller. 
		applicationModule.execute();
		
		//Evaluation module
		MachineLearningBasedAnalyzer evaluationModule = new MachineLearningBasedAnalyzer("Cross-validation","/resources/machineLearning/reviews/paum.xml", RunMode.EVALUATION, "", list);
		//Create the corpus and populate it.
		Corpus corpus3 = evaluationModule.createCorpusAndPupulateIt("All", "/resources/machineLearning/reviews/corpora/all");
		evaluationModule.setCorpus(corpus3); // Set corpus into the controller. 
		evaluationModule.execute();
				
	}
}
