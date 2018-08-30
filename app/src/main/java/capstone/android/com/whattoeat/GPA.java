package capstone.android.com.whattoeat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class GPA {

    private final String myKey = "Your_Key";
    private final String responseLanguage = "ko";
    private String userStr;
    private String lat;
    private String lng;
    private String radius;
    private HashMap<String, String> RNList;

    GPA(String userStr, String lat, String lng, String radius){
        this.userStr = userStr;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

    private void NearbySearch(String type) throws Exception {

        JsonParser parser = new JsonParser();
        JsonObject obj;

        URL search = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + lat + "," + lng + "&radius=" + radius + "&rankyby=distance" + "&type=" + type + "&language="
                + responseLanguage + "&key=" + myKey);

        URLConnection urlConn = search.openConnection();
        obj = (JsonObject) parser.parse(new InputStreamReader(urlConn.getInputStream()));
        JsonArray results = (JsonArray) obj.get("results");

        for (int i = 0; i < results.size(); i++) {
            RNList.put(results.get(i).getAsJsonObject().get("name").getAsString(),
                    results.get(i).getAsJsonObject().get("place_id").getAsString());
        }
    }

    public PlaceInfo DetailSearch(String id) throws Exception{

        if(id==null){
            String name = SelectName();
            id = RNList.get(name);
        }

        JsonParser parser = new JsonParser();
        JsonObject obj;
        PlaceInfo info = new PlaceInfo();

        URL search = new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid="
                + id + "&key=" + myKey);
        URLConnection urlConn = search.openConnection();
        obj = (JsonObject) parser.parse(new InputStreamReader(urlConn.getInputStream()));
        JsonObject result = (JsonObject)obj.get("result");
        if(result != null){
            if(result.get("name")!=null)
                info.setName(result.get("name"));
            if(result.get("price_level")!=null)
                info.setPriceLevel(result.get("price_level"));
            if(result.get("rating")!=null)
                info.setRating(result.get("rating"));
            if(result.get("reviews")!=null)
                info.setReviews(result.get("reviews"));
        }

        return info;
    }

    private void setRNList() throws Exception {
        RNList = new HashMap<>();
        NearbySearch("restaurant");
        NearbySearch("cafe");
        NearbySearch("bakery");
        NearbySearch("bar");
    }

    private String RemoveSpace(String s){
        //s = s.replaceAll("[^\uAC00-\uD7AF\u1100-\u11FF\u3130-\u318F]", ""); //only 한글
        s = s.replaceAll(" ", "");
        return s;
    }

    private ArrayList<Character> DivideHangul(String tempStr){
        tempStr = RemoveSpace(tempStr);
        ArrayList<Character> list = new ArrayList<>();

        for(int i = 0 ; i < tempStr.length();i++)
        {
            char temp = tempStr.charAt(i);

            if(temp >= 0xAC00 && temp <= 0xD7A3)
            {
                char uniVal = (char) (temp - 0xAC00);

                char cho = (char) ((((uniVal - (uniVal % 28))/28)/21) + 0xAC00);
                char jun = (char) ((((uniVal - (uniVal % 28))/28)%21) + 0xAC20);
                char jon = (char) ((uniVal %28) + 0xAC40);

                list.add(cho);
                list.add(jun);
                if(jon != 0xAC40){
                    list.add(jon);
                }
            }
            else{
                list.add(temp);
            }
        }
        return list;
    }

    private String SelectName() throws Exception{
        ArrayList<Character> dividedName = DivideHangul(userStr);
        setRNList();
        ArrayList<String> NList = new ArrayList<>(RNList.keySet());
        ArrayList<Integer> LCS = new ArrayList<>();
        String name = "식당없음";
        int max = -1;
        for (String N : NList) {
            ArrayList<Character> temp = DivideHangul(N);
            int[][] LCSArr = new int[temp.size()+1][dividedName.size()+1];
            for (int i = 0; i < temp.size(); i++) {
                for (int j = 0; j < dividedName.size(); j++) {
                    if(temp.get(i).equals(dividedName.get(j))){
                        LCSArr[i+1][j+1] = LCSArr[i][j] + 1;
                    }
                    else{
                        if(LCSArr[i+1][j] > LCSArr[i][j+1]){
                            LCSArr[i+1][j+1] = LCSArr[i+1][j];
                        }
                        else{
                            LCSArr[i+1][j+1] = LCSArr[i][j+1];
                        }
                    }
                }
            }
            LCS.add(LCSArr[temp.size()][dividedName.size()]);
        }
        for (Integer lcs : LCS){
            if(lcs > max){
                max = lcs;
            }
        }
        if(max > 0){
            name = NList.get(LCS.indexOf(max));
        }
        return name;
    }
    private String Language(String query) throws Exception{
        JsonParser parser = new JsonParser();
        JsonObject obj;
        String language = "ko";

        URL search = new URL("https://translation.googleapis.com/language/translate/v2/detect?q="
                + SearchStr(query) +"&key=" + myKey);

        URLConnection urlConn = search.openConnection();
        obj = (JsonObject) parser.parse(new InputStreamReader(urlConn.getInputStream()));
        JsonObject data = (JsonObject)obj.get("data");
        if(data != null){
            JsonArray detections = (JsonArray)data.get("detections");
            JsonArray tmp = (JsonArray)detections.get(0);
            JsonObject result = (JsonObject)tmp.get(0);
            language = result.get("language").getAsString();
        }
        return language;
    }

    public String Translate(String query) throws Exception{
        String language = Language(query);
        if(language.equals("ko") ||language.equals("und")){
            return query;
        }
        JsonParser parser = new JsonParser();
        JsonObject obj;

        URL search = new URL("https://translation.googleapis.com/language/translate/v2?q="
                + SearchStr(query) + "&target=ko" + "&source=" + language +"&key=" + myKey);

        URLConnection urlConn = search.openConnection();
        obj = (JsonObject) parser.parse(new InputStreamReader(urlConn.getInputStream()));
        JsonObject data = (JsonObject)obj.get("data");
        JsonArray translations = (JsonArray)data.get("translations");
        JsonObject translatedText = (JsonObject)translations.get(0);
        String result = translatedText.get("translatedText").getAsString();

        return result;
    }
    private String SearchStr(String s){
        s = s.replaceAll(" ", "+");
        s = s.replaceAll("\n","+");
        return s;
    }
}