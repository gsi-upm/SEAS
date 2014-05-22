<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<jsp:include page="header.jsp" />
<body>

<div class="col-md-1"></div>
<div class="col-md-3">
<form method="POST" action='Controller' name="controller">
	    <select class="input-group" name="algo">
  			<option value="spFinancial">Sentiment dictionary - Spanish finances - Paradigma</option>
  			<option value="enFinancial">Sentiment dictionary - English finances - Loughran and McDonald</option>
 			<option value="emoticon">Sentiment dictionary - Emoticons</option>
 			<option value="spFinancialEmoticon">Sentiment dictionary - Sp finances and emts - Paradigma</option>
 			<option value="enFinancialEmoticon">Sentiment dictionary - En finances and emts - Loughran and McDonald</option>
 			<option value="onyx">Emotion analysis - Onyxemote</option>
		</select> <p> <p>
    	<input id="text" type="text" class="form-control" name="input" value="${textToAnalyze}" style="height: 122px; width: 246px">
    	<input type="hidden" name="informat" value="text">
    	<input type="hidden" name="intype" value="direct">
    	<input type="hidden" name="outformat" value="json-ld">
    	<p>
    	<p>
    	<input class="btn btn-success" type="submit" value="Analyze">
	</form>
	<hr>
	<p align="justify"><font color="#F8F8F8">SEAS is a set of Sentiment and Emotion Analysis Services according to <a href="http://persistence.uni-leipzig.org/nlp2rdf/"><font color="#D0D0D0">NIF</font></a>. The NLP Interchange Format (NIF) is an RDF/OWL-based format that aims to achieve interoperability between Natural Language Processing (NLP) tools, language resources and annotations.</font><p><p>
	<p align="justify"><font color="#F8F8F8"><i class="fa fa-arrow-right" ></i> Sentiment analysis is generated in <a href="http://www.gi2mo.org/marl/0.1/ns.html"><font color="#D0D0D0">Marl</font></a>.</font><p><p>
	<p align="justify"><font color="#F8F8F8"><i class="fa fa-arrow-right" ></i> Emotion analysis is generated in <a href="http://www.gsi.dit.upm.es/ontologies/onyx/"><font color="#D0D0D0">Onyx</font></a>.</font><p>
	<p align="justify"><font color="#F8F8F8"><i class="fa fa-arrow-right" ></i> Check out our project in <a href="https://github.com/gsi-upm/SEAS"><font color="#D0D0D0">GitHub</font></a>.</font><p>
</div>
  <div class="col-md-1"></div>
  <div class="col-md-6">
  <pre id="eurosentiment" class="${alert}">
  ${eurosentiment}
  </pre>
	</div>

</body>
<footer id="footer">
<font color="#F8F8F8">Brought to you by </font><a href="http://www.gsi.dit.upm.es/index.php/en.html"><img src="http://demos.gsi.dit.upm.es/eurosentiment/static/eurosentiment/img/logo-gsi.png" height="90px"></a><font color="#F8F8F8"></font><img src="http://demos.gsi.dit.upm.es/eurosentiment/static/eurosentiment/img/logo.png" height="90px">
</footer>
</html>