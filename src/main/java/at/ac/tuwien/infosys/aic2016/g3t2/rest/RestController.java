package at.ac.tuwien.infosys.aic2016.g3t2.rest;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;
import at.ac.tuwien.infosys.aic2016.g3t2.RAID.File;
import at.ac.tuwien.infosys.aic2016.g3t2.RAID.FileMetadata;
import at.ac.tuwien.infosys.aic2016.g3t2.RAID.RAIDType;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.UserinteractionRequiredException;
import at.ac.tuwien.infosys.aic2016.g3t2.version.IVersionManager;

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

    protected File getFile(String filename, Integer v) throws ItemMissingException, UserinteractionRequiredException {
        if (v != null)
            return versionManager.read(filename, v);
        else
            return versionManager.read(filename);
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
     * @param v
     *            the version of the file to get
     * @param response
     *            the HttpServletResponse response to set the headers       
     * @return the raw file contents
     * @throws ItemMissingException
     *             if the file was not found
     * @throws UserinteractionRequiredException
     *             if there is an error which cannot be handled automatically
     */
    @GetMapping("/file/{filename:.+}")
    public @ResponseBody byte[] read(@PathVariable String filename,
            @RequestParam(required = false) Integer v, HttpServletResponse response)
            throws ItemMissingException, UserinteractionRequiredException {
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        try {
            return getFile(filename, v).getData();
        } catch (UserinteractionRequiredException e) {
            response.setHeader("Content-Disposition", "");
            response.setStatus(500);
            response.setContentType("application/html");
            String message = e.getMessage();
            if (message == null) {
                message = "Unknown error occured. User interaction required. Check server log for details.";
            }
            message = "User interaction required: " + message;
            return message.getBytes();
        } catch (ItemMissingException e) {
            response.setHeader("Content-Disposition", "");
            response.setContentType("application/html");
            response.setStatus(404);
            return "File not found".getBytes();
        }
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
     * @param v
     *            the version of the file to get
     * @return a list of {@link Location}s
     * @throws ItemMissingException
     *             if the file was not found
     * @throws UserinteractionRequiredException
     *             if there is an error which cannot be handled automatically
     */
    @GetMapping("/file/{filename:.+}/metadata")
    public @ResponseBody FileMetadata getMetadata(@PathVariable String filename,
            @RequestParam(required = false) Integer v)
            throws ItemMissingException, UserinteractionRequiredException {
        return getFile(filename, v).getMetadata();
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
     * @param raid
     *            the raid type to use (e.g. RAID1, RAID5), defaults to RAID5
     * @return true if successful
     */
    @PutMapping("/file/{filename:.+}")
    public @ResponseBody boolean create(@PathVariable String filename, @RequestBody byte[] data,
            @RequestParam(required = false) String raid) {
        return versionManager.create(filename, data, raid == null ? null : RAIDType.valueOf(raid));
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
