package com.tanvir.training.weatherappbatch1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;
import com.tanvir.training.weatherappbatch1.databinding.FragmentWeatherBinding;
import com.tanvir.training.weatherappbatch1.models.current.CurrentResponseModel;
import com.tanvir.training.weatherappbatch1.utils.Constants;
import com.tanvir.training.weatherappbatch1.utils.LocationPermissionService;
import com.tanvir.training.weatherappbatch1.utils.WeatherHelperFunctions;
import com.tanvir.training.weatherappbatch1.viewmodels.WeatherViewModel;

public class WeatherFragment extends Fragment {
    private final String TAG = WeatherFragment.class.getSimpleName();
    private WeatherViewModel viewModel;
    private FragmentWeatherBinding binding;
    private FusedLocationProviderClient providerClient;
    private ActivityResultLauncher<String> launcher =
            registerForActivityResult(new ActivityResultContracts
                            .RequestPermission(),
                    result -> {
                if (result) {
                    detectUserLocation();
                }else {
                    //show dialog and explain why you need this permission
                }
            });
    public WeatherFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWeatherBinding.inflate(inflater);
        providerClient = LocationServices
                .getFusedLocationProviderClient(getActivity());
        viewModel = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);
        checkLocationPermission();
        viewModel.getCurrentLiveData().observe(getViewLifecycleOwner(),
                currentResponseModel -> {
            setData(currentResponseModel);
                    Log.e(TAG, "current: "+currentResponseModel.getMain().getTemp());
        });
        viewModel.getForecastLiveData().observe(getViewLifecycleOwner(),
                forecastResponseModel -> {
                    Log.e(TAG, "forecast: "+forecastResponseModel.getList().size());
        });
        return binding.getRoot();
    }

    private void setData(CurrentResponseModel currentResponseModel) {
        binding.currentDateTV.setText(WeatherHelperFunctions
        .getFormattedDateTime(currentResponseModel.getDt(), "MMM dd, yyyy"));
        binding.currentAddressTV.setText(
                currentResponseModel.getName()+","+currentResponseModel.getSys().getCountry()
        );
        binding.currentTempTV.setText(
                String.format("%.0f\u00B0", currentResponseModel.getMain().getTemp())
        );

        binding.currentFeelsLikeTV.setText(
                String.format("feels like %.0f\u00B0", currentResponseModel.getMain().getFeelsLike())
        );

        binding.currentMaxMinTV.setText(
                String.format("Max %.0f\u00B0 Min %.0f\u00B0", currentResponseModel.getMain().getTempMax(),
                        currentResponseModel.getMain().getTempMin())
        );

        final String iconUrl = Constants.ICON_PREFIX+
                currentResponseModel.getWeather().get(0).getIcon()+
                Constants.ICON_SUFFIX;
        Picasso.get().load(iconUrl).into(binding.currentIconIV);
        binding.currentConditionTV.setText(
                currentResponseModel.getWeather().get(0).getDescription()
        );

        binding.currentHumidityTV.setText("Humidity "+
                currentResponseModel.getMain().getHumidity()+"%");
        binding.currentPressureTV.setText("Pressure "+
                currentResponseModel.getMain().getPressure()+"hPa");
    }

    private void checkLocationPermission() {
        if (LocationPermissionService.isLocationPermissionGranted(getActivity())) {
            detectUserLocation();
        } else {
            LocationPermissionService.requestLocationPermission(launcher);
        }
    }

    @SuppressLint("MissingPermission")
    private void detectUserLocation() {
        providerClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) return;
                    //double lat = location.getLatitude();
                    //double lng = location.getLongitude();
                    viewModel.setLocation(location);
                    viewModel.loadData();
                    //Log.e("WeatherApp", "Lat:"+lat+",lon:"+lng);
                });
    }
}