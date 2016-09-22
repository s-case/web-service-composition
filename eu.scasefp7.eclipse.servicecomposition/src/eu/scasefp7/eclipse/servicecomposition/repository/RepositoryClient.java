package eu.scasefp7.eclipse.servicecomposition.repository;

import java.io.BufferedReader;
import org.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import com.auth0.jwt.JWTSigner;

import eu.scasefp7.eclipse.servicecomposition.Activator;

import java.io.ByteArrayInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
//import java.net.URL;
//import java.net.URLConnection;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.TimeZone;
import java.util.TimeZone;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

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
import org.glassfish.jersey.client.ClientProperties;

public class RepositoryClient {
	/**
	 * apikey for accessing the server
	 */
	private String apiKey = "1cfae05f-9e67-486f-820b-b393dec5764b";
	/**
	 * url of the server
	 */
	private String url = "http://109.231.126.165:8080";
	private String CTUrl = "https://app.scasefp7.com:8000/api/proxy";
	private static JWTSigner signer = new JWTSigner("RXs7eUv1E-qROLL7UX25_w5up6284KpJE6gygLPvQhcUSwY"); // scase_secret as parameter
	private String scase_token = "yDjOpZaBdiVtlXp5Z6KNFhopeKpEdOJTFlWJvPiBZ7atW9Y"; // scase_token
	
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

	public String downloadOntology(String text, String serverVersion, Display disp) {

		// FacesContext context = FacesContext.getCurrentInstance();

		// String path = ((ServletContext) context.getExternalContext()
		// .getContext()).getRealPath("/");
		String signature = createSignature();
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.CONNECT_TIMEOUT, 1000000);
		client.property(ClientProperties.READ_TIMEOUT, 1000000);
		//String latestSubmission = getLatestSubmissionId(text);
		String latestSubmission = serverVersion;

