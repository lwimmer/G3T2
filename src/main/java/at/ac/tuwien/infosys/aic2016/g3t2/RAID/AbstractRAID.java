package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.IBlobstore;

public abstract class AbstractRAID implements IRawRAID {

    protected final Collection<IBlobstore> blobstores;
    protected final RAIDType raidType;
    protected final String prefix;
    protected final String prefixRegex;
    
    public AbstractRAID(int minimumBlobstores, RAIDType raidType, Map<String, IBlobstore> blobstoresMap,
            List<String> disabledBlobstores) {
        this(minimumBlobstores, raidType, removeDisabledBlobstores(blobstoresMap, disabledBlobstores));
    }
    
    public static Collection<IBlobstore> removeDisabledBlobstores(Map<String, IBlobstore> blobstoresMap, List<String> disabledBlobstores) {
        if (disabledBlobstores != null)
            for (String disabled : disabledBlobstores)
                blobstoresMap.remove(disabled);
        return blobstoresMap.values();
    }

    public AbstractRAID(int minimumBlobstores, RAIDType raidType, Collection<IBlobstore> blobstores) {
        if (blobstores.size() < minimumBlobstores)
            throw new IllegalArgumentException("RAID needs at least " + minimumBlobstores + " blobstores!");
        this.raidType = raidType;
        this.prefix = raidType.getPrefix();
        this.prefixRegex = "^" + prefix;
        this.blobstores = blobstores;
    }
    
    public AbstractRAID(int minimumBlobstores, RAIDType raidType, IBlobstore... blobstoresArray) {
        this(minimumBlobstores, raidType, Arrays.asList(blobstoresArray));
    }

    @Override
    public List<String> listFiles() {
        return blobstores
            .parallelStream()
            .flatMap(bs -> bs.listBlobs().stream())
            .filter(n -> prefix == null ? true : n.startsWith(prefix))
            .map(n -> stripPrefix(n))
            .distinct()
            .collect(Collectors.toList());
    }
    
    protected String stripPrefix(String name) {
        if (prefix == null)
            return name;
        return name.replaceFirst(prefixRegex, "");
    }
    
    protected String addPrefix(String name) {
        if (prefix == null)
            return name;
        return prefix.concat(name);
    }
    
}
