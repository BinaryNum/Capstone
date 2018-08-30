package capstone.android.com.whattoeat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;

public class HP {
    private String name;

    HP(String name){
        this.name = SearchStr(name);
    }
    
    private String SearchStr(String s){
        s = s.replaceAll(" ", "+");
        return s;
    }

    private ArrayList<String> getId() throws Exception{
        ArrayList<String> idList = new ArrayList<>();

        URL url = new URL("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query=" + name);
        Document page = Jsoup.connect(url.toString())
                .header("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7, arg1")
                .header("accept-encoding", "gzip, deflate, br")
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .get();

        String s = page.select("a.biz_name").toString();
        if(s.length()!=0){
            s = s.split(";")[1].split("&")[0].split("=")[1];
            idList.add(s);
        }

        s = page.select("dl.info_area > dt > a").toString();
        if(s.length()!=0){
            String[] idArr = s.split("code=");
            for(int i=1; i<idArr.length;i++){
                idList.add(idArr[i].split("\"")[0]);
            }
        }
        return idList;
    }

    private ArrayList<String> SearchMenu(String id) throws Exception{

        ArrayList<String> menu = new ArrayList<>();

        URL url = new URL("https://store.naver.com/restaurants/detail?id=" + id + "&tab=menu");
        Document page = Jsoup.connect(url.toString())
                .header("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7, arg1")
                .header("accept-encoding", "gzip, deflate, br")
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .get();

        Elements e = page.select("div.sc_box");
        for(int i=0;i<e.select("div.price").size();i++){
            menu.add(e.select("div.tit").toString().split("\"tit\">")[i+1].split("<")[0].replaceAll("\n","") + ":"
                    + e.select("div.price").toString().split("\"price\">")[i+1].split("<")[0].replaceAll("\n","")
                    + e.select("span.price_unit").toString().split("\"price_unit\">")[i+1].split("<")[0]);
        }

        return menu;
    }

    public ArrayList<String> getMenu() throws Exception{
        ArrayList<String> idList = getId();
        ArrayList<String> menu = new ArrayList<>();

        for(String id : idList){
            menu = SearchMenu(id);
            if(!menu.isEmpty()){
                break;
            }
        }
        return menu;
    }
}
