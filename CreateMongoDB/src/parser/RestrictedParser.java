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

package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

public class RestrictedParser {
	
	/**
	 * To parse ANEW dictionaries
	 * 
	 * @param dbName Name of the database
	 * @param collectionName Name of the new collection
	 * @param dicFile Dictionary file in tsv format
	 */
	public static void parseANEW(String dbName, String collectionName, String dicFile){
		try{
			System.out.println("Se conecta a la base de datos");
			// Credentials to login into your database
			String user = "";
			String password = "";
			MongoCredential credential = MongoCredential.createMongoCRCredential(user,dbName,password.toCharArray());
			MongoClient mongoClient = new MongoClient(new ServerAddress("localhost"), Arrays.asList(credential));
			// Get the DB
			DB db = mongoClient.getDB(dbName);
			// Create the collection
			db.createCollection(collectionName, null);
			// Get the collection
			DBCollection coll = db.getCollection(collectionName);
			// Parse the dictionary
			System.out.println("Comienza el parseo del diccionario");
			FileReader input = new FileReader(dicFile); 
			BufferedReader bufRead = new BufferedReader(input);
			String myLine = null;
			myLine = bufRead.readLine(); // The first line is not needed
			while ((myLine = bufRead.readLine()) != null){  
				String[] word = myLine.split("	");
				BasicDBObject doc = new BasicDBObject("Word", word[0]).
		                append("Wdnum", new Double(word[1])).
		                append("ValMn", new Double(word[2])).
		                append("ValSD", new Double(word[3])).
		                append("AroMn", new Double(word[4])).
		                append("AroSD", new Double(word[5])).
		                append("DomMn", new Double(word[6])).
		                append("DomSD", new Double(word[7]));
				coll.insert(doc);
				System.out.println("Parseando -> " + myLine);
			}
		
			bufRead.close();
		}catch(Exception e){
			System.err.println(e);
		}
	}
	
	/**
	 * Main method to parse the ANEW dictionaries
	 * @param args
	 */
	public static void main(String[] args){
		parseANEW("RestrictedDictionaries","ANEW2010All","/Users/David/Documents/SEAS/CreateMongoDB/bin/ANEW2010/ANEW2010All.txt");
		parseANEW("RestrictedDictionaries","ANEW2010Men","/Users/David/Documents/SEAS/CreateMongoDB/bin/ANEW2010/ANEW2010Men.txt");
		parseANEW("RestrictedDictionaries","ANEW2010Women","/Users/David/Documents/SEAS/CreateMongoDB/bin/ANEW2010/ANEW2010Women.txt");
	}
}
