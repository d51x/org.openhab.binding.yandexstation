package org.openhab.binding.yandexstation.internal.yandexapi;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.yandexstation.internal.YandexStationHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = YandexApiFactory.class)
@NonNullByDefault
public class YandexApiFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HttpClient httpClient;

    @Activate
    public YandexApiFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    public YandexApi getApi() throws ApiException {
        return new YandexApiImpl(httpClient);
    }
}
