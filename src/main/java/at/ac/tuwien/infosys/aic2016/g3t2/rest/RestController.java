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

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;
import at.ac.tuwien.infosys.aic2016.g3t2.RAID.File;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.UserinteractionRequiredException;
import at.ac.tuwien.infosys.aic2016.g3t2.version.IVersionManager;
import at.ac.tuwien.infosys.aic2016.g3t2.version.Storage;

@Controller
public class RestController {

    @Autowired
    private IVersionManager versionManager;

    /**
     * Lists all files in the storage.
     * 
     * Usage example:
     * 
     * <pre>
     * curl http://localhost:8080/file
     * </pre>
     * 
     * @return list of filenames
     */
    @GetMapping("/file")
    public @ResponseBody List<String> listFiles() {
        return versionManager.listFiles();
    }

    /**
     * Gets the content of a file.
     * 
     * Usage example (substitute "filename"):
     * 
     * <pre>
     * curl http://localhost:8080/file/filename
     * </pre>
     * 
     * @param filename
     *            the name of the file to get
     * @return the raw file contents
     * @throws ItemMissingException
     *             if the file was not found
     */
    @GetMapping("/file/{filename:.+}")
    public @ResponseBody byte[] read(@PathVariable String filename)
            throws ItemMissingException, UserinteractionRequiredException {
        final File file = versionManager.read(filename);
        return file.getData();
    }

    /**
     * Gets the locations of a file.
     * 
     * Usage examples (substitute "filename"):
     * 
     * <pre>
     * curl http://localhost:8080/file/filename/locations
     * </pre>
     * 
     * @param filename
     *            the name of the file to get
     * @return a list of {@link Location}s
     * @throws ItemMissingException
     *             if the file was not found
     */
    @GetMapping("/file/{filename:.+}/locations")
    public @ResponseBody List<Location> readLocations(@PathVariable String filename)
            throws ItemMissingException, UserinteractionRequiredException {
        final File file = versionManager.read(filename);
        return file.getLocations();
    }

    /**
     * Stores a file in the storage.
     * 
     * Usage examples (substitute "filename"):
     * 
     * <pre>
     * curl -T filename http://localhost:8080/file/
     * </pre>
     * 
     * @param filename
     *            the name of the file to store
     * @param data
     *            the contents of the file
     * @return true if successful
     */
    @PutMapping("/file/{filename:.+}")
    public @ResponseBody boolean create(@PathVariable String filename, @RequestBody byte[] data) {
    	//TODO do we need to give raid type as a parameter or will the filename include raid type as prefix?
        return versionManager.create(filename, data, Storage.RAID1);
    }

    /**
     * Deletes a file in the storage.
     * 
     * Usage examples (substitute "filename"):
     * 
     * <pre>
     * curl -X DELETE http://localhost:8080/file/filename
     * </pre>
     * 
     * @param filename
     *            the name of the file to delete
     * @return true if successful
     * @throws ItemMissingException
     *             if the file was not found
     */
    @DeleteMapping("/file/{filename:.+}")
    public @ResponseBody boolean delete(@PathVariable String filename) throws ItemMissingException {
        return versionManager.delete(filename);
    }

}
