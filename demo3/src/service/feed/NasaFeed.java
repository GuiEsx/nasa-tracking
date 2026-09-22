package service.feed;

public interface NasaFeed<T> {
    String getNomeFeed();
    String getEndpointUrl();
    T consultarFeed() throws Exception;
}

