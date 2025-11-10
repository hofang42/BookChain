package com.hofang.bookchainfe.network;

import com.hofang.bookchainfe.model.Address;
import com.hofang.bookchainfe.model.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AddressApiService {

    @GET("api/addresses")
    Call<ApiResponse<AddressListResponse>> getAllAddresses(@Header("Authorization") String token);

    @GET("api/addresses/default")
    Call<ApiResponse<AddressResponse>> getDefaultAddress(@Header("Authorization") String token);

    @GET("api/addresses/{id}")
    Call<ApiResponse<AddressResponse>> getAddressById(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @POST("api/addresses")
    Call<ApiResponse<AddressResponse>> createAddress(
            @Header("Authorization") String token,
            @Body Address address
    );

    @PUT("api/addresses/{id}")
    Call<ApiResponse<AddressResponse>> updateAddress(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body Address address
    );

    @DELETE("api/addresses/{id}")
    Call<ApiResponse<Void>> deleteAddress(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @PUT("api/addresses/{id}/default")
    Call<ApiResponse<AddressResponse>> setDefaultAddress(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    // Response wrapper classes
    class AddressListResponse {
        private List<Address> addresses;

        public List<Address> getAddresses() {
            return addresses;
        }

        public void setAddresses(List<Address> addresses) {
            this.addresses = addresses;
        }
    }

    class AddressResponse {
        private Address address;

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }
    }
}
