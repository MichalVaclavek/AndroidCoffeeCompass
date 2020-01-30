package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * A CoffeeSite, main app. entity
 */
public class CoffeeSite implements Serializable, Comparable<CoffeeSite>, Parcelable
{
    @Expose
    @SerializedName("id")
    protected int id;

    @Expose
    @SerializedName("siteName")
    protected String name;

    @Expose(serialize = false)
    @SerializedName("distFromSearchPoint")
    protected long distance;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDistance() {
        return distance;
    }

    /* Parcelable implementation -- START -- */

    protected CoffeeSite(Parcel in) {
        id = in.readInt();
        name = in.readString();
        distance = in.readLong();

        createdOn = (java.util.Date) in.readSerializable();
        createdOnString = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();

        mainImageURL = in.readString();

        statusZarizeni = in.readParcelable(CoffeeSiteStatus.class.getClassLoader());
        typPodniku = in.readParcelable(CoffeeSiteType.class.getClassLoader());
        typLokality = in.readParcelable(SiteLocationType.class.getClassLoader());
        cena = in.readParcelable(PriceRange.class.getClassLoader());

        mesto = in.readString();
        uliceCP = in.readString();
        hodnoceni = in.readString();

        createdByUserName = in.readString();
        lastEditUserName = in.readString();
        uvodniKoment = in.readString();

        cupTypes = new ArrayList<>();
        in.readTypedList(cupTypes, CupType.CREATOR);

        coffeeSorts = new ArrayList<>();
        in.readTypedList(coffeeSorts, CoffeeSort.CREATOR);

        otherOffers = new ArrayList<>();
        in.readTypedList(otherOffers, OtherOffer.CREATOR);

        nextToMachineTypes = new ArrayList<>();
        in.readTypedList(nextToMachineTypes, NextToMachineType.CREATOR);

        oteviraciDobaDny = in.readString();
        oteviraciDobaHod = in.readString();

        if (in.dataAvail() > 0) {
            comments = new ArrayList<>();
            comments = in.readArrayList(Comment.class.getClassLoader());
        }

        canBeModified = in.readByte() != 0;
        canBeActivated  = in.readByte() != 0;
        canBeDeactivated = in.readByte() != 0;
        canBeCanceled = in.readByte() != 0;
        canBeCommented = in.readByte() != 0;
        canBeDeleted = in.readByte() != 0;
        canBeRatedByStars = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeLong(distance);

        dest.writeSerializable(createdOn);
        dest.writeString(createdOnString);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);

        dest.writeString(mainImageURL);

        dest.writeParcelable(statusZarizeni, flags);
        dest.writeParcelable(typPodniku, flags);
        dest.writeParcelable(typLokality, flags);
        dest.writeParcelable(cena, flags);

        dest.writeString(mesto);
        dest.writeString(uliceCP);
        dest.writeString(hodnoceni);

        dest.writeString(createdByUserName);
        dest.writeString(lastEditUserName);
        dest.writeString(uvodniKoment);

        dest.writeTypedList(cupTypes);
        dest.writeTypedList(coffeeSorts);
        dest.writeTypedList(otherOffers);
        dest.writeTypedList(nextToMachineTypes);

        dest.writeString(oteviraciDobaDny);
        dest.writeString(oteviraciDobaHod);

        dest.writeList(comments);

