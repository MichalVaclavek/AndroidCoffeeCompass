package cz.fungisoft.coffeecompass2.activity.data.model.rest.comments;

/**
 * Class for holding Comment and Stars for CoffeeSite entered by user
 * to be saved on server via REST call.
 */
public class CommentAndStarsToSave {

    private Stars stars;

    private String comment;

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }
    public CommentAndStarsToSave() {
        stars = new Stars();
        comment = "";
    }

    public CommentAndStarsToSave(Stars stars, String comment) {
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

        private int numOfStars = 3;

        public Stars() {}

        public Stars(int numOfStars) {
            this.numOfStars = numOfStars;
        }

        public int getNumOfStars() {
            return numOfStars;
        }

        public void setNumOfStars(int numOfStars) {
            this.numOfStars = numOfStars;
        }
    }

}
