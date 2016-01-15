package eu.scasefp7.eclipse.servicecomposition.repository;

import java.io.BufferedReader;
import org.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.net.URL;
//import java.net.URLConnection;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.TimeZone;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

//import javax.faces.context.FacesContext;
//import javax.servlet.ServletContext;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
//import org.apache.http.entity.mime.MultipartEntityBuilder;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.entity.ContentType;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class RepositoryClient {
	private String apiKey = "1cfae05f-9e67-486f-820b-b393dec5764b";
	private String url = "http://109.231.126.165:8080";
	// private String apiKey = "1cfae05f-9e67-486f-820b-b393dec5764b";
	// private String url = "http://repo.scasefp7.com:8080";

	// public String sendGetRequest(String endpoint, String requestParameters) {
	// String result = "";
	// if (endpoint.startsWith("http://")) {
	// // Send a GET request to the servlet
	// try {
	//
	// // Construct data
	// StringBuffer data = new StringBuffer();
	// // Send data
	// String urlStr = endpoint;
	// if (requestParameters != null && requestParameters.length() > 0) {
	// urlStr += "?" + requestParameters;
	// }
	// URL url = new URL(urlStr);
	// URLConnection conn = url.openConnection();
	// conn.setReadTimeout(10000);
	// // Get the response
	// BufferedReader rd = new BufferedReader(new InputStreamReader(
	// conn.getInputStream()));
	// // BufferedWriter out = new BufferedWriter(new
	// // FileWriter(pathToSave));
	// String line;
	// while ((line = rd.readLine()) != null) {
	// // out.write(line + "\n");
	// result = result + line;
	// }
	// rd.close();
	// // out.close();
	//
	// // System.out.println("Total elapsed time for download ontology :"
	// // + (endTime - startTime));
	// // File fil = new File(pathToSave);
	// // long size = fil.length() / 1024;
	// // System.out.println(size + "KB");
	// // System.out.println(pathToSave);
	// return result;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return result;
	// }
	// }
	// return result;
	// }

	// public String searchTerm(String term) {
	// try {
	// String response = Request
	// .Get(url + "/search?q=" + term + "&apikey=" + apiKey)
	// .connectTimeout(10000).socketTimeout(10000).execute()
	// .returnContent().asString();
	// System.out.println(response);
	// return response;
	// // return
	// // sendGetRequest(url+"/search?q="+term+"&apikey="+apiKey,"");
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// return ex.getMessage();
	// }
	// }

	// public String listAllOntologies() {
	// try {
	// String response = Request.Get(url + "/ontologies?apikey=" + apiKey)
	// .connectTimeout(10000).socketTimeout(10000).execute()
	// .returnContent().asString();
	// System.out.println(response);
	// return response;
	// // return
	// // sendGetRequest(url+"/search?q="+term+"&apikey="+apiKey,"");
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// return ex.getMessage();
	// }
	// }

	public String downloadOntology(String text, Display disp) {
		try {
			// FacesContext context = FacesContext.getCurrentInstance();

			// String path = ((ServletContext) context.getExternalContext()
			// .getContext()).getRealPath("/");
			String latestSubmission = getLatestSubmissionId();
			
			
			String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
			String requestURL="";
			if(latestSubmission.equals(""))
				requestURL=url + "/ontologies/" + text + "/download?apikey=" + apiKey;
				else
					requestURL=url + "/ontologies/WS/submissions/"+latestSubmission + "/download?apikey=" + apiKey;
			InputStream inputStream = Request.Get(requestURL)
					.connectTimeout(20000).socketTimeout(20000).execute().returnContent().asStream();

			OutputStream outputStream = null;
			File file = new File(path + "/" + text + ".owl");
			System.out.println(file.getAbsolutePath());
			outputStream = new FileOutputStream(file);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			inputStream.close();
			outputStream.close();

			// sendGetRequest(url + "/ontologies/"+text+"/download?apikey=" +
			// apiKey,"",text + ".owl");
			return path + "/" + text + ".owl";
			// return
			// sendGetRequest(url+"/search?q="+term+"&apikey="+apiKey,"");
		} catch (Exception ex) {
			ex.printStackTrace();
			
			disp.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
							"Ontology could not be downloaded. Local ontology will be used instead!");
				}
			});
			return "";
		}
	}

	public boolean uploadOntology() {
		try {
			String boundary = "-------------boundary";
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost uploadFile = new HttpPost(url + "/ontologies/WS/submissions");
			uploadFile.addHeader("Authorization", "apikey token=" + apiKey);
			uploadFile.addHeader("Content-Type",
					"multipart/mixed; boundary=" + boundary + "; type=application/json; start=json");
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setBoundary(boundary);
			TimeZone tz = TimeZone.getTimeZone(TimeZone.getDefault().getID());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			df.setTimeZone(tz);
			String nowAsISO = df.format(new Date());
			String jsonStr = "{\"ontology\":\"" + url
					+ "/ontologies/WS\",\"description\":\"This is an ontology for the semantic annotation of web services\",\"hasOntologyLanguage\":\"OWL\",\"authorProperty\":\"\",\"obsoleteParent\":\"\",\"obsoleteProperty\":\"\",\"version\":\"\",\"status\":\"\",\"released\":\""
					+ nowAsISO
					+ "0\",\"isRemote\":\"0\",\"contact\":[{\"name\":\"Kostas Giannoutakis\",\"email\":\"kgiannou@iti.gr\"}],\"publication\":\"\"}";

			builder.addTextBody("json", jsonStr, ContentType.APPLICATION_JSON);
			String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
			builder.addBinaryBody("file", new File(path + "/WS.owl"), ContentType.TEXT_PLAIN, "WS.owl");

			HttpEntity multipart = builder.build();
			uploadFile.setEntity(multipart);
			HttpResponse response = httpClient.execute(uploadFile);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private  String getLatestSubmissionId() {
		String apiKey = "1cfae05f-9e67-486f-820b-b393dec5764b";
		String url = "http://109.231.126.165:8080";
		try {
			// get latest submission
			InputStream inputStream = Request.Get(url + "/ontologies/" + "WS" + "/submissions?apikey=" + apiKey)
					.connectTimeout(20000).socketTimeout(20000).execute().returnContent().asStream();
			Object obj = JSONValue.parse(convertStreamToString(inputStream));
			JSONArray array = (JSONArray) obj;
			JSONObject obj2 = (JSONObject) array.get(0);
			return obj2.get("submissionId").toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

	
}
