package swarm.domain.location.model;

public enum StorageType {
    TOTE("토트 바구니"),
    PALLET("파레트"),
    ;
    private final String description;

    StorageType(final String description) {
        this.description = description;
    }
}
