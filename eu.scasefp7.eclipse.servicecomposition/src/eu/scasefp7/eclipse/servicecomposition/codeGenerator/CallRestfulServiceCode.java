package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

public class CallRestfulServiceCode {

	public static String generateCode(String packageName, boolean foreman) {

		String TAB = "    ";
		// package declaration and imports
		String code = "package " + packageName + ";\n\n" + "import " + packageName + ".WorkflowClass.Variable;\n";

		code += "import java.io.BufferedReader;\nimport java.io.IOException;\nimport java.io.InputStreamReader;\nimport java.io.DataOutputStream;\nimport java.net.HttpURLConnection;\nimport java.net.MalformedURLException;\nimport java.net.URL;\nimport java.util.ArrayList;\nimport org.apache.commons.codec.binary.Base64;\n";
		if (foreman)
			code += "import java.security.GeneralSecurityException;\nimport java.security.cert.X509Certificate;\nimport javax.net.ssl.HttpsURLConnection;\nimport javax.net.ssl.SSLContext;\nimport javax.net.ssl.TrustManager;\nimport javax.net.ssl.X509TrustManager;\n\n";
		// class declaration
		code += "public class CallRESTfulService {\n";
		// method
		code += TAB
				+ "public static String callService(String wsUrl, String crudVerb, ArrayList<Variable> inputs, String entity, boolean hasAuth, String auth){\n"
				+ TAB + TAB + "String result = \"\";\n" + TAB + TAB + "String inputList = \"\";\n";
		code += TAB + TAB + "if (!inputs.isEmpty()) {\n";
		code += TAB + TAB + TAB + "for (Variable input : inputs) {\n";
		code += TAB + TAB + TAB + TAB + "if (input.value !=null && !input.value.isEmpty()) {\n";
		code += TAB + TAB + TAB + TAB + TAB + "if (!inputList.isEmpty())\n";
		code += TAB + TAB + TAB + TAB + TAB + TAB + "inputList += \"&\";\n";
		code += TAB + TAB + TAB + TAB + TAB + "inputList += input.name + \"=\" + input.value;\n";
		code += TAB + TAB + TAB + TAB + "}\n";
		code += TAB + TAB + TAB + "}\n";
		code += TAB + TAB + "}\n\n";
		code += TAB + TAB + "// instead of using URLEncoder\n" + TAB + TAB
				+ "inputList = inputList.replaceAll(\"\\\\{\", \"%7B\");\n";
		code += TAB + TAB + "inputList = inputList.replaceAll(\"\\\\}\", \"%7D\");\n" + TAB + TAB
				+ "inputList = inputList.replaceAll(\"\\\\[\", \"%5B\");\n";
		code += TAB + TAB + "inputList = inputList.replaceAll(\"\\\\]\", \"%5D\");\n" + TAB + TAB
				+ "inputList = inputList.replaceAll(\"\\\\:\", \"%3A\");\n\n";
		code += TAB + TAB + "String inputListGet = \"\";\n" + TAB + TAB + "if (!inputList.isEmpty()) {\n";
		code += TAB + TAB + TAB + "inputListGet = \"?\" + inputList;\n";
		code += TAB + TAB + "}\n";
		if (foreman) {
			code += TAB + TAB + "if (wsUrl.startsWith(\"https://foreman.res.eng.it\")) {\n";
			code += TAB + TAB + TAB + "byPassCert();\n";
			code += TAB + TAB + "}\n";
		}
		code += TAB + TAB
				+ "if (crudVerb.equalsIgnoreCase(\"get\") || crudVerb.equalsIgnoreCase(\"post\") || crudVerb.equalsIgnoreCase(\"delete\") || crudVerb.equalsIgnoreCase(\"put\")) {\n";
		code += TAB + TAB + TAB + "HttpURLConnection conn = null;\n";
		code += TAB + TAB + TAB + "try {\n";
		code += TAB + TAB + TAB + TAB + "URL url = new URL((wsUrl + inputListGet).replaceAll(\" \",\"%20\"));\n" + TAB
				+ TAB + TAB + TAB + "System.out.println(\"Calling \" + url.toString());\n" + TAB + TAB + TAB + TAB
				+ "conn = (HttpURLConnection) url.openConnection();\n" + TAB + TAB + TAB + TAB
				+ "conn.setConnectTimeout(30000);\n" + TAB + TAB + TAB + TAB + "conn.setRequestMethod(crudVerb);\n";
		code += TAB + TAB + TAB + TAB + "if (hasAuth) {\n";
		code += TAB + TAB + TAB + TAB + TAB + "Base64 b = new Base64();\n";
		code += TAB + TAB + TAB + TAB + TAB + "String encoding = b.encodeAsString(new String(auth).getBytes());\n";
		code += TAB + TAB + TAB + TAB + TAB + "conn.setRequestProperty(\"Authorization\", \"Basic \" + encoding);\n";
		code += TAB + TAB + TAB + TAB + "}\n";
		code += TAB + TAB + TAB + TAB
				+ "if (crudVerb.equalsIgnoreCase(\"post\") || crudVerb.equalsIgnoreCase(\"put\")) {\n";
		code += TAB + TAB + TAB + TAB + TAB + "if (url.toString().contains(\"mailgun\")) {\n";
		code += TAB + TAB + TAB + TAB + TAB + TAB + "for (Variable arg : inputs) {\n";
		code += TAB + TAB + TAB + TAB + TAB + TAB + TAB + "if (arg.name.equals(\"apikey\")) {\n";
		code += TAB + TAB + TAB + TAB + TAB + TAB + TAB + TAB + "Base64 b = new Base64();\n" + TAB + TAB + TAB + TAB
				+ TAB + TAB + TAB + TAB
				+ "String encoding = b.encodeAsString(new String(\"api:\" + arg.value).getBytes());\n" + TAB + TAB + TAB
				+ TAB + TAB + TAB + TAB + TAB + "conn.setRequestProperty(\"Authorization\", \"Basic \"+encoding);\n"
				+ TAB + TAB + TAB + TAB + TAB + TAB + TAB + TAB + "break;\n";
		code += TAB + TAB + TAB + TAB + TAB + TAB + TAB + "}\n";
		code += TAB + TAB + TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + TAB + TAB + TAB
				+ "conn.setDoOutput(true);\n" + TAB + TAB + TAB + TAB + TAB + TAB
				+ "DataOutputStream wr = new DataOutputStream(conn.getOutputStream());\n" + TAB + TAB + TAB + TAB + TAB
				+ TAB + "wr.writeBytes(entity);\n" + TAB + TAB + TAB + TAB + TAB + TAB + "wr.flush();\n" + TAB + TAB
				+ TAB + TAB + TAB + TAB + "wr.close();\n";
		code += TAB + TAB + TAB + TAB + TAB + "} else {\n";
		code += TAB + TAB + TAB + TAB + TAB + TAB + "conn.setRequestProperty(\"Content-Type\", \"application/json\");\n"
				+ TAB + TAB + TAB + TAB + TAB + TAB + "conn.setDoInput(true);\n" + TAB + TAB + TAB + TAB + TAB + TAB
				+ "conn.setDoOutput(true);\n" + TAB + TAB + TAB + TAB + TAB + TAB
				+ "DataOutputStream os = new DataOutputStream(conn.getOutputStream());\n" + TAB + TAB + TAB + TAB + TAB
				+ TAB + "os.writeBytes(entity);\n";
		code += TAB + TAB + TAB + TAB + TAB + "}\n";
		code += TAB + TAB + TAB + TAB + "}\n";

		code += TAB + TAB + TAB + TAB + "if (conn.getResponseCode() != 200 && conn.getResponseCode() != 201 && conn.getResponseCode() != 301) {\n";
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
		// code += TAB + TAB + TAB + TAB + "conn.disconnect();\n";
		code += applyTab(3, catchExceptionCode(TAB), TAB);
		code += TAB + TAB + TAB + "finally {\n" + TAB + TAB + TAB + TAB + "if (conn != null) {\n" + TAB + TAB + TAB
				+ TAB + TAB + "conn.disconnect();\n" + TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + "}\n";
		code += TAB + TAB + "} else {\n";
		code += TAB + TAB + TAB
				+ "System.out.println(\"Operation \" + crudVerb + \" is not a valid CRUD operation\");\n";
		code += TAB + TAB + "}\n";
		code += TAB + TAB + "return result;\n";
		code += TAB + "}\n";
		if (foreman) {
			code += TAB + "private static void byPassCert() {\n";
			code += TAB + TAB + "// Create a trust manager that does not validate certificate chains\n";
			code += TAB + TAB + "TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {\n";
			code += TAB + TAB + TAB + "public java.security.cert.X509Certificate[] getAcceptedIssuers() {\n";
			code += TAB + TAB + TAB + TAB + "return new X509Certificate[0];\n";
			code += TAB + TAB + TAB + "}\n\n";
			code += TAB + TAB + TAB
					+ "public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {\n";
			code += TAB + TAB + TAB + "}\n";
			code += TAB + TAB + TAB
					+ "public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {\n";
			code += TAB + TAB + TAB + "}\n";
			code += TAB + TAB + "} };\n\n";
			code += TAB + TAB + "// Install the all-trusting trust manager\n";
			code += TAB + TAB + "try {\n";
			code += TAB + TAB + TAB + "SSLContext sc = SSLContext.getInstance(\"SSL\");\n" + TAB + TAB + TAB
					+ "sc.init(null, trustAllCerts, new java.security.SecureRandom());\n" + TAB + TAB + TAB
					+ "HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());\n";
			code += TAB + TAB + "} catch (GeneralSecurityException e) {\n";
			code += TAB + TAB + "}\n";
			code += TAB + "}\n";
		}
		code += "}\n";

		return code;
	}

	private static String catchExceptionCode(String TAB) {
		String code = "} catch (MalformedURLException e) {\n";
		code += TAB + "e.printStackTrace();\n";
		code += "} catch (IOException e) {\n";
		code += TAB + "e.printStackTrace();\n";
		code += "}";

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
