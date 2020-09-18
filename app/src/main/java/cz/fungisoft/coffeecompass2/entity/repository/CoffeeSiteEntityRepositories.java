package cz.fungisoft.coffeecompass2.entity.repository;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.CupType;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;

/**
 * Class to hold instances of CoffeeSite entities repositories, whose content is read from server
 * and saved into DB.
 * Can return instances of any such type.<br></>
 */
public class CoffeeSiteEntityRepositories {

    /**
     *  Array of all CoffeeSiteEntity Classes to be loaded from server to repository
     *  needed to correct function of creating/updating CoffeeSite instances
     */
    public static final Class<? extends CoffeeSiteEntity>[] COFFEE_SITE_ENTITY_CLASSES
            = new Class[] { CoffeeSiteRecordStatus.class, CoffeeSiteStatus.class, CoffeeSiteType.class,
            CoffeeSort.class, CupType.class, NextToMachineType.class, OtherOffer.class, PriceRange.class,
            SiteLocationType.class, StarsQualityDescription.class};

    public static boolean isDataReadFromServer() {
        return dataReadedFromServer;
    }

    public void setDataReadedFromServer(boolean dataReadedFromServer) {
        this.dataReadedFromServer = dataReadedFromServer;
    }

    // Indication that data are available in the repository i.e. where readed from server
    private static boolean dataReadedFromServer = false;

    public AverageStarsWithNumOfRatingsRepository getAverageStarsWithNumOfHodnoceniRepository() {
        return averageStarsWithNumOfHodnoceniRepository;
    }

    public CoffeeSiteTypeRepository getCoffeeSiteTypesRepository() {
        return coffeeSiteTypesRepository;
    }

    public CoffeeSiteRepository getCoffeeSiteRepository() {
        return coffeeSiteRepository;
    }

    public CoffeeSiteRecordStatusRepository getCoffeeSiteRecordStatusRepository() {
        return coffeeSiteRecordStatusRepository;
    }

    public CoffeeSiteStatusRepository getCoffeeSiteStatusRepository() {
        return coffeeSiteStatusRepository;
    }

    public CoffeeSortRepository getCoffeeSortRepository() {
        return coffeeSortRepository;
    }

    public CommentRepository getCommentRepository() {
        return commentRepository;
    }

    public CupTypeRepository getCupTypeRepository() {
        return cupTypeRepository;
    }

    public NextToMachineTypeRepository getNextToMachineTypeRepository() {
        return nextToMachineTypeRepository;
    }

    public OtherOfferRepository getOtherOfferRepository() {
        return otherOfferRepository;
    }

    public PriceRangeRepository getPriceRangeRepository() {
        return priceRangeRepository;
    }

    public SiteLocationTypeRepository getSiteLocationTypeRepository() {
        return siteLocationTypeRepository;
    }

    public StarsQualityDescriptionRepository getStarsQualityDescriptionRepository() {
        return starsQualityDescriptionRepository;
    }

    /* REPOSITORIES */
    private AverageStarsWithNumOfRatingsRepository averageStarsWithNumOfHodnoceniRepository;
    private CoffeeSiteTypeRepository coffeeSiteTypesRepository;
    private CoffeeSiteRepository coffeeSiteRepository;
    private CoffeeSiteRecordStatusRepository coffeeSiteRecordStatusRepository;
    private CoffeeSiteStatusRepository coffeeSiteStatusRepository;

    private CoffeeSortRepository coffeeSortRepository;
    private CommentRepository commentRepository;
    private CupTypeRepository cupTypeRepository;
    private NextToMachineTypeRepository nextToMachineTypeRepository;
    private OtherOfferRepository otherOfferRepository;
    private PriceRangeRepository priceRangeRepository;
    private SiteLocationTypeRepository siteLocationTypeRepository;
    private StarsQualityDescriptionRepository starsQualityDescriptionRepository;
    /* REPOSITORIES */

    private static CoffeeSiteEntityRepositories instance;

