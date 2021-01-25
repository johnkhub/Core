package za.co.imqs.coreservice.imports;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.*;

import java.net.URI;
import java.util.Map;
import java.util.Set;

public class FakeTemplate implements RestOperations {
    @Override
    public <T> T getForObject(String s, Class<T> aClass, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public <T> T getForObject(String s, Class<T> aClass, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public <T> T getForObject(URI uri, Class<T> aClass) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String s, Class<T> aClass, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String s, Class<T> aClass, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(URI uri, Class<T> aClass) throws RestClientException {
        return null;
    }

    @Override
    public HttpHeaders headForHeaders(String s, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public HttpHeaders headForHeaders(String s, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public HttpHeaders headForHeaders(URI uri) throws RestClientException {
        return null;
    }

    @Override
    public URI postForLocation(String s, Object o, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public URI postForLocation(String s, Object o, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public URI postForLocation(URI uri, Object o) throws RestClientException {
        return null;
    }

    @Override
    public <T> T postForObject(String s, Object o, Class<T> aClass, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public <T> T postForObject(String s, Object o, Class<T> aClass, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public <T> T postForObject(URI uri, Object o, Class<T> aClass) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String s, Object o, Class<T> aClass, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String s, Object o, Class<T> aClass, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(URI uri, Object o, Class<T> aClass) throws RestClientException {
        return null;
    }

    @Override
    public void put(String s, Object o, Object... objects) throws RestClientException {

    }

    @Override
    public void put(String s, Object o, Map<String, ?> map) throws RestClientException {

    }

    @Override
    public void put(URI uri, Object o) throws RestClientException {

    }

    @Override
    public <T> T patchForObject(String s, Object o, Class<T> aClass, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public <T> T patchForObject(String s, Object o, Class<T> aClass, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public <T> T patchForObject(URI uri, Object o, Class<T> aClass) throws RestClientException {
        return null;
    }

    @Override
    public void delete(String s, Object... objects) throws RestClientException {

    }

    @Override
    public void delete(String s, Map<String, ?> map) throws RestClientException {

    }

    @Override
    public void delete(URI uri) throws RestClientException {

    }

    @Override
    public Set<HttpMethod> optionsForAllow(String s, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public Set<HttpMethod> optionsForAllow(String s, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public Set<HttpMethod> optionsForAllow(URI uri) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(String s, HttpMethod httpMethod, HttpEntity<?> httpEntity, Class<T> aClass, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(String s, HttpMethod httpMethod, HttpEntity<?> httpEntity, Class<T> aClass, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI uri, HttpMethod httpMethod, HttpEntity<?> httpEntity, Class<T> aClass) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(String s, HttpMethod httpMethod, HttpEntity<?> httpEntity, ParameterizedTypeReference<T> parameterizedTypeReference, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(String s, HttpMethod httpMethod, HttpEntity<?> httpEntity, ParameterizedTypeReference<T> parameterizedTypeReference, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI uri, HttpMethod httpMethod, HttpEntity<?> httpEntity, ParameterizedTypeReference<T> parameterizedTypeReference) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> aClass) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, ParameterizedTypeReference<T> parameterizedTypeReference) throws RestClientException {
        return null;
    }

    @Override
    public <T> T execute(String s, HttpMethod httpMethod, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Object... objects) throws RestClientException {
        return null;
    }

    @Override
    public <T> T execute(String s, HttpMethod httpMethod, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Map<String, ?> map) throws RestClientException {
        return null;
    }

    @Override
    public <T> T execute(URI uri, HttpMethod httpMethod, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
        return null;
    }
}
