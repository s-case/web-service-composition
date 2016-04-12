package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import org.apache.commons.lang3.text.WordUtils;

public class CallRestfulServiceCode {

	public static String generateCode(String packageName) {

		String TAB = "    ";
		// package declaration and imports
		String code = "package " + packageName + ";\n\n" + "import " + packageName + ".WorkflowClass.Variable;\n";

		code += "import java.io.BufferedReader;\nimport java.io.IOException;\nimport java.io.InputStreamReader;\nimport java.net.HttpURLConnection;\nimport java.net.MalformedURLException;\nimport java.net.URL;\nimport java.util.ArrayList;\nimport java.util.List;\nimport org.apache.commons.codec.binary.Base64;\nimport org.apache.http.HttpResponse;\nimport org.apache.http.NameValuePair;\nimport org.apache.http.client.HttpClient;\nimport org.apache.http.client.entity.UrlEncodedFormEntity;\nimport org.apache.http.client.methods.HttpPost;\nimport org.apache.http.client.methods.HttpPut;\nimport org.apache.http.entity.StringEntity;\nimport org.apache.http.impl.client.HttpClientBuilder;\nimport org.apache.http.message.BasicNameValuePair;\n";
		// class declaration
		code += "public class CallRESTfulService {\n";
		// method
		code += TAB + "public static String callService(String wsUrl, String crudVerb, ArrayList<Variable> inputs, String entity, boolean hasAuth, String auth){\n"
				+ TAB + TAB + "String result = \"\";\n" + TAB + TAB + "String inputList = \"\";\n";
		code += TAB + TAB + "if (!inputs.isEmpty()) {\n";
		code += TAB + TAB + TAB + "for (Variable input : inputs) {\n";
		code += TAB + TAB + TAB + TAB + "if (!inputList.isEmpty())\n";
		code += TAB + TAB + TAB + TAB + TAB + "inputList += \"&\";\n";
		code += TAB + TAB + TAB + TAB + "inputList += input.name + \"=\" + input.value;\n";
		code += TAB + TAB + TAB + "}\n";
		code += TAB + TAB + "}\n";
		code += TAB + TAB + "String inputListGet = \"\";\n" + TAB + TAB + "if (!inputList.isEmpty()) {\n";
		code += TAB + TAB + TAB + "inputListGet = \"?\" + inputList;\n";
		code += TAB + TAB + "}\n";

		code += TAB + TAB + "if (crudVerb.equalsIgnoreCase(\"get\")) {\n";
		code += TAB + TAB + TAB + "try {\n";
		code += TAB + TAB + TAB + TAB + "URL url = new URL((wsUrl + inputListGet).replaceAll(\" \",\"%20\"));\n" + TAB + TAB + TAB + TAB
				+ "System.out.println(\"Calling \" + url.toString());\n" + TAB + TAB + TAB + TAB
				+ "HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n" + TAB + TAB + TAB + TAB
				+ "conn.setRequestMethod(\"GET\");\n" + TAB + TAB + TAB + TAB
				+ "conn.setRequestProperty(\"Accept\", \"application/json\");\n";
		code += TAB + TAB + TAB + TAB + "if (hasAuth) {\n";
		code += TAB + TAB + TAB + TAB + TAB + "Base64 b = new Base64();\n";
		code += TAB + TAB + TAB + TAB + TAB + "String encoding = b.encodeAsString(new String(auth).getBytes());\n";
		code += TAB + TAB + TAB + TAB + TAB + "conn.setRequestProperty(\"Authorization\", \"Basic \" + encoding);\n";
		code += TAB + TAB + TAB + TAB + "}\n";
		code += TAB + TAB + TAB + TAB + "if (conn.getResponseCode() != 200) {\n";
		code += TAB + TAB + TAB + TAB + TAB
				+ "throw new RuntimeException(\"Failed : HTTP error code : \" + conn.getResponseCode());\n";
		code += TAB + TAB + TAB + TAB + "}\n";
		code += TAB + TAB + TAB + TAB
				+ "BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));\n";
		code += TAB + TAB + TAB + TAB + "String output;\n" + TAB + TAB + TAB + TAB
				+ "System.out.println(\"Output from Server ....\\n\");\n";
		code += TAB + TAB + TAB + TAB + "while ((output = br.readLine()) != null) {\n";
		code += TAB + TAB + TAB + TAB + TAB + "result += output;\n" + TAB + TAB + TAB + TAB + TAB
				+ "System.out.println(output);\n";
		code += TAB + TAB + TAB + TAB + "}\n";
		code += TAB + TAB + TAB + TAB + "conn.disconnect();\n";
		code += applyTab(3, catchExceptionCode(TAB), TAB);
		code += TAB + TAB + "} else if (crudVerb.equalsIgnoreCase(\"post\")) {\n";
		code += TAB + TAB + TAB + "try {\n";
		code += TAB + TAB + TAB + TAB + "// Create connection\n" + TAB + TAB + TAB + TAB + "String url = (wsUrl + inputListGet).replaceAll(\" \", \"%20\");\n" + TAB
				+ TAB + TAB + TAB + "System.out.println(\"Calling \" + url.toString());\n" + TAB + TAB + TAB + TAB
				+ "HttpClient client = HttpClientBuilder.create().build();\n" + TAB + TAB + TAB + TAB
				+ "HttpPost post = new HttpPost(url);\n" + TAB + TAB + TAB + TAB
				+ "String USER_AGENT = \"Mozilla/5.0\";\n" + TAB + TAB + TAB + TAB + "// add header\n" + TAB + TAB + TAB
				+ TAB + "post.setHeader(\"User-Agent\", USER_AGENT);\n" + TAB + TAB + TAB + TAB
				+ "post.setHeader(\"Content-Type\", \"application/json\");\n" + TAB + TAB + TAB + TAB
				+ "post.setEntity(new StringEntity(entity, \"UTF-8\"));\n";
//		code += TAB + TAB + TAB + TAB + TAB + "urlParameters.add(new BasicNameValuePair(input.name, input.value));\n";
//		code += TAB + TAB + TAB + TAB + "}\n";
//		code += TAB + TAB + TAB + TAB + "post.setEntity(new UrlEncodedFormEntity(urlParameters));\n";
		code += TAB + TAB + TAB + TAB + "HttpResponse response = client.execute(post);\n";
		code += TAB + TAB + TAB + TAB
				+ "System.out.println(\"Response Code : \" + response.getStatusLine().getStatusCode());\n";
		code += TAB + TAB + TAB + TAB
				+ "BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));\n";
		code += TAB + TAB + TAB + TAB + "StringBuffer resultPost = new StringBuffer();\n";
		code += TAB + TAB + TAB + TAB + "String line = \"\";\n";
		code += TAB + TAB + TAB + TAB + "while ((line = rd.readLine()) != null) {\n";
		code += TAB + TAB + TAB + TAB + TAB + "resultPost.append(line);\n";
		code += TAB + TAB + TAB + TAB + "}\n";
		code += TAB + TAB + TAB + TAB + "result = resultPost.toString();\n";
		code += applyTab(3, catchExceptionCode(TAB), TAB);
		code += TAB + TAB + "} else if (crudVerb.equalsIgnoreCase(\"delete\")) {\n";
		code += TAB + TAB + TAB + "URL url = null;\n";
		code += TAB + TAB + TAB + "HttpURLConnection httpURLConnection = null;\n";
		code += TAB + TAB + TAB + "try {\n";
		code += TAB + TAB + TAB + TAB + "url = new URL((wsUrl + inputListGet).replaceAll(\" \", \"%20\"));\n" + TAB + TAB + TAB + TAB
				+ "System.out.println(\"Calling \" + url.toString());\n";
		code += TAB + TAB + TAB + TAB + "httpURLConnection = (HttpURLConnection) url.openConnection();\n";
		code += TAB + TAB + TAB + TAB
				+ "httpURLConnection.setRequestProperty(\"Content-Type\", \"application/x-www-form-urlencoded\");\n";
		code += TAB + TAB + TAB + TAB + "httpURLConnection.setRequestMethod(\"DELETE\");\n";
		code += TAB + TAB + TAB + TAB + "System.out.println(httpURLConnection.getResponseCode());\n";
		code += applyTab(3, catchExceptionCode(TAB), TAB);
		code += TAB + TAB + TAB + "finally {\n";
		code += TAB + TAB + TAB + TAB + "if (httpURLConnection != null) {\n";
		code += TAB + TAB + TAB + TAB + TAB + "httpURLConnection.disconnect();\n";
		code += TAB + TAB + TAB + TAB + "}\n";
		code += TAB + TAB + TAB + "}\n";
		code += TAB + TAB + "} else if (crudVerb.equalsIgnoreCase(\"put\")) {\n";
		code += TAB + TAB + TAB + "try {\n";
		code += TAB + TAB + TAB + TAB + "// Create connection\n";
		code += TAB + TAB + TAB + TAB + "String url = (wsUrl + inputListGet).replaceAll(\" \", \"%20\");\n";
		code += TAB + TAB + TAB + TAB + "HttpClient client = HttpClientBuilder.create().build();\n";
		code += TAB + TAB + TAB + TAB + "HttpPut put = new HttpPut(url);\n";
		code += TAB + TAB + TAB + TAB + "String USER_AGENT = \"Mozilla/5.0\";\n";
		code += TAB + TAB + TAB + TAB + "// add header\n";
		code += TAB + TAB + TAB + TAB + "put.setHeader(\"User-Agent\", USER_AGENT);\n";
		code += TAB + TAB + TAB + TAB + "put.setEntity(new StringEntity(entity, \"UTF-8\"));\n";
//		code += TAB + TAB + TAB + TAB + "for (Variable input : inputs) {\n";
//		code += TAB + TAB + TAB + TAB + TAB + "urlParameters.add(new BasicNameValuePair(input.name, input.value));\n";
//		code += TAB + TAB + TAB + TAB + "}\n";
//		code += TAB + TAB + TAB + TAB + "put.setEntity(new UrlEncodedFormEntity(urlParameters));\n";
		code += TAB + TAB + TAB + TAB + "HttpResponse response = client.execute(put);\n";
		code += TAB + TAB + TAB + TAB
				+ "System.out.println(\"Response Code : \" + response.getStatusLine().getStatusCode());\n";
		code += TAB + TAB + TAB + TAB
				+ "BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));\n";
		code += TAB + TAB + TAB + TAB + "StringBuffer resultPut = new StringBuffer();\n";
		code += TAB + TAB + TAB + TAB + "String line = \"\";\n";
		code += TAB + TAB + TAB + TAB + "while ((line = rd.readLine()) != null) {\n";
		code += TAB + TAB + TAB + TAB + TAB + "resultPut.append(line);\n";
		code += TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + TAB + "result = resultPut.toString();\n";
		code += applyTab(3, catchExceptionCode(TAB), TAB);
		code += TAB + TAB + "} else {\n";
		code += TAB + TAB + TAB
				+ "System.out.println(\"Operation \" + crudVerb + \" is not a valid CRUD operation\");\n";
		code += TAB + TAB + "}\n";
		code += TAB + TAB + "return result;\n";
		code += TAB + "}\n";
//		code += TAB + "public static void assignResult(JSONObject json, Variable output){\n";
//		code += TAB + TAB + "if (output.type.equals(\"object\")) {\n";
//		code += TAB + TAB + TAB + "if (((JSONObject) json).containsKey(output.name)) {\n";
//		code += TAB + TAB + TAB + TAB+ "JSONObject object = (JSONObject) ((JSONObject) json).get(output.name);\n";
//		code += TAB + TAB + TAB + TAB+ "for (Variable sub : output.subtypes) {\n";
//		code += TAB + TAB + TAB + TAB + TAB+ "assignResult((JSONObject) object, sub);\n";
//		code += TAB + TAB + TAB + TAB+ "}\n"+ TAB + TAB + TAB +"}\n";
//		code += TAB + TAB + "} else if (output.type.equals(\"arrayOfObjects\")) {\n";
//		code += TAB + TAB + TAB + "JSONArray array = (JSONArray) ((JSONObject) json).get(output.name);\n";
//		code += TAB + TAB + TAB + "boolean subisarray = false;\n";
//		code += TAB + TAB + TAB + "for (Variable sub : output.subtypes) {\n";
//		code += TAB + TAB + TAB + TAB + "if (sub.type.equals(\"arrayOfObjects\") || sub.type.equals(\"array\")) {\n";
//		code += TAB + TAB + TAB + TAB + TAB + "subisarray = true;\n";
//		code += TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + "}\n";
//		code += TAB + TAB + TAB + "if (subisarray) {\n";
//		code += TAB + TAB + TAB + TAB + "for (int i = 0; i < array.size(); i++) {\n";
//		code += TAB + TAB + TAB + TAB + TAB + "for (Variable sub : output.subtypes) {\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "Variable out= new Variable(sub);\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "assignResult((JSONObject) array.get(i), out);\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "out.name=output.name + \"[\" + i + \"]\";\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "output.arrayElements.add(out);\n";
//		code += TAB + TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + TAB + "}\n";
//		code += TAB + TAB + TAB + "} else {\n";
//		code += TAB + TAB + TAB + TAB + "if (array != null) {\n";
//		code += TAB + TAB + TAB + TAB + TAB + "for (int i = 0; i < array.size(); i++) {\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "Variable out = new Variable(output);\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "out.name = output.name + \"[\" + i + \"]\";\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "out.type= \"object\";\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "JSONObject object = (JSONObject) array.get(i);\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "for (Variable sub : out.subtypes) {\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + TAB + "assignResult(object, sub);\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "}\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "output.arrayElements.add(out);\n";
//		code += TAB + TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + "}\n";
//		code += TAB + TAB + "} else if (output.type.equals(\"array\")) {\n";
//		code += TAB + TAB + TAB + "JSONArray array = (JSONArray) ((JSONObject) json).get(output.name);\n";
//		code += TAB + TAB + TAB + "for (int i = 0; i < array.size(); i++) {\n";
//		code += TAB + TAB + TAB + TAB + "if (array.get(i) != null) {\n";
//		code += TAB + TAB + TAB + TAB + TAB + "Variable out;\n";
//		code += TAB + TAB + TAB + TAB + TAB + "try {\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "out = new Variable(output.name + \"[\" + i + \"]\", \"\", \"String\");\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "out.value = array.get(i).toString();\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "output.arrayElements.add(out);\n";
//		code += TAB + TAB + TAB + TAB + TAB + "} catch (Exception e) {\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "// TODO Auto-generated catch block\n";
//		code += TAB + TAB + TAB + TAB + TAB + TAB + "e.printStackTrace();\n";
//		code += TAB + TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + TAB + "}\n"+ TAB + TAB + TAB + "}\n";		
//		code += TAB + TAB + "} else {\n";
//		code += applyTab(3, assignPrimitive(TAB, "String"), TAB);
//		code += applyTab(3, assignPrimitive(TAB, "int"), TAB);
//		code += applyTab(3, assignPrimitive(TAB, "long"), TAB);
//		code += applyTab(3, assignPrimitive(TAB, "boolean"), TAB);
//		code += applyTab(3, assignPrimitive(TAB, "float"), TAB);
//		code += applyTab(3, assignPrimitive(TAB, "double"), TAB);
//		code += TAB + TAB + "}\n";
//		code += TAB + "}\n";
		code += "}\n";

		return code;
	}

