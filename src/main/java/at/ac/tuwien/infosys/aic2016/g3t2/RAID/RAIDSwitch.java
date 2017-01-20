package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.UserinteractionRequiredException;

@Service
public class RAIDSwitch implements IRAID {

    @Autowired
    protected Map<String, IRawRAID> raidMap;
    
    protected final static RAIDType DEFAULT_RAID_TYPE = RAIDType.RAID5;
    
    public boolean create(String storagefilename, byte[] data, RAIDType storage) {
        if (storage == null)
            storage = DEFAULT_RAID_TYPE;
        final IRAID raid = raidMap.get(storage.toString());
        return raid.create(storagefilename, data);
    }
    
    @Override
    public boolean create(String storagefilename, byte[] data) {
        return create(storagefilename, data, null);
    }

    @Override
    public boolean delete(String storagefilename) throws ItemMissingException {
        for (IRAID raid : raidMap.values()) {
            try {
                return raid.delete(storagefilename);
            } catch (ItemMissingException e) {
            }
        }
        throw new ItemMissingException();
    }

    @Override
    public File read(String storagefilename) throws ItemMissingException, UserinteractionRequiredException {
        for (IRAID raid : raidMap.values()) {
            try {
                return raid.read(storagefilename);
            } catch (ItemMissingException e) {
            }
        }
        throw new ItemMissingException();
    }

    @Override
    public List<String> listFiles() {
        return raidMap.values()
            .stream()
            .flatMap(r -> r.listFiles().stream())
            .distinct()
            .collect(Collectors.toList());
    }

}
