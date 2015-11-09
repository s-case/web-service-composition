package eu.scasefp7.eclipse.servicecomposition.operationCaller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.apache.commons.codec.binary.Base64;

import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView.Implementation;

/**
 * <h1>RAMLCaller</h1> is used for calling RESTful web services and parsing
 * their response.
 * 
 * @author mkoutli
 *
 */
public class RAMLCaller {

	String[] datatypes = new String[] { "string", "long", "int", "float", "double", "dateTime", "boolean" };

	// http://localhost:8080/RESTfulExample/json/product/get
	// http://localhost:8080/AdviceFromIP/rest/result/query?ipAddress=160.40.50.176
	public void callRESTfulOperation(Operation ramlOperation) {

		String wsUrl = ramlOperation.getDomain().getURI() + ramlOperation.getDomain().getResourcePath();
		ArrayList<String> uriParams = new ArrayList<String>();
		for (Argument arg : ramlOperation.getUriParameters()) {
			uriParams.add(arg.getName().getContent().toString());
		}
		String[] uriParamsArr = new String[uriParams.size()];
		uriParamsArr = uriParams.toArray(uriParamsArr);

		if (stringContainsItemFromList(wsUrl, uriParamsArr)) {
			Value val = null;
			for (Argument arg : ramlOperation.getUriParameters()) {
				val = (Value) arg;
				wsUrl = wsUrl.replace("{" + arg.getName().toString() + "}", val.getValue());
			}

		}

		ArrayList<Argument> inputs = ramlOperation.getInputs();
		String inputList = "";
		if (!inputs.isEmpty()) {
			// inputList = "?";
			Value val = null;
			for (Argument input : inputs) {
				if (!inputList.isEmpty())
					inputList += "&";
				val = (Value) input;
				if (!(val.getValue().isEmpty() && !input.isRequired())) {
					inputList += input.getName().toString() + "=" + val.getValue();
				}
			}
		}

		String inputListGet = "";
		if (!inputList.isEmpty()) {

			inputListGet = "?" + inputList;
		}

		String result = "";
		if (ramlOperation.getDomain().getCrudVerb().equalsIgnoreCase("get")) {
			try {

				URL url = new URL(wsUrl + inputListGet);
				System.out.println("Calling " + url.toString());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod(ramlOperation.getDomain().getCrudVerb());
				conn.setRequestProperty("Accept", "application/json");
//				Base64 b = new Base64();
//				String encoding = b.encodeAsString(new String("scase:project").getBytes());
//				conn.setRequestProperty("Authorization", "Basic " + encoding);

				if (conn.getResponseCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}

				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

				String output;
				System.out.println("Output from Server .... \n");
				while ((output = br.readLine()) != null) {
					result += output;
					System.out.println(output);
				}

				conn.disconnect();

			} catch (MalformedURLException e) {

				e.printStackTrace();

			} catch (IOException e) {

				e.printStackTrace();

			}
		} else if (ramlOperation.getDomain().getCrudVerb().equalsIgnoreCase("post")) {

			try {
				// Create connection
				String url = wsUrl;
				System.out.println("Calling " + url.toString());
				HttpClient client = HttpClientBuilder.create().build();
				HttpPost post = new HttpPost(url);
				String USER_AGENT = "Mozilla/5.0";
				// add header
				post.setHeader("User-Agent", USER_AGENT);

				List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
				Value val = null;
				for (Argument input : inputs) {
					val = (Value) input;
					urlParameters.add(new BasicNameValuePair(input.getName().getContent().toString(), val.getValue()));
				}

				post.setEntity(new UrlEncodedFormEntity(urlParameters));

				HttpResponse response = client.execute(post);
				System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

				StringBuffer resultPost = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					resultPost.append(line);
				}
				result = resultPost.toString();
			} catch (MalformedURLException e) {

				e.printStackTrace();

			} catch (IOException e) {

				e.printStackTrace();

			}

		} else if (ramlOperation.getDomain().getCrudVerb().equalsIgnoreCase("delete")) {
			URL url = null;
			try {
				url = new URL(wsUrl);
				System.out.println("Calling " + url.toString());
			} catch (MalformedURLException exception) {
				exception.printStackTrace();
			}
			HttpURLConnection httpURLConnection = null;
			try {
				httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				httpURLConnection.setRequestMethod("DELETE");
				System.out.println(httpURLConnection.getResponseCode());
			} catch (IOException exception) {
				exception.printStackTrace();
			} finally {
				if (httpURLConnection != null) {
					httpURLConnection.disconnect();
				}
			}

		} else if (ramlOperation.getDomain().getCrudVerb().equalsIgnoreCase("put")) {
			try {
				// Create connection
				String url = wsUrl;

				HttpClient client = HttpClientBuilder.create().build();
				HttpPut put = new HttpPut(url);
				String USER_AGENT = "Mozilla/5.0";
				// add header
				put.setHeader("User-Agent", USER_AGENT);

				List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
				Value val = null;
				for (Argument input : inputs) {
					val = (Value) input;
					urlParameters.add(new BasicNameValuePair(input.getName().getContent().toString(), val.getValue()));
				}

				// just for testing
				String content = "<resource><NAME>xname</NAME><PRICE>3</PRICE></resource>";
				StringEntity entity = new StringEntity(content);
				entity.setContentType(new BasicHeader("Content-Type", "application/xml"));
				put.setEntity(entity);
				// end of test

				// put.setEntity(new UrlEncodedFormEntity(urlParameters));

				HttpResponse response = client.execute(put);
				System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

				StringBuffer resultPut = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					resultPut.append(line);
				}
				result = resultPut.toString();
			} catch (MalformedURLException e) {

				e.printStackTrace();

			} catch (IOException e) {

				e.printStackTrace();

			}

		} else {
			System.out
					.println("Operation " + ramlOperation.getDomain().getCrudVerb() + " is not a valid CRUD operation");
		}

		if (!result.isEmpty()) {
			assign(result, ramlOperation);
		}

	}

	protected void assign(String result, Operation ramlOperation) {
		try {
			JSONObject json = (JSONObject) JSONValue.parseWithException(result);

			for (Argument output : ramlOperation.getOutputs()) {
				parseJson(output, json);
			}
			JSONArray origin_addresses = (JSONArray) json.get("origin_addresses");
			JSONArray destination_addresses = (JSONArray) json.get("destination_addresses");
			JSONArray rows = (JSONArray) json.get("rows");
			// for (Argument sub : output.getSubtypes()) {
			// if (sub.getName().toString().equalsIgnoreCase("currency")){
			// ((Value) sub).setValue(currency);
			// }
			// }

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static boolean stringIsItemFromList(String inputString, String[] items) {
		for (int i = 0; i < items.length; i++) {
			if (inputString.equalsIgnoreCase(items[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean stringContainsItemFromList(String inputString, String[] items) {
		for (int i = 0; i < items.length; i++) {
			if (inputString.contains(items[i])) {
				return true;
			}
		}
		return false;
	}

	private void parseJson(Argument output, Object json) {
		if (json instanceof JSONObject) {
			// if it is an array of primitive datatypes e.g boolean
			if (output.isArray() && stringIsItemFromList(output.getType(), datatypes)) {
				JSONArray array = (JSONArray) ((JSONObject) json).get(output.getName().toString());

				for (int i = 0; i < array.size(); i++) {
					if (array.get(i) != null) {
						Argument out;
						try {
							out = new Argument(output.getName().toString() + "[" + i + "]", "", false,
									output.isNative(), output.getSubtypes());
							out.setOwlService(output.getOwlService());
							Value value = new Value(out);
							// Value value = Value.getValue(out);
							value.setValue(array.get(i).toString());
							output.getElements().add(value);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
				// if it is an array of arrays or array of objects
			} else if (output.isArray() && !stringIsItemFromList(output.getType(), datatypes)) {
				JSONArray array = (JSONArray) ((JSONObject) json).get(output.getType());
				boolean subisarray = false;
				for (Argument sub : output.getSubtypes()) {
					if (sub.isArray()) {
						subisarray = true;
					}
				}
				if (subisarray) {
					for (int i = 0; i < array.size(); i++) {
						for (Argument sub : output.getSubtypes()) {
							JSONArray array2 = (JSONArray) ((JSONObject) array.get(i)).get(sub.getName().toString());
							Argument out = new Argument(sub);
							out.setOwlService(sub.getOwlService());
							out.setName(output.getType() + "[" + i + "]");
							Value value = new Value(out);
							// Value value = Value.getValue(out);
							parseJson(value, (JSONObject) array.get(i));
							output.getElements().add(value);
						}
					}

				} else {
					if (array != null) {
						for (int i = 0; i < array.size(); i++) {
							Argument out = new Argument(output);
							out.setOwlService(output.getOwlService());
							out.setName(output.getType() + "[" + i + "]");
							Value value = new Value(out);

							JSONObject object = (JSONObject) array.get(i);
							for (Argument sub : value.getSubtypes()) {
								parseJson(sub, object);
							}

							output.getElements().add(value);
						}
					}
				}

				// if it is an object but not an array
			} else if (!output.isArray() && !stringIsItemFromList(output.getType(), datatypes)) {

				JSONObject object = (JSONObject) ((JSONObject) json).get(output.getName().toString());

				for (Argument sub : output.getSubtypes()) {
					// if (!stringIsItemFromList(sub.getType(), datatypes)) {
					// parseJson(sub, (JSONObject)
					// object.get(sub.getName().toString()));
					// } else if (sub.isArray()) {
					// // to be filled
					// } else {
					if (object.containsKey(sub.getName().toString())) {
						parseJson(sub, object);
					}
					// }

				}

			} else {

				if (output.getType().equalsIgnoreCase("String")) {
					String value = (String) ((JSONObject) json).get(output.getName().toString());
					((Value) output).setValue(value);
				}
				if (output.getType().equalsIgnoreCase("int")) {
					int value = (int) ((JSONObject) json).get(output.getName().toString());
					((Value) output).setValue(Integer.toString(value));
				}
				if (output.getType().equalsIgnoreCase("long")) {
					long value = (long) ((JSONObject) json).get(output.getName().toString());
					((Value) output).setValue(Long.toString(value));
				}
				if (output.getType().equalsIgnoreCase("boolean")) {
					boolean value = (boolean) ((JSONObject) json).get(output.getName().toString());
					((Value) output).setValue(Boolean.toString(value));
				}
				if (output.getType().equalsIgnoreCase("float")) {
					float value = (float) ((JSONObject) json).get(output.getName().toString());
					((Value) output).setValue(Float.toString(value));
				}
				if (output.getType().equalsIgnoreCase("double")) {
					double value = (double) ((JSONObject) json).get(output.getName().toString());
					((Value) output).setValue(Double.toString(value));
				}
				// dateTime needs to be filled
			}

		}
		// else if (json instanceof JSONArray) {
		// if (output.isArray() && stringIsItemFromList(output.getType(),
		// datatypes)) {
		// // to be filled
		//
		// // if sub is an array of objects
		// } else if (output.isArray() &&
		// !stringIsItemFromList(output.getType(), datatypes)) {
		//
		// for (int i = 0; i < ((JSONArray) json).size(); i++) {
		//
		// Argument out = new Argument(output);
		// out.setOwlService(output.getOwlService());
		// out.setName(output.getType() + "[" + i + "]");
		// Value value = new Value(out);
		// for (Argument sub : value.getSubtypes()) {
		// if (sub.isArray()) {
		// JSONArray array2 = (JSONArray) ((JSONArray) json).get(i);
		// Argument out2 = new Argument(sub);
		// out2.setOwlService(sub.getOwlService());
		// out2.setName(output.getType() + "[" + i + "]");
		// Value value2 = new Value(out2);
		// // Value value = Value.getValue(out);
		// parseJson(value2, (JSONArray) json);
		// output.getElements().add(value);
		//
		// } else {
		// JSONObject object = (JSONObject) ((JSONArray) json).get(i);
		// if (object.containsKey(sub.getName().toString())) {
		// parseJson(sub, object);
		// }
		//
		// }
		//
		// }
		// output.getElements().add(value);
		//
		// }
		//
		// } else if (!output.isArray() &&
		// !stringIsItemFromList(output.getType(), datatypes)) {
		// for (int i = 0; i < ((JSONArray) json).size(); i++) {
		// JSONObject object = (JSONObject) ((JSONArray) json).get(i);
		// for (Argument sub : output.getSubtypes()) {
		// parseJson(sub, object);
		// }
		// }
		//
		// } else {
		// if (output.getType().equalsIgnoreCase("String")) {
		// String value = (String) ((JSONObject)
		// json).get(output.getName().toString());
		// ((Value) output).setValue(value);
		// }
		// if (output.getType().equalsIgnoreCase("int")) {
		// int value = (int) ((JSONObject)
		// json).get(output.getName().toString());
		// ((Value) output).setValue(Integer.toString(value));
		// }
		// if (output.getType().equalsIgnoreCase("long")) {
		// long value = (long) ((JSONObject)
		// json).get(output.getName().toString());
		// ((Value) output).setValue(Long.toString(value));
		// }
		// if (output.getType().equalsIgnoreCase("boolean")) {
		// boolean value = (boolean) ((JSONObject)
		// json).get(output.getName().toString());
		// ((Value) output).setValue(Boolean.toString(value));
		// }
		// if (output.getType().equalsIgnoreCase("float")) {
		// float value = (float) ((JSONObject)
		// json).get(output.getName().toString());
		// ((Value) output).setValue(Float.toString(value));
		// }
		// if (output.getType().equalsIgnoreCase("double")) {
		// double value = (double) ((JSONObject)
		// json).get(output.getName().toString());
		// ((Value) output).setValue(Double.toString(value));
		// }
		// }
		// }

	}

}
