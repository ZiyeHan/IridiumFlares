package com.cmps263.project.crawler;

import com.cmps263.project.commons.FileConstants;
import com.cmps263.project.commons.PageConstants;
import com.cmps263.project.commons.URLConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityLatLongCrawler {

    WebDriver driver;

    public static void main(String[] args){
        final CityLatLongCrawler crawler = new CityLatLongCrawler();
        crawler.init();
        crawler.crawl();
    }


    private void init(){
        driver = initDriver(driver);
    }

    private void crawl(){
        openPage(driver);
        Map<String, String> citiesLocationMap = getCitiesLocation(driver);
        storeCitiesLocation(citiesLocationMap);
    }

    private WebDriver initDriver(WebDriver driver){
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "/usr/local/bin/phantomjs");
        return new PhantomJSDriver(caps);
    }

    private void openPage(WebDriver driver){
        driver.get(URLConstants.CITIES_WEBSITE);
    }

    private Map<String, String> getCitiesLocation(WebDriver driver){
        Map<String,String> citiesLocationMap = new HashMap<String,String>();
        List<WebElement> citiesLocation = driver.findElements(By.xpath(PageConstants.CITIES_TRS_LOCATOR));
        //Remove top 2 useless lines from the table
        citiesLocation = citiesLocation.subList(2, citiesLocation.size());
        for(WebElement cityLocation: citiesLocation){
            String cityName = cityLocation.findElement(By.xpath(".//td[1]")).getText();
            cityName = cityName.substring(0, cityName.indexOf(",") + 1) + cityName.substring(cityName.indexOf(",") + 2, cityName.length());

            String cityLatitudeDegree = cityLocation.findElement(By.xpath(".//td[2]")).getText();
            String cityLatitudeDecimal = cityLocation.findElement(By.xpath(".//td[3]")).getText();

            boolean isS = false;
            if(cityLatitudeDecimal.contains("S")){
                isS = true;
            }

            cityLatitudeDecimal = cityLatitudeDecimal.substring(0, cityLatitudeDecimal.indexOf(' '));
            double cityLatitude = Double.valueOf(cityLatitudeDegree) + Double.valueOf(cityLatitudeDecimal) / 60.0;
            if(isS){
                cityLatitude = - Math.round(cityLatitude * 100.0) / 100.0;
            }else{
                cityLatitude = Math.round(cityLatitude * 100.0) / 100.0;
            }

            String cityLongitudeDegree = cityLocation.findElement(By.xpath(".//td[4]")).getText();
            String cityLongitudeDecimal = cityLocation.findElement(By.xpath(".//td[5]")).getText();

            boolean isW = false;
            if(cityLongitudeDecimal.contains("W")){
                isW = true;
            }

            cityLongitudeDecimal = cityLongitudeDecimal.substring(0, cityLongitudeDecimal.indexOf(' '));
            double cityLongitude = Double.valueOf(cityLongitudeDegree) + Double.valueOf(cityLongitudeDecimal) / 60.0;
            if(isW){
                cityLongitude = - Math.round(cityLongitude * 100.0) / 100.0;
            }else{
                cityLongitude = Math.round(cityLongitude * 100.0) / 100.0;
            }


            citiesLocationMap.put(cityName, cityLatitude + "," + cityLongitude);
        }
        return citiesLocationMap;
    }

    private void storeCitiesLocation(Map<String, String> citiesLocationMap){
        try{
            FileWriter fileStream = new FileWriter(FileConstants.CITIES_LOCATION_FILE_PATH);
            BufferedWriter out = new BufferedWriter(fileStream);
            out.write(FileConstants.CITIES_LOCATION_FILE_HEADER + "\n");
            for(Map.Entry<String, String> record: citiesLocationMap.entrySet()){
                out.write(record.getKey() + "," + record.getValue() + "\n");
            }
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
