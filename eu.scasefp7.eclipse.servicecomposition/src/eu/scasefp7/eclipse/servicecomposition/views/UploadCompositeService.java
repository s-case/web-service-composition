package eu.scasefp7.eclipse.servicecomposition.views;

import java.io.File;
import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.auth0.jwt.JWTSigner;

public class UploadCompositeService {
	private static final String TARGET_URL = "http://109.231.127.61:8080/SCServer/rest/file/upload/";
	private int status = 0;
	private String CTUrl = "https://app.scasefp7.com:8000/api/proxy";
	private static JWTSigner signer = new JWTSigner("RXs7eUv1E-qROLL7UX25_w5up6284KpJE6gygLPvQhcUSwY"); // scase_secret as parameter
	private String scase_token = "yDjOpZaBdiVtlXp5Z6KNFhopeKpEdOJTFlWJvPiBZ7atW9Y"; // scase_token

	public boolean exists(String fileName) throws Exception {
		// check if file exists
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.CONNECT_TIMEOUT, 1000000);
		client.property(ClientProperties.READ_TIMEOUT, 1000000);
		WebTarget webTarget = client.target(CTUrl + "/SCServer/rest/file").path(fileName);

		String resString = webTarget.request().header("AUTHORIZATION", "CT-AUTH " + scase_token + ":" + createSignature()).get(String.class);
		System.out.println(resString);
		JSONObject obj = (JSONObject) JSONValue.parseWithException(resString);
		String responseString = (String) (obj.get("body"));
		System.out.println(responseString);
		if (responseString.equals("true")) {
			return true;
		}
		return false;

		// System.out.println(response.getEntity());

	}

	public void upload(String path, IProgressMonitor monitor) throws Exception {

		// upload file
		Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
		client.property(ClientProperties.CONNECT_TIMEOUT, 1000000);
		client.property(ClientProperties.READ_TIMEOUT, 1000000);
		WebTarget webTarget = client.target(CTUrl + "/SCServer/rest/file/upload");
		
		if (monitor.isCanceled()){
			status = 1;
			return;
		}
			
		
		MultiPart multiPart = new MultiPart();
		multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

		FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", new File(path),
				MediaType.APPLICATION_OCTET_STREAM_TYPE);

		multiPart.bodyPart(fileDataBodyPart);
		
		Response response = webTarget.request().header("AUTHORIZATION", "CT-AUTH " + scase_token + ":" + createSignature())
				.post(Entity.entity(multiPart, multiPart.getMediaType()));

		String resString = response.readEntity(String.class);
		System.out.println(resString);
		
		System.out.println(response.getStatus() + " " + response.getStatusInfo() + " " + response);
		
		status = response.getStatus();
	}
	
	public int getUploadStatus(){
		return status;
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
