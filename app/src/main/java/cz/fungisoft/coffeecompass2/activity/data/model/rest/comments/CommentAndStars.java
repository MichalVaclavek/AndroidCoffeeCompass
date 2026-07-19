package cz.fungisoft.coffeecompass2.activity.data.model.rest.comments;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Class for holding Comment and Stars for CoffeeSite entered by user
 * to be saved on server via REST call.
 */
public class CommentAndStars {

    @Expose
    @SerializedName("stars")
    private Stars stars;

    @Expose
    @SerializedName("comment")
    private String comment;

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }
    public CommentAndStars() {
        stars = new Stars();
        comment = "";
    }

    public CommentAndStars(Stars stars, String comment) {
        this.stars = stars;
        this.comment = comment;
    }

    public Stars getStars() {
        return stars;
    }

    public void setStars(Stars stars) {
        this.stars = stars;
    }


    /**
     * Inner class for holding number of stars. Needed<br>
     * for automatic parsing within Retrofit when reading<br>
     * from server.<br>
     */
    public static class Stars {

        @Expose
        @SerializedName("numOfStars")
        private int numOfStars = 3;

        public Stars() {}

        public Stars(int numOfStars) {
            if (numOfStars > 0 && numOfStars <= 5) {
                this.numOfStars = numOfStars;
            }
        }

        public int getNumOfStars() {
            return numOfStars;
        }

        public void setNumOfStars(int numOfStars) {
            this.numOfStars = numOfStars;
        }
    }

}
