package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

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
    protected int id;
    protected String name;
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

        statusZarizeni = in.readString();
        uliceCP = in.readString();
        typPodniku = in.readString();
        typLokality = in.readString();
        cena = in.readString();
        hodnoceni = in.readString();

        createdByUser = in.readString();
        uvodniKoment = in.readString();

        cupTypes = in.readString();
        coffeeSorts = in.readString();
        otherOffers = in.readString();
        nextToMachineTypes = in.readString();

        oteviraciDobaDny = in.readString();
        oteviraciDobaHod = in.readString();

        if (in.dataAvail() > 0) {
            comments = new ArrayList<>();
            comments = in.readArrayList(Comment.class.getClassLoader());
        }
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

        dest.writeString(statusZarizeni);
        dest.writeString(uliceCP);
        dest.writeString(typPodniku);
        dest.writeString(typLokality);
        dest.writeString(cena);
        dest.writeString(hodnoceni);

        dest.writeString(createdByUser);
        dest.writeString(uvodniKoment);

        dest.writeString(cupTypes);
        dest.writeString(coffeeSorts);
        dest.writeString(otherOffers);
        dest.writeString(nextToMachineTypes);

        dest.writeString(oteviraciDobaDny);
        dest.writeString(oteviraciDobaHod);

        dest.writeList(comments);
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

    protected Date createdOn;

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        this.createdOnString = format.format(this.createdOn);
    }

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


    protected double latitude;
    protected double longitude;

    /**
     * URL of the main image of this CoffeeSite
     */
    protected String mainImageURL = ""; // default empty, means image not available

    protected String statusZarizeni;
    protected String uliceCP;
    protected String typPodniku;
    protected String typLokality;
    protected String cena;
    protected String hodnoceni;

    protected String createdByUser;
    protected String uvodniKoment;

    protected String cupTypes;
    protected String coffeeSorts;
    protected String otherOffers;
    protected String nextToMachineTypes;

    protected String oteviraciDobaDny;
    protected String oteviraciDobaHod;

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

    public String getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
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

    public String getCupTypes() {
        return cupTypes;
    }

    public void setCupTypes(String cupTypes) {
        this.cupTypes = cupTypes;
    }

    public String getStatusZarizeni() {
        return statusZarizeni;
    }

    public void setStatusZarizeni(String statusZarizeni) {
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

    public String getNextToMachineTypes() {
        return nextToMachineTypes;
    }

    public void setNextToMachineTypes(String nextToMachineTypes) {
        this.nextToMachineTypes = nextToMachineTypes;
    }

    public String getUliceCP() {
        return uliceCP;
    }

    public void setUliceCP(String uliceCP) {
        this.uliceCP = uliceCP;
    }

    public String getTypPodniku() {
        return typPodniku;
    }

    public void setTypPodniku(String typPodniku) {
        this.typPodniku = typPodniku;
    }

    public String getTypLokality() {
        return typLokality;
    }

    public void setTypLokality(String typLokality) {
        this.typLokality = typLokality;
    }

    public String getCena() {
        return cena;
    }

    public void setCena(String cena) {
        this.cena = cena;
    }

    public String getCoffeeSorts() {
        return coffeeSorts;
    }

    public void setCoffeeSorts(String coffeeSorts) {
        this.coffeeSorts = coffeeSorts;
    }

    public String getOtherOffers() {
        return otherOffers;
    }

    public void setOtherOffers(String otherOffers) {
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

}
