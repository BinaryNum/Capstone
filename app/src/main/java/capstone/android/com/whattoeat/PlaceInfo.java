package capstone.android.com.whattoeat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;

public class PlaceInfo {

    private String name;
    private String priceLevel;
    private String rating;
    private ArrayList<String> reviews;

    PlaceInfo(){
        name = "이름정보없음";
        priceLevel = "가격정보없음";
        rating = "0";
        reviews = new ArrayList<>(100);
    }

    public String getName() {
        return name;
    }

    public void setName(JsonElement name) {
        this.name = name.getAsString();
    }

    public String getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(JsonElement priceLevel) {
        String pl = priceLevel.getAsString();
        switch (pl){
            case "0":
                this.priceLevel = "매우 저렴함";
                break;
            case "1":
                this.priceLevel = "저렴함";
                break;
            case "2":
                this.priceLevel = "적정";
                break;
            case "3":
                this.priceLevel = "비쌈";
                break;
            case "4":
                this.priceLevel = "매우 비쌈";
                break;
        }
    }

    public String getRating() {
        return rating;
    }

    public void setRating(JsonElement rating) {
        this.rating = rating.getAsString();
    }

    public ArrayList<String> getReviews() {
        return reviews;
    }

    public void setReviews(JsonElement reviews) {
        JsonArray list = (JsonArray)reviews;
        for(int i=0; i<list.size();i++){
            String review = ((JsonObject)list.get(i)).get("text").getAsString();
            this.reviews.add(review);
        }
    }
}