	private static String catchExceptionCode(String TAB) {
		String code = "} catch (MalformedURLException e) {\n";
		code += TAB + "e.printStackTrace();\n";
		code += "} catch (IOException e) {\n";
		code += TAB + "e.printStackTrace();\n";
		code += "}\n";

		return code;
	}

	private static String assignPrimitive(String TAB, String type) {
		String code = "if (output.type.equalsIgnoreCase(\"" + type + "\")) {\n";
		if (type.equalsIgnoreCase("String")) {
			code += TAB + type + " value = (" + type + ") ((JSONObject) json).get(output.name);\n";
		} else {
			code += TAB + type + " value = (" + type + ") ((JSONObject) json).get(output.name.toString());\n";
		}
		if (type.equalsIgnoreCase("String")) {
			code += TAB + "output.value=value;\n";
		} else if (type.equalsIgnoreCase("int")) {
			code += TAB + "output.value=(Integer.toString(value));\n";
		} else {
			code += TAB + "output.value=(" + WordUtils.capitalize(type) + ".toString(value));\n";
		}
		code += "}\n";

		return code;
	}

	protected static String applyTab(int tabs, String code, String TAB) {
		String lines[] = code.split("\n");
		code = "";
		String tabIndent = "";
		for (int tab = 0; tab < tabs; tab++)
			tabIndent += TAB;
		for (String line : lines) {
			if (line.trim().isEmpty())
				continue;
			code += tabIndent + line + "\n";
		}
		return code;
	}
}