    private CoffeeSiteEntityRepositories(final CoffeeSiteDatabase db) {
        coffeeSiteTypesRepository = new CoffeeSiteTypeRepository(db);
        coffeeSiteRepository = new CoffeeSiteRepository(db);
        averageStarsWithNumOfHodnoceniRepository = new AverageStarsWithNumOfRatingsRepository(db);
        coffeeSiteRepository = new CoffeeSiteRepository(db);
        coffeeSiteRecordStatusRepository = new CoffeeSiteRecordStatusRepository(db);
        coffeeSiteStatusRepository = new CoffeeSiteStatusRepository(db);
        coffeeSortRepository = new CoffeeSortRepository(db);
        commentRepository = new CommentRepository(db);
        cupTypeRepository = new CupTypeRepository(db);
        nextToMachineTypeRepository = new NextToMachineTypeRepository(db);
        otherOfferRepository = new OtherOfferRepository(db);
        priceRangeRepository = new PriceRangeRepository(db);
        siteLocationTypeRepository = new SiteLocationTypeRepository(db);
        starsQualityDescriptionRepository = new StarsQualityDescriptionRepository(db);
    }

    public static CoffeeSiteEntityRepositories getInstance(final CoffeeSiteDatabase db) {
        if (instance == null) {

            instance = new CoffeeSiteEntityRepositories(db);
        }
        return instance;
    }


    /** SETTERS **/

    public void setAllCoffeeSiteTypes(List<CoffeeSiteType> allCoffeeSiteTypes) {
        coffeeSiteTypesRepository.insertAll(allCoffeeSiteTypes);
    }

    public void setAllCoffeeSiteRecordStatuses(List<CoffeeSiteRecordStatus> allCoffeeSiteRecordStatuses) {
        coffeeSiteRecordStatusRepository.insertAll(allCoffeeSiteRecordStatuses);
    }

    public void setAllCoffeeSiteStatuses(List<CoffeeSiteStatus> allCoffeeSiteStatuses) {
        coffeeSiteStatusRepository.insertAll(allCoffeeSiteStatuses);
    }

    public void setAllCoffeeSorts(List<CoffeeSort> allCoffeeSorts) {
        coffeeSortRepository.insertAll(allCoffeeSorts);
    }

    public void setAllCupTypes(List<CupType> allCupTypes) {
        cupTypeRepository.insertAll(allCupTypes);
    }

    public void setAllNextToMachineTypes(List<NextToMachineType> allNextToMachineTypes) {
        nextToMachineTypeRepository.insertAll(allNextToMachineTypes);
    }

    public void setAllOtherOffers(List<OtherOffer> allOtherOffers) {
        otherOfferRepository.insertAll(allOtherOffers);
    }

    public void setAllPriceRanges(List<PriceRange> allPriceRanges) {
        priceRangeRepository.insertAll(allPriceRanges);
    }

    public void setAllSiteLocationTypes(List<SiteLocationType> allSiteLocationTypes) {
        siteLocationTypeRepository.insertAll(allSiteLocationTypes);
    }

    public void setAllStarsQualityDescriptions(List<StarsQualityDescription> allStarsQualityDescriptions) {
        for (StarsQualityDescription stars : allStarsQualityDescriptions) {
            stars.setId(stars.getNumOfStars());
        }
        starsQualityDescriptionRepository.insertAll(allStarsQualityDescriptions);
    }


    public <T extends List<? extends CoffeeSiteEntity>> void setEntities(T response) {
        if (response.size() > 0) {
            if (response.get(0) instanceof CoffeeSiteType) {
                setAllCoffeeSiteTypes((List<CoffeeSiteType>) response);
            }
            if (response.get(0) instanceof CoffeeSiteRecordStatus) {
                setAllCoffeeSiteRecordStatuses((List<CoffeeSiteRecordStatus>) response);
            }
            if (response.get(0) instanceof CoffeeSiteStatus) {
                setAllCoffeeSiteStatuses((List<CoffeeSiteStatus>) response);
            }
            if (response.get(0) instanceof CoffeeSort) {
                setAllCoffeeSorts((List<CoffeeSort>) response);
            }
            if (response.get(0) instanceof CupType) {
                setAllCupTypes((List<CupType>) response);
            }
            if (response.get(0) instanceof NextToMachineType) {
                setAllNextToMachineTypes((List<NextToMachineType>) response);
            }
            if (response.get(0) instanceof OtherOffer) {
                setAllOtherOffers((List<OtherOffer>) response);
            }
            if (response.get(0) instanceof PriceRange) {
                setAllPriceRanges((List<PriceRange>) response);
            }
            if (response.get(0) instanceof SiteLocationType) {
                setAllSiteLocationTypes((List<SiteLocationType>) response);
            }
            if (response.get(0) instanceof StarsQualityDescription) {
                setAllStarsQualityDescriptions((List<StarsQualityDescription>) response);
            }
        }
    }

}
