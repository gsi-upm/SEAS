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
  			<option value="spFinancial">Spanish financial dictionaries</option>
  			<option value="enFinancial">English financial dictionaries</option>
 			<option value="emoticon">Emoticons</option>
 			<option value="spFinancialEmoticon">Spanish financial and emoticon dictionaries</option>
 			<option value="enFinancialEmoticon">English financial and emoticon dictionaries</option>
 			<option value="ANEW2010All">ANEW 2010</option>
 			<option value="ANEW2010Men">ANEW 2010 Men</option>
 			<option value="ANEW2010Women">ANEW 2010 Women</option>
		</select> <p> <p>
    	<input type="text" class="form-control" name="input" value="${textToAnalyze}" style="height: 122px; width: 246px">
    	<input type="hidden" name="informat" value="text">
    	<input type="hidden" name="intype" value="direct">
    	<input type="hidden" name="outformat" value="json-ld">
    	<p>
    	<p>
    	<input class="btn btn-success" type="submit" value="Analyze">
	</form>
</div>
  <div class="col-md-1"></div>
  <div class="col-md-6">
  <pre id="eurosentiment" class="${alert}">
  ${eurosentiment}
  </pre>
	</div>

</body>
<footer id="footer">
<font color="#F8F8F8">Brought to you by the research </font><a href="http://www.gsi.dit.upm.es/index.php/en.html">Group on Intelligent Systems [GSI]</a><font color="#F8F8F8">, Technical University of Madrid [UPM]</font>
</footer>
</html>