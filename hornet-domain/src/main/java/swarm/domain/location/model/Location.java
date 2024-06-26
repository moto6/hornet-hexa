package swarm.domain.location.model;


import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.util.Assert;

@Getter
@Entity
@Table(name = "location")
@Comment("로케이션")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_no")
    @Comment("로케이션 번호")
    private Long locationNo;
    @Column(name = "location_barcode", nullable = false)
    @Comment("로케이션 바코드")
    private String locationBarcode;
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false)
    @Comment("보관 타입")
    private StorageType storageType;
    @Enumerated(EnumType.STRING)
    @Column(name = "usage_purpose", nullable = false)
    @Comment("보관 목적")
    private UsagePurpose usagePurpose;
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inventory> inventories = new ArrayList<>();

    @VisibleForTesting
    Location(
        final String locationBarcode,
        final StorageType storageType,
        final UsagePurpose usagePurpose,
        final List<Inventory> inventories) {
        this(locationBarcode, storageType, usagePurpose);
        this.inventories = inventories;
    }

    public Location(
        final String locationBarcode,
        final StorageType storageType,
        final UsagePurpose usagePurpose) {
        validateConstructor(locationBarcode, storageType, usagePurpose);
        this.locationBarcode = locationBarcode;
        this.storageType = storageType;
        this.usagePurpose = usagePurpose;
    }

    private void validateConstructor(
        final String locationBarcode,
        final StorageType storageType,
        final UsagePurpose usagePurpose) {
        Assert.hasText(locationBarcode, "로케이션 바코드는 필수입니다.");
        Assert.notNull(storageType, "보관 타입은 필수입니다.");
        Assert.notNull(usagePurpose, "보관 목적은 필수입니다.");
    }

    public void assignInventory(final LPN lpn) {
        Assert.notNull(lpn, "LPN은 필수입니다.");
        findInventoryBy(lpn)
            .ifPresentOrElse(
                Inventory::increaseQuantity,
                () -> assignNewLPN(lpn));
    }

    private Optional<Inventory> findInventoryBy(final LPN lpn) {
        return inventories.stream()
            .filter(inventory -> inventory.matchLpnToLocation(lpn))
            .findFirst();
    }

    private boolean assignNewLPN(final LPN lpn) {
        return inventories.add(new Inventory(this, lpn));
    }

    public boolean isTote() {
        return StorageType.TOTE == storageType;
    }

    public boolean hasAvailableInventory() {
        return !inventories.isEmpty() &&
            inventories.stream()
                .anyMatch(Inventory::hasAvailableQuantity);
    }
}
