package cz.martykan.forecastie;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WeatherRecyclerAdapter extends RecyclerView.Adapter<WeatherViewHolder> {
    private Context context;
    private List<Weather> itemList;
    private ViewPagerTabType type;

    public WeatherRecyclerAdapter(Context context, List<Weather> itemList, ViewPagerTabType type) {
        this.context = context;
        this.itemList = itemList;
        this.type = type;
    }

    @Override
    public WeatherViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);

        WeatherViewHolder viewHolder = new WeatherViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WeatherViewHolder customViewHolder, int i) {
        Weather weatherItem = itemList.get(i);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String temperature = weatherItem.getTemperature();

        if (sp.getString("unit", "C").equals("C")) {
            temperature = Float.parseFloat(temperature) - 273.15 + "";
        }

        if (sp.getString("unit", "C").equals("F")) {
            temperature = (((9 * (Float.parseFloat(temperature) - 273.15)) / 5)  + 32) + "";
        }

        double wind = Double.parseDouble(weatherItem.getWind());
        if(sp.getString("speedUnit", "m/s").equals("kph")){
            wind = wind * 3.59999999712;
        }

        if (sp.getString("speedUnit", "m/s").equals("mph")) {
            wind = wind * 2.23693629205;
        }

        double pressure = Double.parseDouble(weatherItem.getPressure());
        if(sp.getString("pressureUnit", "hPa").equals("kPa")){
            pressure = pressure/10;
        }
        if(sp.getString("pressureUnit", "hPa").equals("mm Hg")){
            pressure = pressure*0.750061561303;
        }

        Date date;
        try {
            date = new Date(Long.parseLong(weatherItem.getDate()) * 1000);
        }
        catch (Exception e) {
            date = new Date();
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                date = inputFormat.parse(weatherItem.getDate());
            }
            catch (ParseException e2) {
                e2.printStackTrace();
            }
        }

        TimeZone tz = Calendar.getInstance().getTimeZone();
        String defaultDateFormat = context.getResources().getStringArray(R.array.dateFormatsValues)[0];
        String dateFormat = sp.getString("dateFormat", defaultDateFormat);
        System.out.println("dateFormat = " + dateFormat);
        if ("custom".equals(dateFormat)) {
            dateFormat = sp.getString("dateFormatCustom", defaultDateFormat);
        }
        String dateString;
        try {
            SimpleDateFormat resultFormat = new SimpleDateFormat(dateFormat);
            resultFormat.setTimeZone(tz);
            dateString = resultFormat.format(date);
        } catch (IllegalArgumentException e) {
            dateString = context.getResources().getString(R.string.error_dateFormat);
        }

        customViewHolder.itemDate.setText(dateString);
        customViewHolder.itemTemperature.setText(temperature.substring(0, temperature.indexOf(".") + 2) + " °"+ sp.getString("unit", "C"));
        if(Float.parseFloat(weatherItem.getRain()) > 0.1){
            customViewHolder.itemDescription.setText(weatherItem.getDescription().substring(0, 1).toUpperCase() + weatherItem.getDescription().substring(1) + " (" + weatherItem.getRain().substring(0, weatherItem.getRain().indexOf(".") + 2) + " mm)");
        }
        else {
            customViewHolder.itemDescription.setText(weatherItem.getDescription().substring(0, 1).toUpperCase() + weatherItem.getDescription().substring(1));

        }
        Typeface weatherFont = Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");
        customViewHolder.itemIcon.setTypeface(weatherFont);
        customViewHolder.itemIcon.setText(weatherItem.getIcon());
        customViewHolder.itemyWind.setText(context.getString(R.string.wind) + ": " + (wind+"").substring(0, (wind+"").indexOf(".") + 2) + " " + sp.getString("speedUnit", "m/s"));
        customViewHolder.itemPressure.setText(context.getString(R.string.pressure) + ": " + (pressure+"").substring(0, (pressure + "").indexOf(".") + 2) + " " + sp.getString("pressureUnit", "hPa"));
        customViewHolder.itemHumidity.setText(context.getString(R.string.humidity) + ": " + weatherItem.getHumidity() + " %");
    }

    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }
}
