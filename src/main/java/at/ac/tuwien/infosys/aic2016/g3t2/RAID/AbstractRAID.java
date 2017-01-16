package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.IBlobstore;

public abstract class AbstractRAID implements IRAID {

    protected final Collection<IBlobstore> blobstores;
    
    @Autowired
    public AbstractRAID(int minimumBlobstores, Map<String, IBlobstore> blobstoresMap,
            @Value("#{'${disabled_blobstores:}'.split(',')}") List<String> disabledBlobstores) {
        this(minimumBlobstores, removeDisabledBlobstores(blobstoresMap, disabledBlobstores));
    }
    
    public static Collection<IBlobstore> removeDisabledBlobstores(Map<String, IBlobstore> blobstoresMap, List<String> disabledBlobstores) {
        if (disabledBlobstores != null)
            for (String disabled : disabledBlobstores)
                blobstoresMap.remove(disabled);
        return blobstoresMap.values();
    }

    public AbstractRAID(int minimumBlobstores, Collection<IBlobstore> blobstores) {
        if (blobstores.size() < minimumBlobstores)
            throw new IllegalArgumentException("RAID needs at least " + minimumBlobstores + " blobstores!");
        this.blobstores = blobstores;
    }
    
    public AbstractRAID(int minimumBlobstores, IBlobstore... blobstoresArray) {
        this(minimumBlobstores, Arrays.asList(blobstoresArray));
    }

}
