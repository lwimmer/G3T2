package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;

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
			FileReader fr = new FileReader("token.txt");
		    BufferedReader br = new BufferedReader(fr);
		    
		    String access_token=br.readLine();
		    String refresh_token=br.readLine();
		    
		    br.close();
		    fr.close();
			
			api=new BoxAPIConnection(clientId,clientSecret,access_token,refresh_token);
			PrintWriter pw=new PrintWriter("token.txt", "UTF-8");
			
			String access_token_neu=api.getAccessToken();
			String refresh_token_neu=api.getRefreshToken();
			
			
			
			pw.println(access_token_neu);
			pw.println(refresh_token_neu);
			pw.close();
			
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
				
				Blob blob=new Blob();
				blob.setData(data);
				blob.setLocation("box");
				return blob;
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
	
	

}