		String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
				+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology";
		String requestURL = "";
		if (latestSubmission.equals(""))
			requestURL = CTUrl + "/ontologies/" + text + "/download?apikey=" + apiKey;
		else
			requestURL = CTUrl + "/ontologies/" + text + "/submissions/" + latestSubmission + "/download?apikey="
					+ apiKey;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		// write the version file
		if (text.equals("WS")) {
			File versionFile = new File(path + "/version.txt");
			if (!versionFile.exists()) {
				versionFile.getParentFile().mkdirs();
			} else {
				versionFile.delete();
			}
			try {
				versionFile.createNewFile();
				RandomAccessFile f = new RandomAccessFile(versionFile, "rw");
				f.seek(0); // to the beginning
				String version = "submissionId=" + latestSubmission;
				f.write(version.getBytes());
				f.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// write the ontology file
		try {
			Response response = client.target(requestURL).request().header("AUTHORIZATION", "CT-AUTH " + scase_token + ":" + signature).get();
			System.out.println("Get ontology");

			String resString = response.readEntity(String.class);
			JSONObject obj = (JSONObject) JSONValue.parseWithException(resString);
			String body =  (String)obj.get("body");
			inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));;
			File file = new File(path + "/" + text + ".owl");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			System.out.println(file.getAbsolutePath());
			outputStream = new FileOutputStream(file);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			// inputStream.close();
			// outputStream.close();

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
					MessageDialog.openInformation(disp.getActiveShell(), "Warning!",
							"Ontology could not be downloaded. Local ontology will be used instead!");
				}
			});
			return "";
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {

					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}
	
	
	public String downloadPWOntology(String text, String serverVersion, Display disp) {

		//String latestSubmission = getLatestSubmissionId(text);
		String latestSubmission = serverVersion;

		String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
				+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology";
		String requestURL = "";
		if (latestSubmission.equals(""))
			requestURL = url + "/ontologies/" + text + "/download?apikey=" + apiKey;
		else
			requestURL = url + "/ontologies/" + text + "/submissions/" + latestSubmission + "/download?apikey="
					+ apiKey;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		

		// write the ontology file
		try {
			inputStream = Request.Get(requestURL).connectTimeout(30000).socketTimeout(30000).execute()
					.returnContent().asStream();
			System.out.println("Get PW ontology");

			
			File file = new File(path + "/" + text + ".owl");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			System.out.println(file.getAbsolutePath());
			outputStream = new FileOutputStream(file);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			
			return path + "/" + text + ".owl";
			
		} catch (Exception ex) {
			ex.printStackTrace();

			disp.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Warning!",
							"Ontology could not be downloaded. Local ontology will be used instead!");
				}
			});
			return "";
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {

					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}

	public boolean uploadOntology() {
		try {
			String boundary = "-------------boundary";
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost uploadFile = new HttpPost(CTUrl + "/ontologies/WS/submissions?apikey=" + apiKey);
			String signature = createSignature();
			uploadFile.addHeader("AUTHORIZATION", "CT-AUTH " + scase_token + ":" + signature);
			//uploadFile.addHeader("Authorization", "apikey token=" + apiKey);
			uploadFile.addHeader("Content-Type",
					"multipart/mixed; boundary=" + boundary + "; type=application/json; start=json");
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setBoundary(boundary);
			TimeZone tz = TimeZone.getTimeZone(TimeZone.getDefault().getID());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			df.setTimeZone(tz);
			String nowAsISO = df.format(new Date());
			String jsonStr = "{\"ontology\":\"http://109.231.126.165:8080/ontologies/WS\",\"description\":\"This is an ontology for the semantic annotation of web services\",\"hasOntologyLanguage\":\"OWL\",\"authorProperty\":\"\",\"obsoleteParent\":\"\",\"obsoleteProperty\":\"\",\"version\":\"\",\"status\":\"\",\"released\":\""
					+ nowAsISO
					+ "0\",\"isRemote\":\"0\",\"contact\":[{\"name\":\"Kostas Giannoutakis\",\"email\":\"kgiannou@iti.gr\"}],\"publication\":\"\"}";

			builder.addTextBody("json", jsonStr, ContentType.APPLICATION_JSON);
			String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
					+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology";
			builder.addBinaryBody("file", new File(path + "/WS.owl"), ContentType.TEXT_PLAIN, "WS.owl");

			HttpEntity multipart = builder.build();
			uploadFile.setEntity(multipart);
			HttpResponse response = httpClient.execute(uploadFile);
			System.out.println(response);
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

	public String getLatestSubmissionId(String ontology) {
		String url = "http://109.231.126.165:8080";
		String signature = createSignature();
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.CONNECT_TIMEOUT, 1000000);
		client.property(ClientProperties.READ_TIMEOUT, 1000000);
		try {
			// get latest submission
			Response response = client.target(CTUrl + "/ontologies/WS/submissions?apikey=" + apiKey).request().header("AUTHORIZATION", "CT-AUTH " + scase_token + ":" + signature).get();
			System.out.println("Check version");

			String resString = response.readEntity(String.class);
			//System.out.println(resString);
			JSONObject obj = (JSONObject) JSONValue.parseWithException(resString);
			String body =  (String)obj.get("body");
			JSONArray array = (JSONArray)JSONValue.parseWithException(body);
			JSONObject obj2 = (JSONObject) array.get(0);
			String id= obj2.get("submissionId").toString();
			System.out.println(id);
			return id;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

	/**
	 * Copy the WS.owl and the version.txt files from the plug -in to the
	 * workspace /.metadata/.plugins/eu.scasefp7.servicecomposition/ontology
	 * folder if they don't already exist
	 */
	public void copyOntologyToWorkspace() {
		// copy the ontology file
		URL owlFileurl;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
				+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology";
		try {

			File file = new File(path + "/WS.owl");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();

				owlFileurl = new URL("platform:/plugin/" + Activator.PLUGIN_ID + "/ontology/WS.owl");
				inputStream = owlFileurl.openConnection().getInputStream();

				System.out.println(file.getAbsolutePath());
				outputStream = new FileOutputStream(file);

				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				// inputStream.close();
				// outputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {

					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		InputStream inputStream2 = null;
		OutputStream outputStream2 = null;
		try {
			File versionfile = new File(path + "/version.txt");
			if (!versionfile.exists()) {
				URL versionFileurl = new URL("platform:/plugin/" + Activator.PLUGIN_ID + "/ontology/version.txt");
				inputStream2 = versionFileurl.openConnection().getInputStream();
				versionfile.createNewFile();

				System.out.println(versionfile.getAbsolutePath());
				outputStream2 = new FileOutputStream(versionfile);

				int read2 = 0;
				byte[] bytes2 = new byte[1024];

				while ((read2 = inputStream2.read(bytes2)) != -1) {
					outputStream2.write(bytes2, 0, read2);
				}
				// inputStream2.close();
				// outputStream2.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream2 != null) {
				try {
					inputStream2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream2 != null) {
				try {

					outputStream2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}
	/**
	 * createSignature
	 * @return signature for connection with CT
	 */
	private String createSignature(){
		HashMap<String, Object> claims = new HashMap<String, Object>();
		claims.put("token", scase_token);

		// create the signature
		String signature = signer.sign(claims);
		return signature;
	}

}
