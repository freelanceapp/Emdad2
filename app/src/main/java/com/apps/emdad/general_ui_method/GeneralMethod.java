package com.apps.emdad.general_ui_method;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.apps.emdad.R;
import com.apps.emdad.models.NearbyModel;
import com.apps.emdad.tags.Tags;
import com.google.android.gms.common.util.MapUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GeneralMethod {

    @BindingAdapter("error")
    public static void errorValidation(View view, String error) {
        if (view instanceof EditText) {
            EditText ed = (EditText) view;
            ed.setError(error);
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setError(error);


        }
    }


    @BindingAdapter("placeStoreImage")
    public static void placeStoreImage(View view, NearbyModel.Result result) {
        if (view instanceof CircleImageView) {
            CircleImageView imageView = (CircleImageView) view;


            if (result.getPhotos()!=null){
                if (result.getPhotos().size()>0)
                {
                    String url = Tags.IMAGE_Places_URL+result.getPhotos().get(0).getPhoto_reference()+"&key="+view.getContext().getString(R.string.map_api_key);
                    Picasso.get().load(Uri.parse(url)).fit().into(imageView);

                }else
                {
                    Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

                }
            }else {
                Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

            }



        } else if (view instanceof RoundedImageView) {
            RoundedImageView imageView = (RoundedImageView) view;

            if (result.getPhotos()!=null){
                if (result.getPhotos().size()>0)
                {
                    String url = Tags.IMAGE_Places_URL+result.getPhotos().get(0).getPhoto_reference()+"&key="+view.getContext().getString(R.string.map_api_key);
                    Picasso.get().load(Uri.parse(url)).fit().into(imageView);

                }else
                {
                    Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

                }
            }else {
                Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

            }

        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;

            if (result.getPhotos()!=null){
                if (result.getPhotos().size()>0)
                {
                    String url = Tags.IMAGE_Places_URL+result.getPhotos().get(0).getPhoto_reference()+"&key="+view.getContext().getString(R.string.map_api_key);
                    Picasso.get().load(Uri.parse(url)).fit().into(imageView);

                }else
                {
                    Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

                }
            }else {
                Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

            }

        }

    }

    @BindingAdapter({"lat1","lng1","lat2","lng2"})
    public static void calculateDistance(TextView view,double lat1,double lng1,double lat2,double lng2){
        Log.e("ddd",lat1+" "+lng1+" _"+lat2+" "+lng2);
        double v = SphericalUtil.computeDistanceBetween(new LatLng(lat1, lng1), new LatLng(lat2, lng2))/1000;
        view.setText(String.format(Locale.ENGLISH,"%.2f %s",v,view.getContext().getString(R.string.km)));
    }





    @BindingAdapter("image")
    public static void image(View view, String endPoint) {
        if (view instanceof CircleImageView) {
            CircleImageView imageView = (CircleImageView) view;
            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).placeholder(R.drawable.ic_avatar).into(imageView);
            } else {
                Picasso.get().load(R.drawable.ic_avatar).into(imageView);

            }
        } else if (view instanceof RoundedImageView) {
            RoundedImageView imageView = (RoundedImageView) view;

            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).placeholder(R.drawable.ic_avatar).fit().into(imageView);
            } else {
                Picasso.get().load(R.drawable.ic_avatar).into(imageView);

            }
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;

            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).placeholder(R.drawable.ic_avatar).fit().into(imageView);
            } else {
                Picasso.get().load(R.drawable.ic_avatar).into(imageView);

            }
        }

    }


}










