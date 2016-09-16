package com.wifiprotector.android.api;

import org.simpleframework.xml.core.Persister;

public class GetLocationInfoRequest extends BaseRequest<LocationInfo> {
    public GetLocationInfoRequest() {
        super(LocationInfo.class);
    }

    @Override
    protected String getServiceName() {
        return "GetLocationInfo";
    }

    @Override
    public LocationInfo loadDataFromNetwork() throws Exception {
        return new Persister().read(LocationInfo.class, executeRequest().body().byteStream());
    }
}
