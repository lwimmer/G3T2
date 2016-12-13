package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Lazy
public class Box implements IBlobstore {
	
	BoxAPIConnection api = null;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${box.clientId}") private String clientId;
	@Value("${box.clientSecret}") private String clientSecret;
	@Value("${box.pubkeyId}") private String pubkeyId;
	@Value("${box.enterpriseId}") private String enterpriseId;
	@Value("${box.privkeyContent}") private String privkeyContent;

	@PostConstruct
	private void init()
	{
		logger.info("Trying Box login");
		try
		{
			//Paramenter fuer JWT erstellen
	    	KeyFactory kf = KeyFactory.getInstance("RSA");
	        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privkeyContent));
	        PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
	    	SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS512;
	    	String uniqueID = UUID.randomUUID().toString();
	    	long nowMillis = System.currentTimeMillis();
	        long expMillis = nowMillis + 6000;
	        Date exp = new Date(expMillis);
	    	
	        //JWT erstellen
	    	JwtBuilder builder=Jwts.builder();	
	    	builder.setHeaderParam("alg", "RS512");
	    	builder.setHeaderParam("typ", "JWT");
	    	builder.setHeaderParam("kid", pubkeyId);
	    	builder.claim("iss", clientId);
	    	builder.claim("sub", enterpriseId);
	    	builder.claim("box_sub_type", "enterprise");
	    	builder.claim("aud", "https://api.box.com/oauth2/token");
	    	builder.claim("jti", uniqueID);
	    	builder.setExpiration(exp);
	    	builder.signWith(signatureAlgorithm, privKey);
	    	String jwt=builder.compact();   	
	    	
	    	//HTTP Request erstellen
	    	HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("https://www.box.com/api/oauth2/token");	
			post.setHeader("Content-Type", "application/x-www-form-urlencoded");		
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"));
			urlParameters.add(new BasicNameValuePair("client_id", clientId));
			urlParameters.add(new BasicNameValuePair("client_secret", clientSecret));
			urlParameters.add(new BasicNameValuePair("assertion", jwt));
			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			//HTTP Response parsen
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
			String result=rd.readLine();
			JSONObject json = new JSONObject(result);
			String accesstoken=json.getString("access_token");
			
			logger.info("Got access token: {} from reply {}", accesstoken, result);
			
			api=new BoxAPIConnection(accesstoken);
		}
		
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
    	
	}
	

	@Override
	public boolean create(String blobname, byte[] data) {
		
		try
		{
			InputStream input = new ByteArrayInputStream(data);
	    	BoxFolder rootFolder = BoxFolder.getRootFolder(api);
	    	
	    	if (getIdByName(blobname) != null) 
			{
				delete(blobname);
			}
	    	rootFolder.uploadFile(input, blobname);
	    	input.close();
	    	
	    	return true;
		}
		
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			return false;
		}
	}

	@Override
	public boolean delete(String blobname) throws ItemMissingException {
		
		if(getIdByName(blobname) == null)
		{
			throw new ItemMissingException();
		}
		
		else
		{
			BoxFile file = new BoxFile(api, getIdByName(blobname));
			file.delete();
			return true;
		}
		
		
	}

	@Override
	public Blob read(String blobname) throws ItemMissingException {
		
		if(getIdByName(blobname) == null)
		{
			throw new ItemMissingException();
		}
		
		else
		{
			try
			{
				BoxFile file = new BoxFile(api, getIdByName(blobname));
				
				ByteArrayOutputStream output=new ByteArrayOutputStream();
				file.download(output);
				
				byte[] data=output.toByteArray();		
				
				output.close();
				
				return new Blob(data);
			}
			
			catch(Exception e)
			{
				System.out.println(e.getMessage());
				return null;
			}
		}
		
	}

	@Override
	public List<String> listBlobs() {
		
		List<String> liste=new ArrayList<String>();
		
		 BoxFolder rootFolder = BoxFolder.getRootFolder(api);
		 for (BoxItem.Info itemInfo : rootFolder) 
		 {
		        liste.add(itemInfo.getName());
		 }
		 
		 return liste;
		
	}
	
	public String getIdByName(String name)
	{
		String id=null;
		
		BoxFolder rootFolder = BoxFolder.getRootFolder(api);
		for (BoxItem.Info itemInfo : rootFolder) 
		{
			if(itemInfo.getName().equals(name))
			{
				id=itemInfo.getID();
			}
		}
		
		return id;
	}
	
//	public static void main( String[] args ) throws Exception
//    {
//		Box box=new Box();
//		box.init();
//		
//		List<String> liste=box.listBlobs();
//		
//		for(String s:liste)
//		{
//			System.out.println(s);
//		}
//    }
	
	

}
