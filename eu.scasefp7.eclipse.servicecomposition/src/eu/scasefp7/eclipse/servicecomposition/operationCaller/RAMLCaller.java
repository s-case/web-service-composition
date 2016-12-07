package eu.scasefp7.eclipse.servicecomposition.operationCaller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.apache.commons.codec.binary.Base64;

import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.RequestHeader;

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
		ArrayList<Argument> formParams = new ArrayList<Argument>();
		for (Argument arg : ramlOperation.getInputs()) {
			if (arg.isTypeOf().equals("URIParameter")) {
				uriParamNames.add(arg.getName().getContent().toString());
				uriParams.add(arg);
			} else if (arg.isTypeOf().equals("QueryParameter")) {
				queryParams.add(arg);
			} else if (arg.isTypeOf().equals("BodyParameter")) {
				bodyParams.add(arg);
			} else if (arg.isTypeOf().equals("FormEncodedParameter")) {
				formParams.add(arg);
			}
		}
		String[] uriParamsArr = new String[uriParamNames.size()];
		uriParamsArr = uriParamNames.toArray(uriParamsArr);

		if (stringContainsItemFromList(wsUrl, uriParamsArr)) {
			Value val = null;
			for (Argument arg : uriParams) {
				val = (Value) arg;
				if (val.getValue().equals("matched")) {
					val.setValue("");
				}
				wsUrl = wsUrl.replace("{" + arg.getName().toString() + "}", val.getValue());
			}

		}

		// ArrayList<Argument> inputs = ramlOperation.getInputs();
		String inputList = "";
		if (!queryParams.isEmpty()) {
			// inputList = "?";
			Value val = null;
			for (Argument input : queryParams) {
				val = (Value) input;
				if (!(val.getValue().isEmpty() && !input.isRequired()) && !val.getValue().equals("enter value")) {
					if (!inputList.isEmpty()) {

						inputList += "&";

					}
					if (val.getValue().equals("matched")) {
						val.setValue("");
					}
					inputList += input.getName().toString() + "=" + val.getValue();
				}

			}
		}

		// instead of using URLEncoder
		inputList = inputList.replaceAll("\\{", "%7B");
		inputList = inputList.replaceAll("\\}", "%7D");
		inputList = inputList.replaceAll("\\[", "%5B");
		inputList = inputList.replaceAll("\\]", "%5D");
		inputList = inputList.replaceAll("\\:", "%3A");

		String inputListGet = "";
		if (!inputList.isEmpty()) {

			inputListGet = "?" + inputList;
		}

		////// only for eng services////////
		//////// test/////////////
		if (wsUrl.startsWith("https://foreman.res.eng.it")) {
			byPassCert();
		}

		String result = "";
		String crudVerb = ramlOperation.getDomain().getCrudVerb();

		if (crudVerb.equalsIgnoreCase("get") || crudVerb.equalsIgnoreCase("post") || crudVerb.equalsIgnoreCase("delete")
				|| crudVerb.equalsIgnoreCase("put")) {

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
				if (ramlOperation.getRequestHeaders() != null) {
					for (RequestHeader header : ramlOperation.getRequestHeaders()) {
						conn.setRequestProperty(header.getName(), header.getValue());
					}
				}
				// if it is post or put request
				if (ramlOperation.getDomain().getCrudVerb().equalsIgnoreCase("post")
						|| ramlOperation.getDomain().getCrudVerb().equalsIgnoreCase("put")) {
					// if it is request to mailgun
					if (ramlOperation.getName().toString().equals("POST_messages")) {
						List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
						Value val = null;
						for (Argument input : formParams) {
							val = (Value) input;
							urlParameters.add(
									new BasicNameValuePair(input.getName().getContent().toString(), val.getValue()));
						}
						StringBuilder sb = new StringBuilder();
						boolean first = true;

						for (NameValuePair pair : urlParameters) {
							if (first)
								first = false;
							else
								sb.append("&");

							sb.append(URLEncoder.encode(pair.getName(), "UTF-8"));
							sb.append("=");
							sb.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
						}
						Value apikeyVal = null;
						for (Argument arg : uriParams) {
							apikeyVal = (Value) arg;
							if (arg.getName().toString().equals("apikey")) {
								Base64 b = new Base64();
								String encoding = b
										.encodeAsString(new String("api:" + apikeyVal.getValue()).getBytes());
								conn.setRequestProperty("Authorization", "Basic " + encoding);
								break;
							}

						}

						conn.setDoOutput(true);
						DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
						wr.writeBytes(sb.toString());
						wr.flush();
						wr.close();

					} else {
						conn.setRequestProperty("Content-Type", "application/json");
						conn.setRequestProperty("User-Agent", "Mozilla/5.0");
						//conn.setRequestProperty("Content-transfer-encoding", "binary");
						
						conn.setDoInput(true);
						conn.setDoOutput(true);
						JSONObject obj = new JSONObject();
						for (Argument input : bodyParams) {
							obj = (JSONObject) createJson(input, obj);
						}
						String entity = obj.toString();
						DataOutputStream os = new DataOutputStream(conn.getOutputStream());
						os.writeBytes(entity);
					}
				}

				if (conn.getResponseCode() != 200 && conn.getResponseCode() != 201 && conn.getResponseCode() != 301) {
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
				System.out.println(e.toString());
				throw new IOException();

			} finally {
				if (conn != null) {
					// try {
					// if (conn.getInputStream() != null) {
					// conn.getInputStream().close();
					// }
					//
					// } catch (Exception e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// } finally {
					conn.disconnect();
					// }
				}
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
			if (JSONValue.parseWithException(result) instanceof JSONArray){
				result = "{\"\":" + result + "}";
			}
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
				if (((JSONObject) json).containsKey(output.getName().toString())) {
					JSONArray array = (JSONArray) ((JSONObject) json).get(output.getName().toString());

					for (int i = 0; i < array.size(); i++) {
						if (array.get(i) != null) {
							Argument out;
							try {
								out = new Argument(output.getName().toString() + "[" + i + "]", "", "BodyParameter",
										false, output.isNative(), output.getSubtypes());
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
				}
				// if it is an array of arrays or array of objects
			} else if (output.isArray() && !stringIsItemFromList(output.getType(), datatypes)) {
				if (((JSONObject) json).containsKey(output.getName().toString())) {
					if (((JSONObject) json).get(output.getType()) instanceof JSONArray) {
						JSONArray array = (JSONArray) ((JSONObject) json).get(output.getType());
						for (int i = 0; i < array.size(); i++) {
							// create and add site[0]element, device[0]
							Argument out = new Argument(output);
							out.setOwlService(output.getOwlService());
							out.setName(output.getType() + "[" + i + "]");
							Value value = new Value(out);

							for (Argument sub : output.getSubtypes()) {
								if (sub.isArray()) {
									JSONArray array2 = (JSONArray) ((JSONObject) array.get(i))
											.get(sub.getName().toString());
									// if
									// (output.getType().equals(sub.getType())){
									// create device[], metrics[]
									Argument out1 = new Argument(sub);
									out1.setOwlService(sub.getOwlService());
									out1.setName(sub.getName().toString());
									Value value2 = new Value(out1);
									parseJson(value2, (JSONObject) array.get(i));

									// add device[] metrics[]
									value.getElements().add(value2);

									
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
					} else {
						output.setIsArray(false);
						parseJson(output, json);
					}
				}
				// if it is an object but not an array
			} else if (!output.isArray() && !stringIsItemFromList(output.getType(), datatypes)) {
				if (((JSONObject) json).containsKey(output.getName().toString())
						&& !(((JSONObject) json).get(output.getName().toString()) instanceof JSONArray)) {
					JSONObject object = (JSONObject) ((JSONObject) json).get(output.getName().toString());

					for (Argument sub : output.getSubtypes()) {

						if (object != null && object.containsKey(sub.getName().toString())) {
							parseJson(sub, object);
						}

					}
				} else if ((((JSONObject) json).get(output.getName().toString()) instanceof JSONArray)) {
					output.setIsArray(true);
					parseJson(output, json);
				}

			} else {
				if (((JSONObject) json).get(output.getName().toString()) == null) {
					((Value) output).setValue("");
				} else {
					if (output.getType().equalsIgnoreCase("String")) {

						String value = (String) ((JSONObject) json).get(output.getName().toString());
						((Value) output).setValue(value);

					}
					
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
							if (((JSONObject) json).get(output.getName().toString()) instanceof Long) {
								Long lng = (Long) (((JSONObject) json).get(output.getName().toString()));
								value = lng.doubleValue();
							} else if (((JSONObject) json).get(output.getName().toString()) instanceof String) {
								String str = (String) (((JSONObject) json).get(output.getName().toString()));
								value = Double.parseDouble(str);
							} else {
								value = 0.0;
							}
						}
						((Value) output).setValue(Double.toString(value));
					}
					// dateTime needs to be filled
				}
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
				if (!element.getValue().equals("enter value") && !element.getValue().equals("matched")) {
					if (input.getType().equalsIgnoreCase("String")) {
						array.add(element.getValue());
					} else if (input.getType().equalsIgnoreCase("int")) {
						array.add(Integer.parseInt(element.getValue()));
					} else if (input.getType().equalsIgnoreCase("long")) {
						array.add(Long.parseLong(element.getValue()));
					} else if (input.getType().equalsIgnoreCase("boolean")) {
						array.add(Boolean.parseBoolean(element.getValue()));
					} else if (input.getType().equalsIgnoreCase("float")) {
						array.add(Float.parseFloat(element.getValue()));
					} else if (input.getType().equalsIgnoreCase("double")) {
						array.add(Double.parseDouble(element.getValue()));
					}

				}
			}
			if (!array.isEmpty() || input.isRequired()) {
				((JSONObject) obj).put(input.getName().getContent().toString(), array);
			}
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
					if (!obj2.isEmpty() || input.isRequired()) {
						((JSONArray) array).add(obj2);
					}

				} else {
					JSONObject obj3 = new JSONObject();
					for (Argument subinput : input.getSubtypes()) {
						obj3 = (JSONObject) createJson(subinput, obj3);
					}
					if (!obj3.isEmpty() || input.isRequired()) {
						((JSONArray) array).add(obj3);
					}
				}

			}
			if (!array.isEmpty() || input.isRequired()) {
				((JSONObject) obj).put(input.getName().getContent().toString(), array);
			}
			// object
		} else if (!input.isArray() && !stringIsItemFromList(input.getType(), datatypes)) {
			JSONObject obj2 = new JSONObject();

			for (Argument subinput : input.getSubtypes()) {
				obj2 = (JSONObject) createJson(subinput, obj2);
			}
			if (!obj2.isEmpty() || input.isRequired()) {
				((JSONObject) obj).put(input.getName().getContent().toString(), obj2);
			}
			// primitive
		} else {
			if (input.getType().equalsIgnoreCase("String")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (!val.getValue().equals("enter value") && !val.getValue().equals("matched")
							&& !val.getValue().isEmpty())
						((JSONObject) obj).put(input.getName().getContent().toString(), val.getValue());
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(val.getValue());
				}
			}
			if (input.getType().equalsIgnoreCase("int")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (!val.getValue().equals("enter value") && !val.getValue().equals("matched")
							&& !val.getValue().isEmpty())
						((JSONObject) obj).put(input.getName().getContent().toString(),
								Integer.parseInt(val.getValue()));
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(Integer.parseInt(val.getValue()));
				}
			}
			if (input.getType().equalsIgnoreCase("long")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (!val.getValue().equals("enter value") && !val.getValue().equals("matched")
							&& !val.getValue().isEmpty())
						((JSONObject) obj).put(input.getName().getContent().toString(), Long.parseLong(val.getValue()));
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(Long.parseLong(val.getValue()));
				}
			}
			if (input.getType().equalsIgnoreCase("boolean")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (!val.getValue().equals("enter value") && !val.getValue().equals("matched")
							&& !val.getValue().isEmpty())
						((JSONObject) obj).put(input.getName().getContent().toString(),
								Boolean.parseBoolean(val.getValue()));
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(Boolean.parseBoolean(val.getValue()));
				}
			}
			if (input.getType().equalsIgnoreCase("float")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (!val.getValue().equals("enter value") && !val.getValue().equals("matched")
							&& !val.getValue().isEmpty())
						((JSONObject) obj).put(input.getName().getContent().toString(),
								Float.parseFloat(val.getValue()));
				} else if (obj instanceof JSONArray) {
					((JSONArray) obj).add(Float.parseFloat(val.getValue()));
				}
			}
			if (input.getType().equalsIgnoreCase("double")) {
				Value val = (Value) input;
				if (obj instanceof JSONObject) {
					if (!val.getValue().equals("enter value") && !val.getValue().equals("matched")
							&& !val.getValue().isEmpty())
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

	private void byPassCert() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
		}
		// Now you can access an https URL without having the certificate in the
		// truststore
		try {
			URL url = new URL("https://hostname/index.html");
		} catch (MalformedURLException e) {
		}
	}

}
