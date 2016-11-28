package at.ac.tuwien.infosys.aic2016.g3t2.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import at.ac.tuwien.infosys.aic2016.g3t2.RAID.File;
import at.ac.tuwien.infosys.aic2016.g3t2.RAID.RAID1;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;

@Controller
public class RestController {
	
	@Autowired
	private RAID1 storage;
	
	@GetMapping("/listFiles")
    public @ResponseBody List<String> listFiles() {
        return storage.listFiles();
    }
	
	@GetMapping("/read/{filename}")
	public @ResponseBody byte[] read(@PathVariable String filename) throws ItemMissingException {
		final File file = storage.read(filename);
		if (file == null)
			throw new ItemMissingException();
		return file.getData();
			
	}
	
	@GetMapping("/readWithMeta/{filename}")
	public @ResponseBody File readWithMeta(@PathVariable String filename, boolean onlyMeta) throws ItemMissingException {
		final File file = storage.read(filename);
		if (file == null)
			throw new ItemMissingException();
		if (onlyMeta)
			return File.removeMeta(file);
		return file;
	}
	
	@PutMapping("/create/{filename}")
    public @ResponseBody boolean create(@PathVariable String filename, @RequestBody byte[] data) {
		return storage.create(filename, data);
	}
	
	@DeleteMapping("/delete/{filename}")
    public @ResponseBody boolean delete(@PathVariable String filename) throws ItemMissingException {
		return storage.delete(filename);
	}

}
