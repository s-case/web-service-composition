package eu.scasefp7.eclipse.servicecomposition.views;

import java.io.File;

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

public class UploadCompositeService {
	private static final String TARGET_URL = "http://109.231.127.61:8080/SCServer/rest/file/upload/";
	private int status = 0;
	

	public boolean exists(String fileName) throws Exception {
		// check if file exists
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.CONNECT_TIMEOUT, 1000000);
		client.property(ClientProperties.READ_TIMEOUT, 1000000);
		WebTarget webTarget = client.target("http://109.231.127.61:8080/SCServer/rest/file").path(fileName);

		String responseString = webTarget.request("text/plain").get(String.class);
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
		WebTarget webTarget = client.target(TARGET_URL);
		
		if (monitor.isCanceled()){
			status = 1;
			return;
		}
			
		
		MultiPart multiPart = new MultiPart();
		multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

		FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", new File(path),
				MediaType.APPLICATION_OCTET_STREAM_TYPE);

		multiPart.bodyPart(fileDataBodyPart);
		
		Response response = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(multiPart, multiPart.getMediaType()));

		System.out.println(response.getStatus() + " " + response.getStatusInfo() + " " + response);
		
		status = response.getStatus();
	}
	
	public int getUploadStatus(){
		return status;
	}
}
