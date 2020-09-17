package com.apps.emdad.services;

import com.apps.emdad.models.CategoryDataModel;
import com.apps.emdad.models.CountryDataModel;
import com.apps.emdad.models.CustomPlaceDataModel;
import com.apps.emdad.models.CustomPlaceDataModel2;
import com.apps.emdad.models.NearbyModel;
import com.apps.emdad.models.OfferSettingModel;
import com.apps.emdad.models.OrderModel;
import com.apps.emdad.models.OrdersDataModel;
import com.apps.emdad.models.PlaceDetailsModel;
import com.apps.emdad.models.PlaceGeocodeData;
import com.apps.emdad.models.PlaceMapDetailsData;
import com.apps.emdad.models.ShopDepartmentDataModel;
import com.apps.emdad.models.SingleOrderDataModel;
import com.apps.emdad.models.SliderModel;
import com.apps.emdad.models.UserModel;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface Service {

    @GET("place/findplacefromtext/json")
    Call<PlaceMapDetailsData> searchOnMap(@Query(value = "inputtype") String inputtype,
                                          @Query(value = "input") String input,
                                          @Query(value = "fields") String fields,
                                          @Query(value = "language") String language,
                                          @Query(value = "key") String key
    );


    @GET("place/nearbysearch/json")
    Call<NearbyModel> nearbyPlaceRankBy(@Query(value = "location") String location,
                                        @Query(value = "keyword") String keyword,
                                        @Query(value = "rankby") String rankby,
                                        @Query(value = "language") String language,
                                        @Query(value = "pagetoken") String pagetoken,
                                        @Query(value = "key") String key
    );

    @GET("place/nearbysearch/json")
    Call<NearbyModel> nearbyPlaceInDistance(@Query(value = "location") String location,
                                            @Query(value = "keyword") String keyword,
                                            @Query(value = "radius") int radius,
                                            @Query(value = "language") String language,
                                            @Query(value = "pagetoken") String pagetoken,
                                            @Query(value = "key") String key
    );

    @GET("geocode/json")
    Call<PlaceGeocodeData> getGeoData(@Query(value = "latlng") String latlng,
                                      @Query(value = "language") String language,
                                      @Query(value = "key") String key);

    @GET("place/details/json")
    Call<PlaceDetailsModel> getPlaceDetails(@Query(value = "placeid") String placeid,
                                            @Query(value = "fields") String fields,
                                            @Query(value = "language") String language,
                                            @Query(value = "key") String key
    );


    @GET("api/countries")
    Call<CountryDataModel> getCountries(@Query(value = "lang") String lang);

    @GET
    Call<ResponseBody> getFullUrl(@Url String url);


    @FormUrlEncoded
    @POST("api/login")
    Call<UserModel> login(@Field("phone_code") String phone_code,
                          @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("api/register")
    Call<UserModel> signUpWithoutImage(@Field("name") String name,
                                       @Field("email") String email,
                                       @Field("phone_code") String phone_code,
                                       @Field("phone") String phone,
                                       @Field("gender") String gender,
                                       @Field("date_of_birth") String date_of_birth,
                                       @Field("id_country") String id_country,
                                       @Field("software_type") String software_type

    );

    @Multipart
    @POST("api/register")
    Call<UserModel> signUpWithImage(@Part("name") RequestBody name,
                                    @Part("email") RequestBody email,
                                    @Part("phone_code") RequestBody phone_code,
                                    @Part("phone") RequestBody phone,
                                    @Part("gender") RequestBody gender,
                                    @Part("date_of_birth") RequestBody date_of_birth,
                                    @Part("id_country") RequestBody id_country,
                                    @Part("software_type") RequestBody software_type,
                                    @Part MultipartBody.Part image
    );


    @FormUrlEncoded
    @POST("api/update-firebase")
    Call<ResponseBody> updatePhoneToken(@Header("Authorization") String user_token,
                                        @Field("phone_token") String phone_token,
                                        @Field("user_id") int user_id,
                                        @Field("software_type") String software_type
    );

    @FormUrlEncoded
    @POST("api/logout")
    Call<ResponseBody> logout(@Header("Authorization") String user_token,
                              @Field("phone_token") String firebase_token,
                              @Field("software_type") String software_type


    );

    @GET("api/slider")
    Call<SliderModel> getSlider();

    @GET("api/get-profile")
    Call<UserModel> getUserById(@Header("Authorization") String user_token,
                                @Query(value = "lang") String lang,
                                @Query(value = "user_id") int user_id);


    @FormUrlEncoded
    @POST("api/update-receive-notifications")
    Call<UserModel> updateReceiveNotification(@Header("Authorization") String user_token,
                                              @Field("user_id") int user_id,
                                              @Field("receive_notifications") String receive_notifications


    );


    @GET("api/category")
    Call<CategoryDataModel> getCategory();

    @GET("api/get-place-by-google-id")
    Call<CustomPlaceDataModel> getCustomPlaceByGooglePlaceId(@Query(value = "google_place_id") String google_place_id);

    @GET("api/place-by-category")
    Call<CustomPlaceDataModel2> getCustomShops(@Query(value = "department_id") String department_id,
                                               @Query(value = "page") int page,
                                               @Query(value = "pagination") String pagination,
                                               @Query(value = "limit_per_page") int limit_per_page
    );

    @GET("api/get-place-departments-products")
    Call<ShopDepartmentDataModel> getShopDepartmentProduct(@Query(value = "market_id") String market_id);

    @GET("api/offer-settings")
    Call<OfferSettingModel> getOfferSetting();


    @FormUrlEncoded
    @POST("api/create-order")
    Call<SingleOrderDataModel> sendTextOrder(@Header("Authorization") String user_token,
                                             @Field("user_id") int user_id,
                                             @Field("order_type") String order_type,
                                             @Field("market_id") int market_id,
                                             @Field("google_place_id") String google_place_id,
                                             @Field("bill_cost") String bill_cost,
                                             @Field("client_address") String client_address,
                                             @Field("client_latitude") double client_latitude,
                                             @Field("client_longitude") double client_longitude,
                                             @Field("market_name") String market_name,
                                             @Field("market_address") String market_address,
                                             @Field("market_latitude") double market_latitude,
                                             @Field("market_longitude") double market_longitude,
                                             @Field("order_time_arrival") String order_time_arrival,
                                             @Field("coupon_id") String coupon_id,
                                             @Field("details") String details,
                                             @Field("notes") String notes


    );

    @Multipart
    @POST("api/create-order")
    Call<SingleOrderDataModel> sendTextOrderWithImage(@Header("Authorization") String user_token,
                                                      @Part("user_id") RequestBody user_id,
                                                      @Part("order_type") RequestBody order_type,
                                                      @Part("market_id") RequestBody market_id,
                                                      @Part("google_place_id") RequestBody google_place_id,
                                                      @Part("bill_cost") RequestBody bill_cost,
                                                      @Part("client_address") RequestBody client_address,
                                                      @Part("client_latitude") RequestBody client_latitude,
                                                      @Part("client_longitude") RequestBody client_longitude,
                                                      @Part("market_name") RequestBody market_name,
                                                      @Part("market_address") RequestBody market_address,
                                                      @Part("market_latitude") RequestBody market_latitude,
                                                      @Part("market_longitude") RequestBody market_longitude,
                                                      @Part("order_time_arrival") RequestBody order_time_arrival,
                                                      @Part("coupon_id") RequestBody coupon_id,
                                                      @Part("details") RequestBody details,
                                                      @Part("notes") RequestBody notes,
                                                      @Part List<MultipartBody.Part> images


    );


    @FormUrlEncoded
    @POST("api/update-location")
    Call<ResponseBody> updateLocation(@Header("Authorization") String user_token,
                                      @Field("user_id") int user_id,
                                      @Field("latitude") double latitude,
                                      @Field("longitude") double longitude
    );


    @GET("api/get-client-orders")
    Call<OrdersDataModel> getClientOrder(@Header("Authorization") String user_token,
                                         @Query(value = "user_id") int user_id,
                                         @Query(value = "type") String type,
                                         @Query(value = "page") int page,
                                         @Query(value = "pagination") String pagination,
                                         @Query(value = "limit_per_page") int limit_per_page
    );
}