        dest.writeByte((byte) (canBeModified ? 1 : 0));
        dest.writeByte((byte) (canBeActivated  ? 1 : 0));
        dest.writeByte((byte) (canBeDeactivated ? 1 : 0));
        dest.writeByte((byte) (canBeCanceled ? 1 : 0));
        dest.writeByte((byte) (canBeCommented ? 1 : 0));
        dest.writeByte((byte) (canBeDeleted ? 1 : 0));
        dest.writeByte((byte) (canBeRatedByStars ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<CoffeeSite> CREATOR = new Creator<CoffeeSite>() {
        @Override
        public CoffeeSite createFromParcel(Parcel in) {
            return new CoffeeSite(in);
        }

        @Override
        public CoffeeSite[] newArray(int size) {
            return new CoffeeSite[size];
        }
    };

    /* Parcelable implementation -- END -- */

    public void setDistance(long distance) {
        this.distance = distance;
    }

    private final SimpleDateFormat format = new SimpleDateFormat("dd.MM. yyyy, HH:mm");

    //@SerializedName("createdOn")
    @Expose
    @SerializedName("createdOn")
    protected Date createdOn;

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        this.createdOnString = format.format(this.createdOn);
    }

    //@Expose(serialize = false)
    //@SerializedName("createdOn")
    protected String createdOnString;

    public String getCreatedOnString() {
        return createdOnString;
    }

    public void setCreatedOnString(String createdOnString) {
        this.createdOnString = createdOnString;
        Date created;

        try {
            created  = format.parse ( this.createdOnString);
        } catch (ParseException e) {
            created = new Date();
        }

        this.createdOn = created;
    }

    @Expose
    @SerializedName("numOfCoffeeAutomatyVedleSebe")
    protected int numOfCoffeeAutomatyVedleSebe = 0;

    @Expose
    @SerializedName("zemSirka")
    protected double latitude;
    @Expose
    @SerializedName("zemDelka")
    protected double longitude;

    /**
     * URL of the main image of this CoffeeSite
     */
    protected String mainImageURL = ""; // default empty, means image not available

    //protected String statusZarizeni;
    @Expose
    @SerializedName("statusZarizeni")
    protected CoffeeSiteStatus statusZarizeni;

    @Expose
    @SerializedName("uliceCP")
    protected String uliceCP;

    @Expose
    @SerializedName("mesto")
    protected String mesto;

    @Expose
    @SerializedName("typPodniku")
    protected CoffeeSiteType typPodniku;

    @Expose
    @SerializedName("typLokality")
    protected SiteLocationType typLokality;

    @Expose
    @SerializedName("cena")
    protected PriceRange cena;

    protected String hodnoceni;

    @Expose
    @SerializedName("originalUserName")
    protected String createdByUserName;

    @Expose
    @SerializedName("lastEditUserName")
    protected String lastEditUserName;

    @Expose
    @SerializedName("initialComment")
    protected String uvodniKoment;

    @Expose
    @SerializedName("cupTypes")
    protected List<CupType> cupTypes;

    @Expose
    @SerializedName("coffeeSorts")
    protected List<CoffeeSort> coffeeSorts;

    @Expose
    @SerializedName("otherOffers")
    protected List<OtherOffer> otherOffers;

    @Expose
    @SerializedName("nextToMachineTypes")
    protected List<NextToMachineType> nextToMachineTypes;

    @Expose
    @SerializedName("pristupnostDny")
    protected String oteviraciDobaDny;

    @Expose
    @SerializedName("pristupnostHod")
    protected String oteviraciDobaHod;

    /** Properties of CoffeeSiteDTO which describes allowed operations **/
    /** with CoffeeSite **/

    @Expose(serialize = false)
    @SerializedName("canBeModified")
    protected boolean canBeModified = false;
    @Expose(serialize = false)
    @SerializedName("canBeActivated")
    protected boolean canBeActivated = false;
    @Expose(serialize = false)
    @SerializedName("canBeDeactivated")
    protected boolean canBeDeactivated = false;
    @Expose(serialize = false)
    @SerializedName("canBeCanceled")
    protected boolean canBeCanceled = false;
    @Expose(serialize = false)
    @SerializedName("canBeDeleted")
    protected boolean canBeDeleted = false;
    @Expose(serialize = false)
    @SerializedName("canBeCommented")
    protected boolean canBeCommented = false;
    @Expose(serialize = false)
    @SerializedName("canBeRatedByStars")
    protected boolean canBeRatedByStars = false;

    //@Expose(serialize = false , deserialize = false)
    protected List<Comment> comments;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void clearComments() {
        this.comments.clear();
    }

    public String getMainImageURL() {
        return mainImageURL;
    }

    public void setMainImageURL(String mainImageURL) {
        this.mainImageURL = mainImageURL;
    }

    public String getCreatedByUserName() {
        return createdByUserName;
    }

    public void setCreatedByUserName(String createdByUserName) {
        this.createdByUserName = createdByUserName;
    }

    public String getLastEditUserName() {
        return lastEditUserName;
    }

    public void setLastEditUserName(String lastEditUserName) {
        this.lastEditUserName = lastEditUserName;
    }

    public String getHodnoceni() {
        return hodnoceni;
    }

    public void setHodnoceni(String hodnoceni) {
        this.hodnoceni = hodnoceni;
    }

    public String getUvodniKoment() {
        return uvodniKoment;
    }

    public void setUvodniKoment(String uvodniKoment) {
        this.uvodniKoment = uvodniKoment;
    }

    /* Getters and Setrs */

    public List<CupType> getCupTypes() {
        return cupTypes;
    }

    public String getCupTypesOneString() {
        return convertListToOneStringWithDelimiter(this.cupTypes, ',');
    }

    public void setCupTypes(List<CupType> cupTypes) {
        this.cupTypes = cupTypes;
    }

    public CoffeeSiteStatus getStatusZarizeni() {
        return statusZarizeni;
    }

    public void setStatusZarizeni(CoffeeSiteStatus statusZarizeni) {
        this.statusZarizeni = statusZarizeni;
    }

    public String getOteviraciDobaDny() {
        return oteviraciDobaDny;
    }

    public void setOteviraciDobaDny(String oteviraciDobaDny) {
        this.oteviraciDobaDny = oteviraciDobaDny;
    }

    public String getOteviraciDobaHod() {
        return oteviraciDobaHod;
    }

    public void setOteviraciDobaHod(String oteviraciDobaHod) {
        this.oteviraciDobaHod = oteviraciDobaHod;
    }

    public List<NextToMachineType> getNextToMachineTypes() {
        return nextToMachineTypes;
    }

    public String getNextToMachineTypesOneString() {
        return convertListToOneStringWithDelimiter(this.nextToMachineTypes, ',');
    }

    public void setNextToMachineTypes(List<NextToMachineType> nextToMachineTypes) {
        this.nextToMachineTypes = nextToMachineTypes;
    }

    public String getUliceCP() {
        return uliceCP;
    }

    public void setUliceCP(String uliceCP) {
        this.uliceCP = uliceCP;
    }

    public String getMesto() {
        return mesto;
    }

    public void setMesto(String mesto) {
        this.mesto = mesto;
    }

    public CoffeeSiteType getTypPodniku() {
        return typPodniku;
    }

    public void setTypPodniku(CoffeeSiteType typPodniku) {
        this.typPodniku = typPodniku;
    }

    public SiteLocationType getTypLokality() {
        return typLokality;
    }

    public void setTypLokality(SiteLocationType typLokality) {
        this.typLokality = typLokality;
    }

    public PriceRange getCena() {
        return cena;
    }

    public void setCena(PriceRange  cena) {
        this.cena = cena;
    }

    public List<CoffeeSort> getCoffeeSorts() {
        return coffeeSorts;
    }

    public String getCoffeeSortsOneString() {
        return convertListToOneStringWithDelimiter(this.coffeeSorts, ',');
    }

    public void setCoffeeSorts(List<CoffeeSort> coffeeSorts) {
        this.coffeeSorts = coffeeSorts;
    }

    public List<OtherOffer> getOtherOffers() {
        return otherOffers;
    }

    public String getOtherOffersOneString() {
        return convertListToOneStringWithDelimiter(this.otherOffers, ',');
    }

    public void setOtherOffers(List<OtherOffer> otherOffers) {
        this.otherOffers = otherOffers;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getNumOfCoffeeAutomatyVedleSebe() {
        return numOfCoffeeAutomatyVedleSebe;
    }

    public void setNumOfCoffeeAutomatyVedleSebe(int numOfCoffeeAutomatyVedleSebe) {
        this.numOfCoffeeAutomatyVedleSebe = numOfCoffeeAutomatyVedleSebe;
    }

    public CoffeeSite() {
    }

    public CoffeeSite(int id, String name, long dist) {
        this.id = id;
        this.name = name;
        this.distance = dist;
    }

    /*
    @Override
    public String toString() {
        StringBuilder details = new StringBuilder();
        details.append("Jméno: " + name + "\n");
        details.append("Vzdálenost: " + distance + " m\n");
        details.append("Druhy kávy: " + coffeeSorts + "\n");
        details.append("Typ zařízení: " + typPodniku + "\n");
        details.append("Typ lokality: " + typLokality + "\n");
        details.append("Ulice: " + uliceCP + "\n");
        details.append("Cenový rozsah: " + cena + "\n");
        details.append("Další nabídka: " + otherOffers + "\n");
        details.append("Další nabídka: " + uvodniKoment + "\n");
        details.append("Další nabídka: " + hodnoceni + "\n");
        details.append("Zem. šířka: " + latitude + "\n");
        details.append("Zem. délka: " + longitude + "\n");

        return details.toString();
    }
    */

    @Override
    public int compareTo(CoffeeSite o) {
        int retVal = Long.compare(this.getDistance(), o.getDistance());
        return retVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoffeeSite that = (CoffeeSite) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Objects of the List has to implement toString()
     *
     * @param list
     * @param delimiter
     * @return
     */
    private String convertListToOneStringWithDelimiter(List<?> list, char delimiter) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toString().trim());
            if (i != list.size() - 1) { // not a last item in the array
                sb.append(delimiter + " ");
            }
        }
        return sb.toString();
    }

}
