package za.co.imqs.api.asset.tag;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dto.AssetEnvelopeDto;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static za.co.imqs.coreservice.dataaccess.LookupProvider.Kv.pair;

public class Populate {

    public static void populate(String session, UUID uuid) throws Exception {
        AbstractAssetControllerAPITest.configure();


        // Remove tags
        given().header("Cookie", session).delete("/assets/{uuid}/tag/{tag1}?{tag2}&{tag3}", uuid, "TAG1", "TAG2", "TAG3");

        // Delete asset
        given().header("Cookie", session).delete("/assets/testing/{uuid}", uuid);


        // Add asset
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new AbstractAssetControllerAPITest.CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at")
                .get();

        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", uuid).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        // Add tags
        final List<LookupProvider.Kv> kv = new LinkedList<>();
        kv.add(pair("TAG1","TagO OneO"));
        kv.add(pair("TAG2","TagO TwoO"));
        kv.add(pair("TAG3","TagO ThreeO"));

        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(kv).
                put("/lookups/kv/{target}", "TAGS").
                then().statusCode(200);

    }
}
