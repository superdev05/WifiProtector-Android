package com.wifiprotector.android.api;

import com.squareup.okhttp.Response;
import com.wifiprotector.android.utils.NetworkUtils;

import java.util.HashMap;
import java.util.Map;

public class RegisterUser3Request extends BaseRequest<RegisterUser3Response> {

    private String activationCode;
    private boolean registerAsPaidUser;

    public RegisterUser3Request(String activationCode, boolean registerAsPaidUser) {
        super(RegisterUser3Response.class);
        this.activationCode = activationCode;
        this.registerAsPaidUser = registerAsPaidUser;
    }

    @Override
    protected String getServiceName() {
        return "RegisterUser3";
    }

    @Override
    protected Map<String, String> postParams() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("uniqueId", getDeviceId());
        map.put("mac", NetworkUtils.getMacAddressSeparatedByHyphens());
        map.put("activationCode", activationCode);
        map.put("license", "mobile");

        return map;
    }

    @Override
    public RegisterUser3Response loadDataFromNetwork() throws Exception {
        Response response = executeRequest();
        String value = response.header("WifiProtReturnValue");
        String message = response.header("WifiProtReturnMessage");
        return new RegisterUser3Response(activationCode, value, message);
    }

    @Override
    protected String getLicenceType() {
        return registerAsPaidUser ? PAID_LICENSE : FREE_LICENSE;
    }
}
