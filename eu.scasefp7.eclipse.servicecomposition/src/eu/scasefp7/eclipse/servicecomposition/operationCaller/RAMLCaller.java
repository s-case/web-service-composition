package eu.scasefp7.eclipse.servicecomposition.operationCaller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
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

	/**
	 * primitive datatypes
	 */
	String[] datatypes = new String[] { "string", "long", "int", "float", "double", "dateTime", "boolean" };

	// http://localhost:8080/RESTfulExample/json/product/get
	// http://localhost:8080/AdviceFromIP/rest/result/query?ipAddress=160.40.50.176
	public void callRESTfulOperation(Operation ramlOperation) throws Exception {

		String wsUrl = ramlOperation.getDomain().getURI();
		if (ramlOperation.getDomain().getResourcePath() != null) {
			wsUrl += ramlOperation.getDomain().getResourcePath();
		}
		ArrayList<String> uriParamNames = new ArrayList<String>();
		ArrayList<Argument> uriParams = new ArrayList<Argument>();
		ArrayList<Argument> queryParams = new ArrayList<Argument>();
		ArrayList<Argument> bodyParams = new ArrayList<Argument>();
		for (Argument arg : ramlOperation.getInputs()) {
			if (arg.isTypeOf().equals("URIParameter")) {
				uriParamNames.add(arg.getName().getContent().toString());
				uriParams.add(arg);
			} else if (arg.isTypeOf().equals("QueryParameter")) {
				queryParams.add(arg);
			} else if (arg.isTypeOf().equals("BodyParameter")) {
				bodyParams.add(arg);
			}
		}
		String[] uriParamsArr = new String[uriParamNames.size()];
		uriParamsArr = uriParamNames.toArray(uriParamsArr);

		if (stringContainsItemFromList(wsUrl, uriParamsArr)) {
			Value val = null;
			for (Argument arg : uriParams) {
				val = (Value) arg;
				wsUrl = wsUrl.replace("{" + arg.getName().toString() + "}", val.getValue());
			}

		}

		// ArrayList<Argument> inputs = ramlOperation.getInputs();
		String inputList = "";
		if (!queryParams.isEmpty()) {
			// inputList = "?";
			Value val = null;
			for (Argument input : queryParams) {
				if (!inputList.isEmpty()) {
					val = (Value) input;
					if (!(val.getValue().isEmpty() && !input.isRequired()) && val.getValue() != "enter value") {
						inputList += "&" + input.getName().toString() + "=" + val.getValue();
					}
				} else {
					val = (Value) input;
					if (!(val.getValue().isEmpty() && !input.isRequired()) && val.getValue() != "enter value") {
						inputList += input.getName().toString() + "=" + val.getValue();
					}
				}
			}
		}

		String inputListGet = "";
		if (!inputList.isEmpty()) {

			inputListGet = "?" + inputList;
		}

		String result = "";
		if (ramlOperation.getDomain().getCrudVerb().equalsIgnoreCase("get")) {
			HttpURLConnection conn = null;
			try {

				URL url = new URL((wsUrl + inputListGet).replaceAll(" ", "%20"));
				System.out.println("Calling " + url.toString());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(30000);
				conn.setRequestMethod(ramlOperation.getDomain().getCrudVerb());
				conn.setRequestProperty("Accept", "application/json");
				if (ramlOperation.getDomain().getSecurityScheme() != null) {
					if (ramlOperation.getDomain().getSecurityScheme().equalsIgnoreCase("Basic Authentication")) {
						Base64 b = new Base64();
						String username = ((Value) ramlOperation.getAuthenticationParameters().get(0)).getValue();
						String password = ((Value) ramlOperation.getAuthenticationParameters().get(1)).getValue();
						String encoding = b.encodeAsString(new String(username + ":" + password).getBytes());
						conn.setRequestProperty("Authorization", "Basic " + encoding);
					}
				}

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

				// conn.disconnect();

			} catch (MalformedURLException e) {

				e.printStackTrace();

			} catch (IOException e) {
				e.printStackTrace();
				throw new IOException();

			} finally {
				if (conn != null) {
					try {
						if (conn.getInputStream() != null) {
							conn.getInputStream().close();
						}
						conn.disconnect();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else if (ramlOperation.getDomain().getCrudVerb().equalsIgnoreCase("post")) {

			try {
				// Create connection
				String url = (wsUrl + inputListGet).replaceAll(" ", "%20");
				System.out.println("Calling " + url.toString());
				HttpClient client = HttpClientBuilder.create().build();
				HttpPost post = new HttpPost(url);
				String USER_AGENT = "Mozilla/5.0";
				// add header
				post.setHeader("User-Agent", USER_AGENT);
				post.setHeader("Content-Type", "application/json");
				// List<NameValuePair> urlParameters = new
				// ArrayList<NameValuePair>();
				// Value val = null;
				// for (Argument input : bodyParams) {
				// val = (Value) input;
				// urlParameters.add(new
				// BasicNameValuePair(input.getName().getContent().toString(),
				// val.getValue()));
				// }
				//

				// String request = "Journey={\"journeyLegs\":[
				// {\"transportType\":\"ECAR\", \"geoLocationPoints\":[
				// {\"latitude\":44.8500574, \"longitude\":13.8368621,
				// \"eta\":0}, {\"latitude\":44.8500531,
				// \"longitude\":13.8370427, \"eta\":2}] },
				// {\"transportType\":\"CAR\", \"geoLocationPoints\":[
				// {\"latitude\":44.8500531, \"longitude\":13.8370427,
				// \"eta\":2}, {\"latitude\":44.8500574,
				// \"longitude\":13.8368621, \"eta\":5}] }]}";
				JSONObject obj = new JSONObject();
				for (Argument input : bodyParams) {
					obj = (JSONObject) createJson(input, obj);
				}
				// String journey= "Journey=" + obj.toString();
				String entity = obj.toString();
				post.setEntity(new StringEntity(entity, "UTF-8"));
				// post.setEntity(new StringEntity(request, "UTF-8"));
				// post.setEntity(new UrlEncodedFormEntity(urlParameters));

				HttpResponse response = client.execute(post);
				//System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
				}
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

				StringBuffer resultPost = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					resultPost.append(line);
				}
				result = resultPost.toString();
				System.out.println(result);
			} catch (MalformedURLException e) {

				e.printStackTrace();

			} catch (IOException e) {

				e.printStackTrace();
				throw new IOException();
			}

		} else if (ramlOperation.getDomain().getCrudVerb().equalsIgnoreCase("delete")) {
			URL url = null;
			try {
				url = new URL((wsUrl + inputListGet).replaceAll(" ", "%20"));
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
				throw new IOException();
			} finally {
				if (httpURLConnection != null) {
					httpURLConnection.disconnect();
				}
			}

		} else if (ramlOperation.getDomain().getCrudVerb().equalsIgnoreCase("put")) {
			try {
				// Create connection
				String url = (wsUrl + inputListGet).replaceAll(" ", "%20");

				HttpClient client = HttpClientBuilder.create().build();
				HttpPut put = new HttpPut(url);
				String USER_AGENT = "Mozilla/5.0";
				// add header
				put.setHeader("User-Agent", USER_AGENT);

				// List<NameValuePair> urlParameters = new
				// ArrayList<NameValuePair>();
				// Value val = null;
				// for (Argument input : bodyParams) {
				// val = (Value) input;
				// urlParameters.add(new
				// BasicNameValuePair(input.getName().getContent().toString(),
				// val.getValue()));
				// }

				JSONObject obj = new JSONObject();
				for (Argument input : bodyParams) {
					obj = (JSONObject) createJson(input, obj);
				}
				// String journey= "Journey=" + obj.toString();
				put.setEntity(new StringEntity(obj.toString(), "UTF-8"));

				// // just for testing
				// String content =
				// "<resource><NAME>xname</NAME><PRICE>3</PRICE></resource>";
				// StringEntity entity = new StringEntity(content);
				// entity.setContentType(new BasicHeader("Content-Type",
				// "application/xml"));
				// put.setEntity(entity);
				// // end of test

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
				throw new IOException();
			}

		} else {
			System.out
					.println("Operation " + ramlOperation.getDomain().getCrudVerb() + " is not a valid CRUD operation");
			throw new Exception();
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
							out = new Argument(output.getName().toString() + "[" + i + "]", "", "BodyParameter", false,
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
				for (int i = 0; i < array.size(); i++) {
					// create and add site[0]element, device[0]
					Argument out = new Argument(output);
					out.setOwlService(output.getOwlService());
					out.setName(output.getType() + "[" + i + "]");
					Value value = new Value(out);

					for (Argument sub : output.getSubtypes()) {
						if (sub.isArray()) {
							JSONArray array2 = (JSONArray) ((JSONObject) array.get(i)).get(sub.getName().toString());
							// if (output.getType().equals(sub.getType())){
							// create device[], metrics[]
							Argument out1 = new Argument(sub);
							out1.setOwlService(sub.getOwlService());
							out1.setName(sub.getName().toString());
							Value value2 = new Value(out1);
							parseJson(value2, (JSONObject) array.get(i));

							// add device[] metrics[]
							value.getElements().add(value2);

							// }else{
							// //create device,metrics
							// Argument out1 = new Argument(sub);
							// out1.setOwlService(sub.getOwlService());
							// out1.setName(sub.getName().toString());
							// Value value1 = new Value(out1);
							// //add device, metrics
							// value.getElements().add(value1);
							// //create device[0],[1], metrics[0],[1]
							// Argument out2 = new Argument(sub);
							// out2.setOwlService(sub.getOwlService());
							// out2.setName(sub.getName().toString());
							// Value value2 = new Value(out2);
							//
							//
							// parseJson(value2, (JSONObject) array.get(i));
							//
							// //add device[0],[1], metrics[0],[1]
							// value1.getElements().add(value2);
							// }
						} else {

							// create device
							Argument out1 = new Argument(sub);
							out1.setOwlService(sub.getOwlService());
							out1.setName(sub.getName().toString());
							Value value1 = new Value(out1);
							JSONObject object = (JSONObject) array.get(i);

							parseJson(value1, (object));

							value.getElements().add(value1);
						}
					}
					output.getElements().add(value);
				}

				// if it is an object but not an array
			} else if (!output.isArray() && !stringIsItemFromList(output.getType(), datatypes)) {
				if (((JSONObject) json).containsKey(output.getName().toString())) {
					JSONObject object = (JSONObject) ((JSONObject) json).get(output.getName().toString());

					for (Argument sub : output.getSubtypes()) {

						if (object.containsKey(sub.getName().toString())) {
							parseJson(sub, object);
						}

					}
				}

			} else {

				if (output.getType().equalsIgnoreCase("String")) {
					if (((JSONObject) json).get(output.getName().toString()) == null) {
						((Value) output).setValue("null");
					} else {
						String value = (String) ((JSONObject) json).get(output.getName().toString());
						((Value) output).setValue(value);
					}
				}
				// if (output.getType().equalsIgnoreCase("int")) {
				// int value = (int) ((JSONObject)
				// json).get(output.getName().toString());
				// ((Value) output).setValue(Integer.toString(value));
				// }
				if (output.getType().equalsIgnoreCase("long") || output.getType().equalsIgnoreCase("int")) {
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
					double value;
					try {
						value = (double) ((JSONObject) json).get(output.getName().toString());
					} catch (Exception ex) {
						String str = (String) (((JSONObject) json).get(output.getName().toString()));
						value = Double.parseDouble(str);
					}
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

	private Object createJson(Argument input, Object obj) throws IOException {
		// JSONObject obj = new JSONObject();
		// array of primitive
		if (input.isArray() && stringIsItemFromList(input.getType(), datatypes)) {
			JSONArray array = new JSONArray();
			for (Value element : input.getElements()) {
				if (element.getValue() != "enter value" && element.getValue() != "matched"){
					if (input.getType().equalsIgnoreCase("String")) {
						array.add(element.getValue());
					}else if (input.getType().equalsIgnoreCase("int")) {
						array.add(Integer.parseInt(element.getValue()));
					}else if (input.getType().equalsIgnoreCase("long")) {
						array.add(Long.parseLong(element.getValue()));
					}else if (input.getType().equalsIgnoreCase("boolean")) {
						array.add(Boolean.parseBoolean(element.getValue()));
					}else if (input.getType().equalsIgnoreCase("float")) {
						array.add(Float.parseFloat(element.getValue()));
					}else if (input.getType().equalsIgnoreCase("double")) {
						array.add(Double.parseDouble(element.getValue()));
					}
					
				}
			}
			((JSONObject) obj).put(input.getName().getContent().toString(), array);
			// array of objects or arrays
		} else if (input.isArray() && !stringIsItemFromList(input.getType(), datatypes)) {
			JSONArray array = new JSONArray();
			for (Value element : input.getElements()) {
				if (element.isArray()) {
					JSONArray array2 = new JSONArray();
					JSONObject obj2 = new JSONObject();
					for (Value element2 : element.getElements()) {
						obj2 = (JSONObject) createJson(element2, obj2);
						array2.add(obj2);
					}
					((JSONArray) array).add(obj2);

				} else {
					JSONObject obj3 = new JSONObject();
					for (Argument subinput : input.getSubtypes()) {
						obj3 = (JSONObject) createJson(subinput, obj3);
					}
					((JSONArray) array).add(obj3);
				}

			}

			((JSONObject) obj).put(input.getName().getContent().toString(), array);
			// object
		} else if (!input.isArray() && !stringIsItemFromList(input.getType(), datatypes)) {
			JSONObject obj2 = new JSONObject();

			for (Argument subinput : input.getSubtypes()) {
				obj2 = (JSONObject) createJson(subinput, obj2);
			}
			((JSONObject) obj).put(input.getName().getContent().toString(), obj2);
			// primitive
		} else {
			if (input.getType().equalsIgnoreCase("String")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (val.getValue() != "enter value" && val.getValue() != "matched")
						((JSONObject) obj).put(input.getName().getContent().toString(), val.getValue());
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(val.getValue());
				}
			}
			if (input.getType().equalsIgnoreCase("int")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (val.getValue() != "enter value" && val.getValue() != "matched")
						((JSONObject) obj).put(input.getName().getContent().toString(),
								Integer.parseInt(val.getValue()));
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(Integer.parseInt(val.getValue()));
				}
			}
			if (input.getType().equalsIgnoreCase("long")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (val.getValue() != "enter value" && val.getValue() != "matched")
						((JSONObject) obj).put(input.getName().getContent().toString(), Long.parseLong(val.getValue()));
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(Long.parseLong(val.getValue()));
				}
			}
			if (input.getType().equalsIgnoreCase("boolean")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (val.getValue() != "enter value" && val.getValue() != "matched")
						((JSONObject) obj).put(input.getName().getContent().toString(),
								Boolean.parseBoolean(val.getValue()));
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(Boolean.parseBoolean(val.getValue()));
				}
			}
			if (input.getType().equalsIgnoreCase("float")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (val.getValue() != "enter value" && val.getValue() != "matched")
						((JSONObject) obj).put(input.getName().getContent().toString(),
								Float.parseFloat(val.getValue()));
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(Float.parseFloat(val.getValue()));
				}
			}
			if (input.getType().equalsIgnoreCase("double")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (val.getValue() != "enter value" && val.getValue() != "matched")
						((JSONObject) obj).put(input.getName().getContent().toString(),
								Double.parseDouble(val.getValue()));
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(Double.parseDouble(val.getValue()));
				}

			}

		}

		// StringWriter out = new StringWriter();
		// ((JSONObject) obj).writeJSONString(out);
		//
		// String jsonText = out.toString();
		return obj;
	}

}
