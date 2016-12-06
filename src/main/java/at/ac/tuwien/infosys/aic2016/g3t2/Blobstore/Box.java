package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${box.clientId}") private String clientId;
	@Value("${box.clientSecret}") private String clientSecret;
	
	@PostConstruct
	private void init()
	{
		try
		{
			//Key und IDs
			String keyContent="MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDD0dM42MYZoVOlN6Ye1o0fbTnqG9z3rCFFyg6ctN+G1Ymp2PuWKpl/6vFEQf6KBqgTr2wOoaVCWxkIrarshDyK4nJHPPEH5YoWsY+y/5Z2D9SoGPqJFSZ2H/c8zCXiOaESLZaSP7IHdqFO1wFbrZOHbM/us20RX8WrPlTuKNgrOZWFeGfAQxlEWpLD2ygdU+aorAosnsB3ZeVjV0PTI+19srfs9XF5vAezM3a7cAB/OXYm5nC/FJb31UuH5DnkBVAIW4lL7uFM2EgU5UirrZ2YjGlEFAAD0zLZX9jPKtRnXTrojXwO9kEpw6MOhx3OLq9YZjcElqUPdaCoSFrniMkBAgMBAAECggEAD3HQOA6Y6VKZF1HhT1NaxBzIUZerAPnZkfiS2HdHngnflr7fcQOhIb61Es4ltls6DHtsiWbkcuxeeBnCfm8bm1Sq8MV0uUu4bXvJurN/+YXcHvoBYKiDZRO2W1w6rLIg6x8fXh+Z+g64QFCi8ckp9qd1av1J3/Hu1EfRSQt9qUL5C0LnihFtae91FRTCJvNxYVy6OGcM8YZXaNNzASDM+QAmCsgnKKu8BO7x3+jfE5XuSHljalzinQitucS7e+V6IRm5vNpUSKraDNrmPZ54qqaMssOWrmlI8hOMwYXkcvfRMod61Qp22wqnq0iXx/yJGUE9C16rL2tkr4IJfUjPMQKBgQDvYcuqAzETocBsum6awI3W3bf1KS/lSIwNtaVj9TJdXZeY9SqjEvTf/LMSzRwdUQNUgkAipvRT0EhdbEP+CPVbvcgHAReSKDFX+i55LKE/72w8Hcgu2ugGPZJ1/mJePObchFmDHeZYnKkBDZGHSkwJioVjrs8Jgn87kWHGnIR25QKBgQDRadsO0GMfLDEaXxynuYGmXI366CF6ujP7rPV8Y98iYrZiB5TTaY6DQOEGbtw7tdzkXALf6UJvu18ICZNk4bNjjDSTWaVhUcN6l/fIr3+/UeZQDMarRb6VSelHByFpZSM00moWIt7h/RtKfotZ9zc74OEoSqaL2LVpWoR35hFr7QKBgDJn/WLHWUYxATvj5fZgJX3hIiNVkOhO5m7cmP77WqeeVZW3ykqca0PfCjYjSpBBxtm8s5SYY0piyVoiug58BG5VABG5gSUdoYZNAAw0AaBc4gWmn8h9/+2QeY6vCjedy18T32VknmR6WHwIR2SdmHVJ3dynqqDuAayhaF1SB/KhAoGBAMdZ8gMkNXgTZZspN+ojGlz4duCH4ncmkx3fBMKpgF0Hg3/Gn9KyBczFEnJBTr443lM1lDb7oxciU9Ee7IV4poD5k4NCL8F5SJGH4YXWK9JBcPJ7dxTMjCUp+zx2eUQP13gNZpg0EeEoRbagyJ+YS6hg53anuewfHHNyi/Bnv+XxAoGBAONG9tIlQOoyvrKCqPlfMnv7YoSgADGSXWjEibMSMssaeKJXu5EVhYejX6uJ+KL3khL/2U/dVzWWet8yn3NM0aCSp6Kv5vNwnEapikO3RduxFgzqxKnZnAm7Fgwewdu3iQ/V1JXmkeLVHHbrFkVyeRMDdcLZ/yaO7JRp1XSs5tjA";
			String client_id="6lef4wpryuf85m9rydw9scfdmnvj0157";
			String pub_key_id="8fdosegz";
			String enterprise_id="6764195";
			String client_secret="hekId6l8GgyOQnqN1rTJFJMLxrMwnWJ8";
	    	
			//Paramenter fuer JWT erstellen
	    	KeyFactory kf = KeyFactory.getInstance("RSA");
	        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyContent));
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
	    	builder.setHeaderParam("kid", pub_key_id);
	    	builder.claim("iss", client_id);
	    	builder.claim("sub", enterprise_id);
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
			urlParameters.add(new BasicNameValuePair("client_id", client_id));
			urlParameters.add(new BasicNameValuePair("client_secret", client_secret));
			urlParameters.add(new BasicNameValuePair("assertion", jwt));
			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			//HTTP Response parsen
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
			String result=rd.readLine();
			String accesstoken=result.substring(17, 49);
			
			
//			System.out.println(result);
//			System.out.println(accesstoken);
			
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
		
		if(getIdByName(blobname).equals(null))
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
		
		if(getIdByName(blobname).equals(null))
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
