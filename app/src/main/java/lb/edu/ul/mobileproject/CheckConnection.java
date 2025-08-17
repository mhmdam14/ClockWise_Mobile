package lb.edu.ul.mobileproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class CheckConnection {
    public static boolean isInternetConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            android.net.Network network = connectivityManager.getActiveNetwork();
            if (network == null) return true;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities == null ||
                    (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                            !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
                            !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return true;
    }
}
