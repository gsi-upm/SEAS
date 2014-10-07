![SEAS](http://www.gsi.dit.upm.es/downloads/Logos/seas.png)
=====
![GSI Logo](http://gsi.dit.upm.es/templates/jgsi/images/logo.png)

## Introduction
SEAS is a set of Sentiment and Emotion Analysis Services according to NIF. The NLP Interchange Format (NIF) is an RDF/OWL-based format that aims to achieve interoperability between Natural Language Processing (NLP) tools, language resources and annotations. NIF consists of specifications, ontologies and software, which are combined under the version identifier "2.0", but are versioned individually. 

All of the services have been developed using Apache Tomcat 7, some of them are based in [SAGA](https://github.com/gsi-upm/SAGA) and others are based on open source projects.

## Requisites

These projects have been developed using the following:

1. Java 7 - [Download](https://www.java.com/en/download/)
2. Eclipse JEE - [Download](https://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/keplersr2)
3. Apache Tomcat 7 - [Download](http://tomcat.apache.org/download-70.cgi) - [Setup](http://tomcat.apache.org/tomcat-7.0-doc/setup.html)
4. MongoBD - [Download](http://www.mongodb.org/downloads) - [Setup](http://docs.mongodb.org/manual/) - [JAR](http://docs.mongodb.org/ecosystem/tutorial/getting-started-with-java-driver/)

### Setup Tomcat in Eclipse

In Eclipse go to Preferences -> Server -> RunTime Enviroments -> Add -> Apache -> Apache Tomcat 7.0 -> Select path to Tomcat installation -> Finish

### Set up CretateMongoBD project

1. Import the project into Eclipse.
2. Download [mongo.jar](http://docs.mongodb.org/ecosystem/tutorial/getting-started-with-java-driver/) and add it to the project's library.
3. Go to [http://csea.phhp.ufl.edu/media/anewmessage.html](http://csea.phhp.ufl.edu/media/anewmessage.html) and make a request for Affective Norms for English Words so they provide you with the dictionaries, which are only abaliable for accademic purposes.
4. Configure the project with your MongoBD and with the path to the dictionaries.
5. Execute the project As a Java Application to parse ANEW into your MongoBD.

### Set up RestrictedToNIF project

1. Import the project into Eclipse.
2. Add Tomcat Libraries into the project.
3. Configure your MongoDB installation in the code.
4. Go to [http://www3.nd.edu/~mcdonald/Word_Lists.html](http://www3.nd.edu/~mcdonald/Word_Lists.html) to get these financial dictionaries, which are not abaliable for commercial use without authorization.
5. Put these dictionaries in [GATE](https://gate.ac.uk) format. (See the [dictionary format](https://github.com/gsi-upm/SEAS/tree/master/SAGAtoNIF/src/resources/gazetteer/finances/spanish/paradigma) used in SAGAtoNIF as an example of how to do it)
6. Run As Server.

### Set up SAGAToNIF project

1. Import the project into Eclipse.
2. Add Tomcat Libraries into the project.
3. Run As Server.

### Set up SEAS project

1. Import the project into Eclipse.
2. Add Tomcat Libraries into the project.
3. Run As Server.

Now you can use the service via SEAS web service or console.

## How to use the SAGAtoNIF's or RestrictedToNIF's API

You can go [here](http://demos.gsi.dit.upm.es/tomcat/SEAS/Controller) to see a demo and test the service.

To access the API just send a POST request to http://localhost:8080/SAGAtoNIF/Service or to http://localhost:8080/RestrictedToNIF/RestrictedService OR http://demos.gsi.dit.upm.es/tomcat/SAGAtoNIF/Service or to http://demos.gsi.dit.upm.es/tomcat/RestrictedToNIF/RestrictedService with these parameters:

    input:
        The original text to be analyzed
    informat:
        text
    intype:
        direct
    outformat:
        json-ld
    algo:
        spFinancial (SAGAtoNIF service)
        emoticon (SAGAtoNIF service)
        spFinancialEmoticon (SAGAtoNIF service)
        enFinancial (RestrictedToNIF service)
        enFinancialEmoticon (RestrictedToNIF service)
        
On GNU/Linux, you can test the API using curl. A request would look like this:
```
curl --data "input=The text you want to analyze&intype=direct&informat=text&outformat=json-ld&algo=spFinancialEmoticon" http://localhost:8080/SAGAtoNIF/Service

or

curl --data "input=The text you want to analyze&intype=direct&informat=text&outformat=json-ld&algo=spFinancialEmoticon" http://demos.gsi.dit.upm.es/tomcat/SAGAtoNIF/Service
```
    
On Java, you can test the API using HttpClient. A request would look like this:
```
HttpClient httpclient = HttpClients.createDefault();
HttpPost httppost = new HttpPost("http://demos.gsi.dit.upm.es/tomcat/SAGAtoNIF/Service");
    
ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(4);
params.add(new BasicNameValuePair("input", "The text that you want to analyze"));
params.add(new BasicNameValuePair("intype", "direct"));
params.add(new BasicNameValuePair("informat", "text"));
params.add(new BasicNameValuePair("outformat", "json-ld"));
params.add(new BasicNameValuePair("algo", "spFinancialEmoticon"));
    
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
     
        while ((inputLine = in.readLine()) != null) {
    	marl.append(inputLine);
      	marl.append("\n");
        }
        in.close();
        String responseInString = marl.toString();
        // Use responseInString as you like
    } finally {
    instream.close();
    }
}
```

An example would look like this:
```
    The request:
    curl --data "input=I feel :)&intype=direct&informat=text&outformat=json-ld&algo=emoticon" http://localhost:8080/SAGAtoNIF/Service
    
    The response:
    {
  "@context": "http://demos.gsi.dit.upm.es/eurosentiment/static/context.jsonld",
    "analysis": [
      {
        "@id": "http://www.gsi.dit.upm.es/ontologies/analysis#SAGA",
        "@type": [
          "marl:SentimentAnalysis"
          ],
        "marl:maxPolarityValue": 1.0,
        "marl:minPolarityValue": -1.0
      }
    ],
    "entries": [
    {
      "nif:isString": "I feel :)",
      "opinions": [
        {
          "@id": "_:Opinion1",
          "marl:hasPolarity": "marl:Positive",
          "marl:polarityValue": 1.0,
          "marl:describesObjectFeature": "Overall"
        }],
      "strings": [
          {  
          "nif:anchorOf": ":)",
          "nif:beginIndex": 7,
          "nif:endIndex": 8,
          "opinions": {
                "@id": "_:Opinion",
                "marl:hasPolarity": "marl:Positive",
                "marl:polarityValue": 1.0
           }
          } 
      ]
    }
  ]
}
```

## Licenses

The source code on this repository is under the following licenses:

```
Copyright (c) 2014 GSI, UPM. All rights reserved.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
```

```
Copyright (c) 2013 GSI, UPM. All rights reserved.

Licensed under Lesser General Public License For Linguistic Resources; 
You may not use this repository and code except in compliance with the License. 
You may obtain a copy of the License at http://www.ida.liu.se/~sarst/bitse/lgpllr.html Unless required by 
applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific 
language governing permissions and limitations under the License.
```

This work has been partialy funded by the Ministry of Industry, Energy and Tourism through the R&D project Financial Twitter Tracker (TSI-090100-2011-114)
![Financial Twitter Tracker](http://demos.gsi.dit.upm.es/ftt/img/ftt_header.png)

EUROSENTIMENT PROJECT
Grant Agreement no: 296277
Starting date: 01/09/2012
Project duration: 24 months

![Eurosentiment Logo](logo_grande.png)
![FP7 logo](logo_fp7.gif)

