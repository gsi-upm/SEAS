<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>SAGAtoNIF</title>
</head>
<body>
	<form method="POST" action='Service' name="service">
	    <select name="algo">
  			<option value="spFinancial">Spanish financial dictionaries</option>
 			<option value="emoticon">Emoticons</option>
 			<option value="spFinancialEmoticon">Spanish financial and emoticon dictionaries</option>
		</select> <p> <p>
    	<input type="text" class="form-control" name="input" value="Insert your spanish financial and emoticon text here." style="height: 122px; width: 246px">
    	<input type="hidden" name="informat" value="text">
    	<input type="hidden" name="intype" value="direct">
    	<input type="hidden" name="outformat" value="json-ld">
    	<p>
    	<p>
    	<input class="btn btn-success" type="submit" value="Analyze">
	</form>
</body>
</html>