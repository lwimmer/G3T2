package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

public enum RAIDType {
    RAID1("r1_"), RAID5("r5_");

    private final String prefix;
    
    private RAIDType(String prefix) {
        this.prefix = prefix;
    }
    
    public String getPrefix() {
        return prefix;
    }
}
