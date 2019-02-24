package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * A CoffeeSite
 */
public class CoffeeSite implements Serializable
{
    public int id;
    public String name;
    public long distance;

    private double latitude;
    private double longitude;

    private boolean imageAvailable = false;

    private String statusZarizeni;
    private String uliceCP;
    private String typPodniku;
    private String typLokality;
    private String cena;
    private String hodnoceni;

    private String createdByUser;
    private String uvodniKoment;

    private String cupTypes;
    private String coffeeSorts;
    private String otherOffers;
    private String nextToMachineTypes;

    private String oteviraciDobaDny;
    private String oteviraciDobaHod;

    private List<Comment> comments;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public boolean isImageAvailable() {
        return imageAvailable;
    }

    public void setImageAvailable(boolean imageAvailable) {
        this.imageAvailable = imageAvailable;
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

    public CoffeeSite(int id, String name, long dist) {
        this.id = id;
        this.name = name;
        this.distance = dist;
    }

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

}